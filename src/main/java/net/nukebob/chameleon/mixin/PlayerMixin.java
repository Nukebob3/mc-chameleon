package net.nukebob.chameleon.mixin;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.nukebob.chameleon.gameplay.TeamControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "getDesiredPose", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$noCrouch(CallbackInfoReturnable<Pose> cir) {
        if (cir.getReturnValue() != Pose.CROUCHING) return;

        Player self = (Player) (Object) this;
        cir.setReturnValue(TeamControl.isChameleon(self.getTeam()) || !self.onGround()?
                Pose.STANDING:Pose.SWIMMING);
    }
}
