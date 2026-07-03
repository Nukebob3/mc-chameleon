package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.MCChameleonClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    protected abstract void travelInAir(Vec3 input);

    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$playerWallClimb(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object)this instanceof Player player)) return;

        Level level = player.level();
        if (level == null) return;


        AABB box = player.getBoundingBox().inflate(0.1);
        if (level.isClientSide()) {
            if (player.onGround()) {
                MCChameleonClient.climbing = false;
                cir.setReturnValue(false);
                return;
            }
            if (!player.isJumping() && !MCChameleonClient.climbing) {
                cir.setReturnValue(false);
                return;
            }
            if (Minecraft.getInstance().options.keySprint.isDown()) {
                MCChameleonClient.climbing = false;
                cir.setReturnValue(false);
                return;
            }
            if (mc_chameleon$isNextToWall(level, box, player)) {
                MCChameleonClient.climbing = true;
                cir.setReturnValue(true);
                return;
            }
            MCChameleonClient.climbing = false;
        } else {
            if (player.onGround()) {
                cir.setReturnValue(false);
                return;
            }
            if (mc_chameleon$isNextToWall(level, box, player)) {
                cir.setReturnValue(true);
                return;
            }
        }

        cir.setReturnValue(false);
    }

    @Inject(method = "isSuppressingSlidingDownLadder", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$cling(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!self.level().isClientSide()) return;
        cir.setReturnValue(MCChameleonClient.climbing&&!self.isShiftKeyDown());
    }

    @Inject(method = "handleOnClimbable", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$jumpToClimb(Vec3 delta, CallbackInfoReturnable<Vec3> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof Player player)) return;
        if (player.onClimbable() && !player.isJumping()) {
            Vec3 result = cir.getReturnValue();
            cir.setReturnValue(new Vec3(result.x, Math.min(result.y, 0), result.z));
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$onlyWalk(Vec3 input, CallbackInfo ci) {
        travelInAir(input);
        ci.cancel();
    }

    @Unique
    private boolean mc_chameleon$isNextToWall(Level level, AABB box, LivingEntity self) {
        double maxOffset = 0.1;
        Vec3 p0 = new Vec3(box.minX - maxOffset, self.getY(), self.getZ());
        Vec3 p1 = new Vec3(box.maxX + maxOffset, self.getY(), self.getZ());
        Vec3 p2 = new Vec3(self.getX(), self.getY(), box.minZ - maxOffset);
        Vec3 p3 = new Vec3(self.getX(), self.getY(), box.maxZ + maxOffset);
        Vec3 p4 = new Vec3(self.getX(), box.maxY + maxOffset, self.getZ());
        BlockState bs0 = level.getBlockState(BlockPos.containing(p0));
        BlockState bs1 = level.getBlockState(BlockPos.containing(p1));
        BlockState bs2 = level.getBlockState(BlockPos.containing(p2));
        BlockState bs3 = level.getBlockState(BlockPos.containing(p3));
        BlockState bs4 = level.getBlockState(BlockPos.containing(p4));

        return  mc_chameleon$isClimbableBlockstate(bs0, p0, level)||
                mc_chameleon$isClimbableBlockstate(bs1, p1, level)||
                mc_chameleon$isClimbableBlockstate(bs2, p2, level)||
                mc_chameleon$isClimbableBlockstate(bs3, p3, level)||
                mc_chameleon$isClimbableBlockstate(bs4, p4, level);
    }

    @Unique
    private boolean mc_chameleon$isClimbableBlockstate(BlockState state, Vec3 pos, Level level) {
        if (state.is(Blocks.BARRIER)) return false;
        return !state.getCollisionShape(level, BlockPos.containing(pos)).isEmpty();
    }
}
