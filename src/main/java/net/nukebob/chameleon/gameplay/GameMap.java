package net.nukebob.chameleon.gameplay;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.dimension.ChameleonDimensions;

public class GameMap {
    public static final GameMap MINECRAFT = new GameMap(ChameleonDimensions.MAPS, new Vec3(37.5, 102.9375, 48.5), new Vec2(0,0));

    public final ResourceKey<Level> level;
    public final Vec3 spawn;
    public final Vec2 rotation;

    public GameMap(ResourceKey<Level> level, Vec3 spawn, Vec2 rotation) {
        this.level = level;
        this.spawn = spawn;
        this.rotation = rotation;
    }
}
