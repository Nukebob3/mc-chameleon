package net.nukebob.chameleon.gameplay;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class Raycast {
    public static HitResult raycast(Level level, Player except, boolean hider, Vec3 start, Vec3 lookVec, double maxDistance) {
        Vec3 direction = lookVec.normalize();
        double step = 0.1;
        double distanceTraveled = 0;
        while (distanceTraveled < maxDistance) {
            start = start.add(direction.scale(step));
            distanceTraveled += step;

            BlockPos blockPos = BlockPos.containing(start);
            BlockState blockState = level.getBlockState(blockPos);

            if (!blockState.isAir()) {
                boolean isPassable = blockState.is(Blocks.SHORT_GRASS)
                        || blockState.is(Blocks.TALL_GRASS)
                        || blockState.is(Blocks.FERN)
                        || blockState.is(Blocks.LARGE_FERN)
                        || blockState.is(Blocks.BARRIER);

                if (!isPassable) {
                    VoxelShape shape = blockState.getCollisionShape(level, blockPos);
                    if (!shape.isEmpty()) {
                        Vec3 prevPos = start.subtract(direction.scale(step));
                        BlockHitResult hit = shape.clip(prevPos, start, blockPos);
                        if (hit != null) {
                            return hit;
                        }
                    }
                }
            }

            AABB searchBox = new AABB(start.x - 0.2, start.y - 0.2, start.z - 0.2, start.x + 0.2, start.y + 0.2, start.z + 0.2);
            List<Entity> entities = level.getEntities(except, searchBox);

            for (Entity target : entities) {
                if (target instanceof LivingEntity livingTarget) {
                    if (hider) {
                        if (TeamControl.isHunter(livingTarget.getTeam())) {
                            continue;
                        }
                    }

                    return new EntityHitResult(livingTarget, start);
                }
            }
        }
        return null;
    }
}
