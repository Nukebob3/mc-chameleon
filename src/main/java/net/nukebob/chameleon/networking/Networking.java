package net.nukebob.chameleon.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.sounds.SoundSource;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.config.GameConfig;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.sound.ChameleonSounds;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.texture.ColourLocation;

public class Networking {
    //@Environment(EnvType.CLIENT)
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientBoundUpdatePixelsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.uuid().equals(context.player().getUUID())) {
                    MCChameleonClient.localSkinCache= payload.pixels();
                }

                ChameleonTexture texture = ChameleonTexture.getChameleonTexture(payload.uuid());
                texture.updatePixels(payload.pixels());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientBoundClearPixelsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.uuid().equals(context.player().getUUID())) {
                    MCChameleonClient.localSkinCache=new int[1504];
                }

                context.client().getTextureManager().release(MCChameleon.idSkin(payload.uuid().toString()));
                ChameleonTexture.skins.remove(payload.uuid());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientBoundUpdateSpecificPixelsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.uuid().equals(context.player().getUUID())) {
                    for (ColourLocation.ColLoc pixel: payload.pixels()) {
                        MCChameleonClient.localSkinCache[pixel.location()]  = pixel.colour();
                    }
                }

                ChameleonTexture texture = ChameleonTexture.getChameleonTexture(payload.uuid());
                texture.updatePixelsSpecific(payload.pixels());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientBoundPosePayload.TYPE, (payload, context) -> {
            MCChameleonClient.POSES.computeIfAbsent(payload.uuid(), uuid -> new PoseTracker()).setTargetPose(payload.pose()==-1?null:Poses.values()[payload.pose()]);
        });
    }

    //@Environment(EnvType.SERVER)
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerBoundUpdatePixelsPayload.TYPE, (payload, context) -> {
            context.server().execute(() ->Skins.update(context.player().getUUID(), payload.pixels()));
        });
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerBoundUpdateSpecificPixelsPayload.TYPE, (payload, context) -> {
            context.server().execute(() ->Skins.update(context.player().getUUID(), payload.pixels()));
        });
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerBoundPosePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                MCChameleon.POSES.computeIfAbsent(context.player().getUUID(), uuid -> new PoseTracker()).setTargetPose(payload.pose());
                PlayerLookup.all(MCChameleon.SERVER).forEach(player -> {
                    if (!player.getUUID().equals(context.player().getUUID())) ServerPlayNetworking.send(player, new Payloads.ClientBoundPosePayload(context.player().getUUID(), payload.pose()==null?-1:payload.pose().ordinal()));
                });
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerBoundWhistle.TYPE, (payload, context) -> {
            context.server().execute(() -> {

                var player = context.player();

                player.level().playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        GameConfig.loadConfig().isWhistleSound ? ChameleonSounds.WHISTLE : ChameleonSounds.FART,
                        SoundSource.PLAYERS,
                        3f, 1f);
            });
        });
    }
}
