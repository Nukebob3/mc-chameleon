package net.nukebob.chameleon.mixin;

import net.minecraft.client.KeyMapping;
import net.nukebob.chameleon.keybind.SettingsLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$stopKeybindDown(CallbackInfoReturnable<Boolean> cir) {
        KeyMapping key = (KeyMapping) (Object) this;
        if (SettingsLock.disabledKeybinds.contains(key.getName().replaceFirst("key.",""))) cir.setReturnValue(false);
    }

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$stopKeybindClick(CallbackInfoReturnable<Boolean> cir) {
        KeyMapping key = (KeyMapping) (Object) this;
        if (SettingsLock.disabledKeybinds.contains(key.getName().replaceFirst("key.",""))) cir.setReturnValue(false);
    }
}
