package net.nukebob.chameleon.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraOrbitMixin {

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    private @Nullable Entity entity;

    @Shadow
    public abstract float getCameraEntityPartialTicks(DeltaTracker deltaTracker);

    @Inject(method = "update", at = @At("RETURN"))
    private void mc_chameleon$applyOrbitOverride(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!ChameleonOrbitCamera.isActive() || this.entity == null) return;

        float yaw = ChameleonOrbitCamera.getYaw();
        float pitch = ChameleonOrbitCamera.getPitch();
        this.setRotation(yaw, pitch);

        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double dirX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dirY = -Math.sin(pitchRad);
        double dirZ = Math.cos(yawRad) * Math.cos(pitchRad);

        float partialTicks = this.getCameraEntityPartialTicks(deltaTracker);

        double eyeX = Mth.lerp(partialTicks, this.entity.xo, this.entity.getX());
        double eyeY = Mth.lerp(partialTicks, this.entity.yo, this.entity.getY()) + this.entity.getEyeHeight();
        double eyeZ = Mth.lerp(partialTicks, this.entity.zo, this.entity.getZ());
        Vec3 eyePos = new Vec3(eyeX, eyeY, eyeZ);

        double distance = ChameleonOrbitCamera.getDistance();
        this.setPosition(eyePos.add(-dirX * distance, -dirY * distance, -dirZ * distance));
    }
}