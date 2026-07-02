package net.nukebob.chameleon.mixin;

import net.minecraft.client.ScrollWheelHandler;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScrollWheelHandler.class)
public class ScrollWheelHandlerMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$zoom(double scaledXScrollOffset, double scaledYScrollOffset, CallbackInfoReturnable<Vector2i> cir) {
        ChameleonOrbitCamera.getInstance().setDistance((float) (ChameleonOrbitCamera.getInstance().getDistance()-0.25*scaledYScrollOffset));
        if (ChameleonOrbitCamera.getInstance().isActive()) cir.setReturnValue(new Vector2i());
    }
}
