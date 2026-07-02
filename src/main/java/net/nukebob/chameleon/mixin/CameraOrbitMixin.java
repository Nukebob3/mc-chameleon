package net.nukebob.chameleon.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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

    @Shadow
    private @Nullable Level level;

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    public abstract Matrix4f getViewRotationMatrix(Matrix4f dest);

    @Shadow
    protected abstract Matrix4f createProjectionMatrixForCulling();

    @Shadow
    protected abstract void prepareCullFrustum(Matrix4fc modelViewMatrix, Matrix4f projectionMatrixForCulling, Vec3 cameraPos);

    @Inject(method = "update", at = @At("RETURN"))
    private void mc_chameleon$applyOrbitOverride(DeltaTracker deltaTracker, CallbackInfo ci) {
        ChameleonOrbitCamera camera = ChameleonOrbitCamera.getInstance();
        if (camera==null|| !camera.isActive() || this.entity == null || this.level==null) return;

        float yaw = camera.getYaw();
        float pitch = camera.getPitch();
        this.setRotation(yaw, pitch);

        if (camera.isInFreeCam()) {
            float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
            Vec3 velocity = camera.getDeltaMovement();

            double camX = camera.getX() + velocity.x*partialTicks;
            double camY = camera.getY() + velocity.y*partialTicks;
            double camZ = camera.getZ() + velocity.z*partialTicks;

            this.setPosition(camX, camY, camZ);
            this.setRotation(yaw, pitch);
        } else {
            double yawRad = Math.toRadians(yaw);
            double pitchRad = Math.toRadians(pitch);

            double dirX = -Math.sin(yawRad) * Math.cos(pitchRad);
            double dirY = -Math.sin(pitchRad);
            double dirZ = Math.cos(yawRad) * Math.cos(pitchRad);

            float partialTicks = this.getCameraEntityPartialTicks(deltaTracker);

            Player player = Minecraft.getInstance().player;
            if (player==null) return;
            double eyeX = Mth.lerp(partialTicks, player.xo, player.getX());
            double eyeY = Mth.lerp(partialTicks, player.yo, player.getY()) + player.getEyeHeight();
            double eyeZ = Mth.lerp(partialTicks, player.zo, player.getZ());
            Vec3 eyePos = new Vec3(eyeX, eyeY, eyeZ);

            double distance = camera.getDistance();
            this.setPosition(eyePos.add(-dirX * distance, -dirY * distance, -dirZ * distance));
            this.setRotation(yaw, pitch);
        }
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void mc_chameleon$fixCullFrustrum(DeltaTracker deltaTracker, CallbackInfo ci) {
        ChameleonOrbitCamera camera = ChameleonOrbitCamera.getInstance();
        if (camera == null || !camera.isActive()) return;

        setRotation(camera.getYaw(), camera.getPitch());

        Matrix4f viewRot = getViewRotationMatrix(new Matrix4f());
        Matrix4f proj = createProjectionMatrixForCulling();
        Vec3 pos = ((Camera)(Object)this).position();
        prepareCullFrustum(viewRot, proj, pos);
    }
}