package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.nukebob.chameleon.gameplay.TeamControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "doAddParticle", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$disableParticles(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShowParticles, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
        if (Minecraft.getInstance().level==null) return;
        if (!TeamControl.getChameleonsTeam(Minecraft.getInstance().level.getScoreboard()).canSeeFriendlyInvisibles()) ci.cancel();
    }
}
