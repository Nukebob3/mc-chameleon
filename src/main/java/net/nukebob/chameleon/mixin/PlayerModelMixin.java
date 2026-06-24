package net.nukebob.chameleon.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.nukebob.chameleon.accessors.ArmWidthSetter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerModel.class)
public class PlayerModelMixin extends HumanoidModel<AvatarRenderState> implements ArmWidthSetter {
    public PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Override
    public void mc_chameleon$setArmWidth(boolean slim) {
        float scale = slim ? 0.75F : 1.0F;
        float leftShift = slim ? -0.25F : 0.0F;
        float rightShift = slim ? -0.5F : 0.0F;

        this.leftArm.xScale = scale;
        this.leftArm.x = 5.0F + leftShift;
        this.leftArm.getChild("left_sleeve").xScale = scale;
        this.leftArm.getChild("left_sleeve").x = leftShift;

        this.rightArm.xScale = scale;
        this.rightArm.x = -5.0F + rightShift;
        this.rightArm.getChild("right_sleeve").xScale = scale;
        this.rightArm.getChild("right_sleeve").x = rightShift;
    }
}
