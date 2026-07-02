package net.nukebob.chameleon.mixin;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.nukebob.chameleon.accessor.PoseTrackerAccessor;
import net.nukebob.chameleon.gameplay.PoseTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements PoseTrackerAccessor {@Unique
    private PoseTracker mc_chameleon$poseTracker;

    @Override
    public PoseTracker mc_chameleon$getPoseTracker() {
        return this.mc_chameleon$poseTracker;
    }

    @Override
    public void mc_chameleon$setPoseTracker(PoseTracker tracker) {
        this.mc_chameleon$poseTracker = tracker;
    }
}
