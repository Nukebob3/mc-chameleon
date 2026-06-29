package net.nukebob.chameleon.mixin;

import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.nukebob.chameleon.setter.ArmWidthSetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSkin.class)
public abstract class PlayerSkinMixin implements ArmWidthSetter {
    @Inject(method = "model", at = @At("RETURN"), cancellable = true)
    public void mc_chameleon$overrideModelType(CallbackInfoReturnable<PlayerModelType> cir) {
        if (this.mc_chameleon$isCanvas()) {
            cir.setReturnValue(PlayerModelType.WIDE);
        }
    }
}
