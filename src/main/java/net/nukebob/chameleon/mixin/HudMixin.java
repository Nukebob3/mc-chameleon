package net.nukebob.chameleon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.ChatComponent;
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

    @WrapOperation(method = "extractChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"))
    private void mc_chameleon$moveChatToTopRight(ChatComponent instance, GuiGraphicsExtractor graphics, Font font, int ticks, int mouseX, int mouseY, ChatComponent.DisplayMode displayMode, boolean changeCursorOnInsertions, Operation<Void> original) {
        //TODO move chat
        original.call(instance, graphics, font, ticks, mouseX, mouseY, displayMode, changeCursorOnInsertions);
    }
}
