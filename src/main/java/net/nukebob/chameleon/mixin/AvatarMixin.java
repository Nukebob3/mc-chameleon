package net.nukebob.chameleon.mixin;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.gameplay.TeamControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(Avatar.class)
public class AvatarMixin {
    @Inject(method = "getDefaultDimensions", at = @At("RETURN"), cancellable = true)
    private void mc_chameleon$changeHitboxForCustomPose(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        Avatar self = (Avatar) (Object) this;

        if (!TeamControl.isChameleon(self.getTeam())) return;

        Map<UUID, PoseTracker> poses = self.level().isClientSide() ? MCChameleonClient.POSES : MCChameleon.POSES;
        PoseTracker tracker = poses.get(self.getUUID());
        if (tracker != null && Poses.FLAT.equals(tracker.getTargetPose())) {
            cir.setReturnValue(EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F));
        }
    }
}
