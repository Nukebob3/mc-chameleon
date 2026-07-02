package net.nukebob.chameleon.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.nukebob.chameleon.keybind.SettingsLock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public class OptionInstanceMixin {
    @Shadow
    @Final
    private Component caption;

    @Inject(method = "createButton(Lnet/minecraft/client/Options;IIILnet/minecraft/client/OptionInstance$ValueUpdateListener;)Lnet/minecraft/client/gui/components/AbstractWidget;", at = @At("RETURN"))
    private void mc_chameleon$disableLockedSettingButton(Options options, int x, int y, int width, OptionInstance.ValueUpdateListener<?> onValueChanged, CallbackInfoReturnable<AbstractWidget> cir) {
        if (caption.getString().contains("cape")) System.out.println(caption + " | " + caption.getString());
        if (caption.getContents() instanceof TranslatableContents tc && SettingsLock.lockedSettings.contains(tc.getKey().replaceFirst("options.",""))) {
            cir.getReturnValue().active = false;
            cir.getReturnValue().setTooltip(Tooltip.create(Component.translatable("lock.mc-chameleon.reason").withColor(TextColor.RED)));
        }
    }
}
