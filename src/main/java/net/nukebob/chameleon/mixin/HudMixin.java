package net.nukebob.chameleon.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.render.ChameleonHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$hudForPlayer(CallbackInfoReturnable<Player> cir) {
        if (ChameleonOrbitCamera.getInstance().isActive()) {
            cir.setReturnValue(Minecraft.getInstance().player);
        }
    }

    @Inject(method = "extractCameraOverlays", at = @At("TAIL"))
    private void mc_chameleon$addChameleonHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ChameleonHud.render(graphics, deltaTracker);
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$noHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().player!=null&&!Minecraft.getInstance().player.isCreative()) ci.cancel();
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$noEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().player!=null&&!Minecraft.getInstance().player.isCreative()) ci.cancel();
    }

    @Inject(method = "extractBossOverlay", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$noBossBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().player!=null&&!Minecraft.getInstance().player.isCreative()) ci.cancel();
    }

    @Inject(method = "extractSleepOverlay", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$noSleepOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().player!=null&&!Minecraft.getInstance().player.isCreative()) ci.cancel();
    }

    @Inject(method = "extractDemoOverlay", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$noDemoOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().player!=null&&!Minecraft.getInstance().player.isCreative()) ci.cancel();
    }
}
