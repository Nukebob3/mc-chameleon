package net.nukebob.chameleon.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

public class GunBeamRenderer {
    private final Vec3 startPos;
    private final Vec3 endPos;
    private float age;

    public GunBeamRenderer(Vec3 startPos, Vec3 endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
        age = 0;
    }

    public void age(float elapsed) {
        age+=elapsed;
    }

    public float getAge() {
        return age;
    }

    public void render(final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camPos = camera.pos;

        poseStack.pushPose();
        poseStack.translate(startPos.x - camPos.x, startPos.y - camPos.y, startPos.z - camPos.z);

        Vec3 beamVector = endPos.subtract(startPos);

        renderBeam(poseStack, submitNodeCollector, beamVector, age);

        poseStack.popPose();
    }

    private void renderBeam(
            final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, Vec3 beamVector, final float age
    ) {
        float top = (float)(beamVector.length() + 1.0);
        float beamLength = (float) beamVector.length();
        beamVector = beamVector.normalize();
        float xRot = (float)Math.acos(beamVector.y);
        float yRot = (float) (Math.PI / 2) - (float)Math.atan2(beamVector.z, beamVector.x);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot * (180.0F / (float)Math.PI)));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot * (180.0F / (float)Math.PI)));

        float size = 0.025F;
        submitNodeCollector.submitCustomGeometry(poseStack, ChameleonRenderTypes.gunShot(), (pose, buffer) -> {

            vertex(buffer, pose, -size, top,  size, 0.0F,  beamLength, age);
            vertex(buffer, pose, -size, 0.0F,  size, 0.0F,  0.0F, age);
            vertex(buffer, pose,  size, 0.0F,  size, 0.25F, 0.0F, age);
            vertex(buffer, pose,  size, top,  size, 0.25F, beamLength, age);

            vertex(buffer, pose,  size, top,  size, 0.25F, beamLength, age);
            vertex(buffer, pose,  size, 0.0F,  size, 0.25F, 0.0F, age);
            vertex(buffer, pose,  size, 0.0F, -size, 0.5F,  0.0F, age);
            vertex(buffer, pose,  size, top, -size, 0.5F,  beamLength, age);

            vertex(buffer, pose,  size, top, -size, 0.5F,  beamLength, age);
            vertex(buffer, pose,  size, 0.0F, -size, 0.5F,  0.0F, age);
            vertex(buffer, pose, -size, 0.0F, -size, 0.75F, 0.0F, age);
            vertex(buffer, pose, -size, top, -size, 0.75F, beamLength, age);

            vertex(buffer, pose, -size, top, -size, 0.75F, beamLength, age);
            vertex(buffer, pose, -size, 0.0F, -size, 0.75F, 0.0F, age);
            vertex(buffer, pose, -size, 0.0F,  size, 1.0F,  0.0F, age);
            vertex(buffer, pose, -size, top,  size, 1.0F,  beamLength, age);
        });
    }

    private static void vertex(
            final VertexConsumer builder,
            final PoseStack.Pose pose,
            final float x,
            final float y,
            final float z,
            final float u,
            final float v,
            float age
    ) {
        builder.addVertex(pose, x, y, z)
                .setColor(age/10f, 1f, 1f, 1f)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0f, 1.0F, 0.0F);
    }
}