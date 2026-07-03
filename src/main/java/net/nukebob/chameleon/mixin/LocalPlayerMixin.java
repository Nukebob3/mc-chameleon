package net.nukebob.chameleon.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "isCrouching", at = @At("HEAD"), cancellable = true)
    private void stopCrouchingLocal(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
