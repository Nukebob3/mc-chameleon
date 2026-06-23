package net.nukebob.chameleon.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
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
    private static PartDefinition redirectHeadCreation(PartDefinition root, String name, CubeListBuilder builder, PartPose pose) {
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
    private void cancelIdleBobbing(ModelPart part, float ageInTicks, float sign) {
    }
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL")
    )
    private void lockBodyRotationToHead(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;

        model.root().yRot = model.head.yRot;
        model.head.yRot=0;
    }
}
