package net.nukebob.chameleon.mixin;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.nukebob.chameleon.keybind.SettingsLock;
import net.nukebob.chameleon.texture.ChameleonTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(method = "load", at = @At("RETURN"))
    private void mc_chameleon$enforceLocked(CallbackInfo ci) {
        SettingsLock.applyLockedSettings((Options)(Object)this);
    }

    @Inject(method = "getCameraType", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$overridePerspective(CallbackInfoReturnable<CameraType> cir) {
        if (Minecraft.getInstance().player==null) return;
        cir.setReturnValue(ChameleonTexture.skins.containsKey(Minecraft.getInstance().player.getUUID())?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);
    }
}
