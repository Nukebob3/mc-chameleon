package net.nukebob.chameleon.mixin;

import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.nukebob.chameleon.keybind.SettingsLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GraphicsPreset.class)
public class GraphicsPresetMixin {
    @Inject(method = "apply", at = @At("TAIL"))
    private void mc_chameleon$lockedSettings(Minecraft minecraft, CallbackInfo ci) {
        SettingsLock.applyLockedSettings(minecraft.options);
    }
}
