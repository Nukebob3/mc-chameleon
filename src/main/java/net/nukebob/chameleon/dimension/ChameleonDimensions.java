package net.nukebob.chameleon.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.nukebob.chameleon.MCChameleon;

public class ChameleonDimensions {
    public static final ResourceKey<Level> LOBBY = register("lobby");
    public static final ResourceKey<Level> MAPS = register("maps");

    private static ResourceKey<Level> register(final String id) {
        return ResourceKey.create(Registries.DIMENSION, MCChameleon.id(id));
    }

    public static void registerDimensions(){}
}
