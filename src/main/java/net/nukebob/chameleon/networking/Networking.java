package net.nukebob.chameleon.networking;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.config.GameConfig;
import net.nukebob.chameleon.gameplay.*;
import net.nukebob.chameleon.render.GameHud;
import net.nukebob.chameleon.render.GunBeamRenderer;
import net.nukebob.chameleon.sound.ChameleonSounds;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.texture.ColourLocation;
import net.nukebob.chameleon.voicechat.VoiceChatAccess;

import java.util.List;
import java.util.UUID;

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
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientBoundClearPosesPayload.TYPE, (payload, context) -> {
            MCChameleonClient.POSES.clear();
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientBoundShotPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> MCChameleonClient.shots.add(new GunBeamRenderer(payload.start(), payload.end())));
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientBoundGameHudUpdatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                GameHud.time=payload.time();
                GameHud.maxTime= payload.maxTime();
                GameHud.whistle=payload.whistle();
                GameHud.subtitle = payload.subtitle();

                GameHud.timeSinceUpdate = 0;
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientBoundGameHudPlayersPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                GameHud.hiders = payload.hiders();
                GameHud.seekers = payload.seekers();
            });
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
                context.player().refreshDimensions();
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerBoundWhistle.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                if (!(Game.state.equals(GameState.SEEK)||Game.state.equals(GameState.HIDE))) return;

                var player = context.player();

                player.level().playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        GameConfig.loadConfig().isWhistleSound ? ChameleonSounds.WHISTLE : ChameleonSounds.FART,
                        SoundSource.PLAYERS,
                        3f, 1f);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerBoundShotPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                boolean hitPlayer = !payload.hit().equals(new UUID(0,0));
                context.player().level().playSound(null, context.player().getX(), context.player().getY(), context.player().getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1, 0.8f);
                PlayerLookup.all(context.server()).forEach(player -> {
                    if (hitPlayer) player.playSound(SoundEvents.BELL_BLOCK, 1, 0.5f);
                    if (!player.equals(context.player())) {
                        ServerPlayNetworking.send(player, new Payloads.ClientBoundShotPayload(context.player().position().add(context.player().getEyePosition()).scale(0.5), payload.target()));
                    }

                    if (hitPlayer&&Game.state.equals(GameState.SEEK))
                        if (payload.hit().equals(player.getUUID())&& TeamControl.isChameleon(player.getTeam())) {
                            PlayerLookup.all(context.server()).forEach(p -> p.sendOverlayMessage(Component.literal(context.player().getPlainTextName()).withColor(0xFFebae34).append(Component.literal(" found ").withColor(0xFFFFFFFF)).append(Component.literal(player.getPlainTextName()).withColor(0xFF27dbd8))));

                            ServerPlayNetworking.send(player, new Payloads.ClientBoundPosePayload(payload.hit(), -1));

                            if (GameConfig.loadConfig().isInfection) {
                                if (Game.getNumberOfHiders()>1) {
                                    Game.setSeeker(player);
                                    Game.mapTp(player);
                                } else {
                                    becomeSpectator(player);
                                }
                            } else {
                                becomeSpectator(player);
                            }
                            Game.updatePlayerCount();
                        }
                });
                spawnFirework(context.player(), hitPlayer, payload.target());
            });
        });
    }

    private static void becomeSpectator(ServerPlayer player) {
        player.setGameMode(GameType.SPECTATOR);

        VoiceChatAccess.addPlayerToGroup(player);
    }

    private static void spawnFirework(Player player, boolean hitPlayer, Vec3 pos) {
        int[] colors = hitPlayer? new int[]{0xFF0000, 0xFF7F00, 0xFFFF00, 0x00FF00, 0x0000FF, 0x4B0082, 0x9400D3} : new int[]{0xFFFFFF, 0xFFAAAA, 0xAAFFAA, 0xAAAAFF};
        var explosion = new FireworkExplosion(
                FireworkExplosion.Shape.BURST,
                IntArrayList.wrap(colors),
                IntArrayList.wrap(new int[0]), false, false
        );

        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.set(DataComponents.FIREWORKS, new Fireworks(0, List.of(explosion)));

        var level = player.level();
        FireworkRocketEntity firework = new FireworkRocketEntity(level, pos.x(), pos.y(), pos.z(), stack);

        level.addFreshEntity(firework);
        level.broadcastEntityEvent(firework, (byte) 17);
        firework.discard();
    }
}
