package net.nukebob.chameleon;

import net.fabricmc.api.ClientModInitializer;
import net.nukebob.chameleon.networking.Networking;

public class MCChameleonClient implements ClientModInitializer {
    public static int[] localSkinCache = new int[1504];

    @Override
    public void onInitializeClient() {
        Networking.registerClientReceivers();
    }
}
