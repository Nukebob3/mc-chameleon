package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Redirect(
            method = "createMesh",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/builders/PartDefinition;addOrReplaceChild(Ljava/lang/String;Lnet/minecraft/client/model/geom/builders/CubeListBuilder;Lnet/minecraft/client/model/geom/PartPose;)Lnet/minecraft/client/model/geom/builders/PartDefinition;"
            )
    )
    private static PartDefinition mc_chameleon$redirectHeadCreation(PartDefinition root, String name, CubeListBuilder builder, PartPose pose) {
        if ("head".equals(name)) {
            return root.addOrReplaceChild(
                    name,
                    CubeListBuilder.create().texOffs(4, 4).addBox(-4.0F, -8.0F, -2.0F, 8.0F, 8.0F, 4.0F, CubeDeformation.NONE),
                    pose
            );
        } else if ("hat".equals(name)) {
            return root.addOrReplaceChild(
                    name,
                    CubeListBuilder.create().texOffs(36, 4).addBox(-4.0F, -8.0F, -2.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.5f)),
                    pose
            );
        }

        return root.addOrReplaceChild(name, builder, pose);
    }
    @Redirect(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V"
            )
    )
    private void mc_chameleon$cancelIdleBobbing(ModelPart part, float ageInTicks, float sign) {
    }
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL")
    )
    private void mc_chameleon$lockBodyRotationToHead(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;

        model.root().yRot = model.head.yRot;
        model.head.yRot=0;

        PoseTracker.update(Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
        Poses pose = PoseTracker.getPose();
        float factor = PoseTracker.getProgress();
        if (pose!=null) pose(pose, model, factor);
    }

    @Unique
    private void pose(Poses pose, HumanoidModel<?> model, float factor) {
        //root
        model.root().xRot=(float) Math.toRadians(pose.getRootRot().getX())*factor + model.root().xRot*(1-factor);
        model.root().yRot+=(float) Math.toRadians(pose.getRootRot().getY())*factor;
        model.root().zRot+=(float) Math.toRadians(pose.getRootRot().getZ())*factor;
        model.root().x-=pose.getRootOffset().getX()*factor;
        model.root().y+=pose.getRootOffset().getY()*factor;
        model.root().z-=pose.getRootOffset().getZ()*factor;
        //waist
        model.head.xRot=(float) Math.toRadians(pose.getWaist().getX())*factor + model.head.xRot*(1-factor);
        model.head.yRot=(float) Math.toRadians(pose.getWaist().getY())*factor + model.head.yRot*(1-factor);
        model.head.zRot=(float) Math.toRadians(pose.getWaist().getZ())*factor + model.head.zRot*(1-factor);
        model.body.xRot=(float) Math.toRadians(pose.getWaist().getX())*factor + model.body.xRot*(1-factor);
        model.body.yRot=(float) Math.toRadians(pose.getWaist().getY())*factor + model.body.yRot*(1-factor);
        model.body.zRot=(float) Math.toRadians(pose.getWaist().getZ())*factor + model.body.zRot*(1-factor);
        if (pose.equals(Poses.ARCH)) {
            model.leftLeg.z+=5*factor;
            model.rightLeg.z+=5*factor;

            model.head.y+=2*factor;
            model.body.y+=2*factor;
            model.leftArm.y+=2*factor;
            model.rightArm.y+=2*factor;

            model.head.y+=1*factor;
            model.leftArm.y-=1*factor;
            model.rightArm.y-=1*factor;
        }
        model.leftArm.xRot=(float) Math.toRadians(pose.getWaist().getX())*factor + model.leftArm.xRot*(1-factor);
        model.leftArm.yRot=(float) Math.toRadians(pose.getWaist().getY())*factor + model.leftArm.yRot*(1-factor);
        model.leftArm.zRot=(float) Math.toRadians(pose.getWaist().getZ())*factor + model.leftArm.zRot*(1-factor);
        model.rightArm.xRot=(float) Math.toRadians(pose.getWaist().getX())*factor + model.rightArm.xRot*(1-factor);
        model.rightArm.yRot=(float) Math.toRadians(pose.getWaist().getY())*factor + model.rightArm.yRot*(1-factor);
        model.rightArm.zRot=(float) Math.toRadians(pose.getWaist().getZ())*factor + model.rightArm.zRot*(1-factor);
        //others
        model.head.xRot+=(float) Math.toRadians(pose.getHead().getX())*factor;
        model.head.yRot+=(float) Math.toRadians(pose.getHead().getY())*factor;
        model.head.zRot+=(float) Math.toRadians(pose.getHead().getZ())*factor;
        model.body.xRot+=(float) Math.toRadians(pose.getTorso().getX())*factor;
        model.body.yRot+=(float) Math.toRadians(pose.getTorso().getY())*factor;
        model.body.zRot+=(float) Math.toRadians(pose.getTorso().getZ())*factor;
        if (pose.equals(Poses.T_POSE)) {
            model.leftArm.x+=1*factor;
            model.rightArm.x-=1*factor;
        }
        model.leftArm.xRot+=(float) Math.toRadians(pose.getLeftArm().getX())*factor;
        model.leftArm.yRot+=(float) Math.toRadians(pose.getLeftArm().getY())*factor;
        model.leftArm.zRot+=(float) Math.toRadians(pose.getLeftArm().getZ())*factor;
        model.rightArm.xRot+=(float) Math.toRadians(pose.getRightArm().getX())*factor;
        model.rightArm.yRot+=(float) Math.toRadians(pose.getRightArm().getY())*factor;
        model.rightArm.zRot+=(float) Math.toRadians(pose.getRightArm().getZ())*factor;
        model.leftLeg.xRot=(float) Math.toRadians(pose.getLeftLeg().getX())*factor + model.leftLeg.xRot*(1-factor);
        model.leftLeg.yRot=(float) Math.toRadians(pose.getLeftLeg().getY())*factor + model.leftLeg.yRot*(1-factor);
        model.leftLeg.zRot=(float) Math.toRadians(pose.getLeftLeg().getZ())*factor + model.leftLeg.zRot*(1-factor);
        model.rightLeg.xRot=(float) Math.toRadians(pose.getRightLeg().getX())*factor + model.rightLeg.xRot*(1-factor);
        model.rightLeg.yRot=(float) Math.toRadians(pose.getRightLeg().getY())*factor + model.rightLeg.yRot*(1-factor);
        model.rightLeg.zRot=(float) Math.toRadians(pose.getRightLeg().getZ())*factor + model.rightLeg.zRot*(1-factor);
    }
}
