package net.nukebob.chameleon.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.gameplay.TeamControl;
import net.nukebob.chameleon.item.ChameleonItems;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.render.GunBeamRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private DeltaTracker.Timer deltaTracker;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public Entity crosshairPickEntity;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$cancelAttack(CallbackInfoReturnable<Boolean> cir) {
        if (player!=null&&(player.isCreative()||player.isSpectator())) return;
        cir.setReturnValue(true);
        if (player!=null&&player.getMainHandItem().is(ChameleonItems.GUN)&&!player.getCooldowns().isOnCooldown(player.getMainHandItem())) {
            float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);

            Vec3 rayStart = player.getEyePosition(partialTicks);
            Vec3 end;
            UUID hit = new UUID(0, 0);
            Vec3 lookVec = this.player.getLookAngle();

            HitResult raycast = mc_chameleon$raycast(player, rayStart, lookVec, 100);
            if (raycast==null||raycast.getType().equals(HitResult.Type.MISS)) {
                end = rayStart.add(lookVec.scale(100));
            } else if (raycast instanceof EntityHitResult entityHitResult) {
                hit = entityHitResult.getEntity().getUUID();
                end = entityHitResult.getLocation();
            } else {
                end = raycast.getLocation();
            }

            Vec3 rightVec = lookVec.cross(new Vec3(0, player.getMainArm().equals(HumanoidArm.LEFT)?-1:1, 0)).normalize();
            if (rightVec.lengthSqr() < 0.001) rightVec = new Vec3(1, 0, 0);

            Vec3 start = rayStart.add(rightVec.scale(0.25D)).add(0, -0.2D, 0);
            MCChameleonClient.shots.add(new GunBeamRenderer(start, end));

            player.getCooldowns().addCooldown(player.getMainHandItem(), 20);
            ClientPlayNetworking.send(new Payloads.ServerBoundShotPayload(end, hit));
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$cancelUse(CallbackInfo ci) {
        if (player!=null&&player.isCreative()) return;
        ci.cancel();
    }

    @Unique
    private HitResult mc_chameleon$raycast(Player player, Vec3 start, Vec3 lookVec, double maxDistance) {
        Level level = player.level();
        Vec3 currentPos = start;
        Vec3 direction = lookVec.normalize();
        double step = 0.1;
        double distanceTraveled = 0;

        while (distanceTraveled < maxDistance) {
            currentPos = currentPos.add(direction.scale(step));
            distanceTraveled += step;

            BlockPos blockPos = BlockPos.containing(currentPos);
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
                        Vec3 prevPos = currentPos.subtract(direction.scale(step));
                        BlockHitResult hit = shape.clip(prevPos, currentPos, blockPos);
                        if (hit != null) {
                            return hit;
                        }
                    }
                }
            }

            AABB searchBox = new AABB(currentPos.x - 0.2, currentPos.y - 0.2, currentPos.z - 0.2, currentPos.x + 0.2, currentPos.y + 0.2, currentPos.z + 0.2);
            List<Entity> entities = level.getEntities(player, searchBox);

            for (Entity target : entities) {
                if (target instanceof LivingEntity livingTarget) {
                    if (TeamControl.isHunter(livingTarget.getTeam())) {
                        continue;
                    }

                    this.crosshairPickEntity = livingTarget;
                    return new EntityHitResult(livingTarget, currentPos);
                }
            }
        }
        return null;
    }
}
