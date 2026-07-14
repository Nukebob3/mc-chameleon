package net.nukebob.chameleon.mixin;

import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Shadow
    private int selected;

    @Inject(method = "setSelectedSlot", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$setToFirstSlot(int selected, CallbackInfo ci) {
        this.selected = 0;
        ci.cancel();
    }
}
