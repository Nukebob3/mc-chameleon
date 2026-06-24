package net.nukebob.chameleon.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.nukebob.chameleon.Render.ChameleonRenderTypes;
import net.nukebob.chameleon.accessors.ArmWidthSetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<M extends EntityModel<?>> {
    @Shadow
    protected M model;

    @Inject(
            method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyPlayerRenderType(LivingEntityRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing, CallbackInfoReturnable<RenderType> cir) {
        RenderType originalType = cir.getReturnValue();

        if (this.model instanceof ArmWidthSetter playerModel) {
            playerModel.mc_chameleon$setArmWidth(true);
        }

        if (originalType != null) {
            if (state instanceof AvatarRenderState state1) {
                if (state.scale!=1) return;

                Identifier texture = state1.skin.body().texturePath();
                cir.setReturnValue(ChameleonRenderTypes.entityGray(texture));
            }
        }
    }
}
