package net.nukebob.chameleon.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$disableBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (ChameleonOrbitCamera.getInstance().isActive()) {
            cir.setReturnValue(false);
        }
    }
}
