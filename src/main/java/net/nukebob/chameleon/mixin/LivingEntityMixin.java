package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
        Player player = Minecraft.getInstance().player;
        if (player==null) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (!self.getUUID().equals(player.getUUID())) return;

        Level level = Minecraft.getInstance().level;
        if (level==null) return;
        if (!player.isJumping()) return;
        AABB box = self.getBoundingBox().inflate(0.1);


        if (mc_chameleon$isNextToWall(level, box, self))
            cir.setReturnValue(true);
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

        return  !bs0.getCollisionShape(level, BlockPos.containing(p0)).isEmpty()||
                !bs1.getCollisionShape(level, BlockPos.containing(p0)).isEmpty()||
                !bs2.getCollisionShape(level, BlockPos.containing(p0)).isEmpty()||
                !bs3.getCollisionShape(level, BlockPos.containing(p0)).isEmpty()||
                !bs4.getCollisionShape(level, BlockPos.containing(p0)).isEmpty();
    }
}
