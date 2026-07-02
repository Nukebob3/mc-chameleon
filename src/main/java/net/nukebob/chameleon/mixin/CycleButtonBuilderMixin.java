package net.nukebob.chameleon.mixin;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.nukebob.chameleon.keybind.SettingsLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CycleButton.Builder.class)
public class CycleButtonBuilderMixin<T> {
    @Inject(method = "create(IIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/CycleButton$OnValueChange;)Lnet/minecraft/client/gui/components/CycleButton;",
    at = @At("RETURN"))
    private void mc_chameleon$disableLockedCycleButton(int x, int y, int width, int height, Component name, CycleButton.OnValueChange<T> valueChangeListener, CallbackInfoReturnable<CycleButton<T>> cir) {
        if (name.getContents() instanceof TranslatableContents tc && SettingsLock.lockedSettings.contains(tc.getKey().replaceFirst("options.",""))) {
            cir.getReturnValue().active = false;
            cir.getReturnValue().setTooltip(Tooltip.create(Component.translatable("lock.mc-chameleon.reason").withColor(TextColor.RED)));
        }
    }
}
