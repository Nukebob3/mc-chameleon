package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.nukebob.chameleon.gameplay.TeamControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.AnimationState.class)
public class SpriteContentsAnimationStateMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$disableBlockAnimation(CallbackInfo ci) {
        if (Minecraft.getInstance().level==null) return;
        if (TeamControl.getChameleonsTeam(Minecraft.getInstance().level.getScoreboard())==null) return;
        if (!TeamControl.getChameleonsTeam(Minecraft.getInstance().level.getScoreboard()).canSeeFriendlyInvisibles()) ci.cancel();
    }
}
