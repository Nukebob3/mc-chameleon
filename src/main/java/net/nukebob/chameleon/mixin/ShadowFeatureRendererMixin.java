package net.nukebob.chameleon.mixin;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.ShadowFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShadowFeatureRenderer.class)
public class ShadowFeatureRendererMixin {
    @Inject(method = "buildGroup", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$cancelAll(FeatureFrameContext context, List<ShadowFeatureRenderer.Submit> submits, CallbackInfo ci) {
        ci.cancel();
    }
}
