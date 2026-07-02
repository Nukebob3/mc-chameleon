package net.nukebob.chameleon.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.nukebob.chameleon.keybind.SettingsLock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBindsList.KeyEntry.class)
public class KeyBindsListKeyEntryMixin {
    @Shadow
    @Final
    private KeyMapping key;

    @Shadow
    @Final
    private Button changeButton;

    @Shadow
    @Final
    private Button resetButton;

    @ModifyExpressionValue(
            method = "refreshEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;same(Lnet/minecraft/client/KeyMapping;)Z"
            )
    )
    private boolean mc_chameleon$noIssues(boolean original) {
        return false;
    }

    @Inject(method = "refreshEntry", at = @At("TAIL"))
    private void mc_chameleon$lockKey(CallbackInfo ci) {
        if (SettingsLock.disabledKeybinds.contains(key.getName().replaceFirst("key.",""))) {
            this.changeButton.active = false;
            this.resetButton.active = false;
            this.changeButton.setTooltip(Tooltip.create(Component.translatable("lock.mc-chameleon.reason").withColor(TextColor.RED)));
        }
    }
}
