package net.nukebob.chameleon.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.nukebob.chameleon.Render.ChameleonRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    /*
        protected @Nullable RenderType getRenderType(final S state, final boolean isBodyVisible, final boolean forceTransparent, final boolean appearGlowing) {
        Identifier texture = this.getTextureLocation(state);
        if (forceTransparent) {
            return RenderTypes.entityTranslucentCullItemTarget(texture);
        } else if (isBodyVisible) {
            return this.model.renderType(texture);
        } else {
            return appearGlowing ? RenderTypes.outline(texture) : null;
        }
    }
     */
    @Inject(
            method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyPlayerRenderType(LivingEntityRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing, CallbackInfoReturnable<RenderType> cir) {
        if (true) {
            RenderType originalType = cir.getReturnValue();

            if (originalType != null) {
                if (state instanceof AvatarRenderState state1) {
                    Identifier texture = state1.skin.body().texturePath();
                    cir.setReturnValue(ChameleonRenderTypes.entityGray(texture));
                    //cir.setReturnValue(RenderTypes.endCrystalBeam(texture));
                }
            }
        }
    }
}
