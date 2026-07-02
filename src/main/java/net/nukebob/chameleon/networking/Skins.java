package net.nukebob.chameleon.networking;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.texture.ColourLocation;

import java.util.*;

public class Skins {
    public static final Map<UUID, int[]> skinMap = new HashMap<>();

    public static void update(UUID uuid, int[] pixels) {
        skinMap.put(uuid, pixels);

        PlayerLookup.all(MCChameleon.SERVER).forEach(player -> {
            if (!player.getUUID().equals(uuid))
                ServerPlayNetworking.send(player, new Payloads.ClientBoundUpdatePixelsPayload(uuid, pixels));
        });
    }

    public static void blank(UUID uuid) {
        int[] pixels = new int[1504];
        for (int i = 0; i < 1504; i++) {
            pixels[i]=0xFFFFFFFF;
        }
        skinMap.put(uuid, pixels);

        PlayerLookup.all(MCChameleon.SERVER).forEach(player -> {
            ServerPlayNetworking.send(player, new Payloads.ClientBoundUpdatePixelsPayload(uuid, pixels));
        });
    }

    public static void update(UUID uuid, ColourLocation.ColLoc[] update) {
        int[] pixels = skinMap.get(uuid);
        for (ColourLocation.ColLoc colourLocation : update) {
            pixels[colourLocation.location()] = colourLocation.colour();
        }
        skinMap.put(uuid, pixels);

        PlayerLookup.all(MCChameleon.SERVER).forEach(player -> {
            if (!player.getUUID().equals(uuid))
                ServerPlayNetworking.send(player, new Payloads.ClientBoundUpdateSpecificPixelsPayload(uuid, update));
        });
    }

    public static void remove(UUID uuid) {
        skinMap.remove(uuid);

        PlayerLookup.all(MCChameleon.SERVER).forEach(player -> {
            ServerPlayNetworking.send(player, new Payloads.ClientBoundClearPixelsPayload(uuid));
        });
    }
}
