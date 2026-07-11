package net.nukebob.chameleon.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "isCrouching", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$stopCrouchingLocal(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "onGameModeChanged", at = @At("TAIL"))
    private void mc_chameleon$lockCamera(GameType gameType, CallbackInfo ci) {
        if (gameType.equals(GameType.SPECTATOR)) {
            ChameleonOrbitCamera.getInstance().setActive(true);
        }
    }
}
