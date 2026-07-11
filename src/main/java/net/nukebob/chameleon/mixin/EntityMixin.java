package net.nukebob.chameleon.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.gameplay.TeamControl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract boolean onGround();

    @Shadow
    @Nullable
    public abstract PlayerTeam getTeam();

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$redirectToCam(MoverType moverType, Vec3 delta, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (!self.level().isClientSide()) return;
        if (TeamControl.isChameleon(self.getTeam())&&self.isSpectator()) {
            ci.cancel();
            return;
        }
        ChameleonOrbitCamera cam = ChameleonOrbitCamera.getInstance();
        if (cam != null && cam.isActive() && cam.isInFreeCam()) ci.cancel();
    }

    @Inject(method = "isCrouching", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$noCrouch(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "getPose", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$replaceCrouch(CallbackInfoReturnable<Pose> cir) {
        if (!cir.getReturnValue().equals(Pose.CROUCHING)) return;
        cir.setReturnValue(
                TeamControl.isChameleon(getTeam()) || !onGround()
                        ? Pose.STANDING
                        : Pose.SWIMMING
        );
    }

    @Inject(method = "isDiscrete", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$alwaysShowNametagThroughWalls(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$noFireAnimation(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
