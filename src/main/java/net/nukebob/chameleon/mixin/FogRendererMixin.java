package net.nukebob.chameleon.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FogRenderer.class, priority = 1001)
public class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$removeFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        FogData data = cir.getReturnValue();
        if (camera.getFluidInCamera().equals(FogType.WATER)||camera.getFluidInCamera().equals(FogType.LAVA)||camera.getFluidInCamera().equals(FogType.POWDER_SNOW)||camera.getFluidInCamera().equals(FogType.POWDER_SNOW)) {
            data.cloudEnd = 128f;
            data.skyEnd = 128f;
            data.environmentalEnd = 128f;
            cir.setReturnValue(data);
        }
    }
}
