package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public class HudMixin {
    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void hudForPlayer(CallbackInfoReturnable<Player> cir) {
        if (ChameleonOrbitCamera.getInstance().isActive()) {
            cir.setReturnValue(Minecraft.getInstance().player);
        }
    }
}
