package net.nukebob.chameleon.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.nukebob.chameleon.texture.ChameleonTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$forceWideSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer)(Object)this;
        if (!ChameleonTexture.skins.containsKey(self.getUUID())) return;

        PlayerSkin original = cir.getReturnValue();
        cir.setReturnValue(new PlayerSkin(original.body(), null, null, PlayerModelType.WIDE, original.secure()));
    }
}
