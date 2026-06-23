package net.nukebob.chameleon.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.texture.ColourLocation;

public class Networking {
    @Environment(EnvType.CLIENT)
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientboundUpdatePixelsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.uuid().equals(context.player().getUUID())) {
                    MCChameleonClient.localSkinCache= payload.pixels();
                }

                ChameleonTexture texture = new ChameleonTexture(payload.uuid());
                texture.init();
                texture.updatePixels(payload.pixels());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientboundClearPixelsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.uuid().equals(context.player().getUUID())) {
                    MCChameleonClient.localSkinCache=new int[1632];
                }

                context.client().getTextureManager().release(MCChameleon.idSkin(payload.uuid().toString()));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ClientboundUpdateSpecificPixelsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.uuid().equals(context.player().getUUID())) {
                    for (ColourLocation.ColLoc pixel: payload.pixels()) {
                        MCChameleonClient.localSkinCache[pixel.location()]  = pixel.colour();
                    }
                }

                ChameleonTexture texture = new ChameleonTexture(payload.uuid());
                texture.load();
                texture.updatePixelsSpecific(payload.pixels());
                context.client().getTextureManager().release(MCChameleon.idSkin(payload.uuid().toString()));
            });
        });
    }
}
