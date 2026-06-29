package net.nukebob.chameleon.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.texture.ColourLocation;

public class Networking {
    @Environment(EnvType.CLIENT)
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
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerBoundUpdatePixelsPayload.TYPE, (payload, context) -> {
            PlayerLookup.all(MCChameleon.SERVER).forEach(player -> {
                Skins.update(context.player().getUUID(), payload.pixels());
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ServerBoundUpdateSpecificPixelsPayload.TYPE, (payload, context) -> {
            PlayerLookup.all(MCChameleon.SERVER).forEach(player -> {
                Skins.update(context.player().getUUID(), payload.pixels());
            });
        });
    }
}
