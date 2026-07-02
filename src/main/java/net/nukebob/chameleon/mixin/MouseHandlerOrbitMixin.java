package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerOrbitMixin {

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$suppressTurnWhileOrbiting(double mousea, CallbackInfo ci) {
        if (ChameleonOrbitCamera.getInstance().isActive()) {
                double ss = this.minecraft.options.sensitivity().get() * 0.6F + 0.2F;
                double sensitivityMod = ss * ss * ss;
                double sens = sensitivityMod * 8.0;

                double dx = this.accumulatedDX * sens * 0.8;
                double dy = this.accumulatedDY * sens * 0.8;

                ChameleonOrbitCamera.getInstance().rotate((float) dx, (float) dy);

            ci.cancel();
        }
    }
}
