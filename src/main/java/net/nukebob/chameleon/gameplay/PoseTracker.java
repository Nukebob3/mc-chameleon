package net.nukebob.chameleon.gameplay;

import net.minecraft.util.Mth;

public class PoseTracker {
    private static Poses currentPose = null;
    private static Poses targetPose = null;
    private static float transitionProgress = 0.0f;
    private static final float TRANSITION_SPEED = 0.025f;

    public static void setTargetPose(Poses pose) {
        if (targetPose != pose) {
            currentPose = targetPose;
            targetPose = pose;
            transitionProgress = 0.0f;
        }
    }

    public static void update(float delta) {
        if (transitionProgress < 1.0f) {
            transitionProgress = Mth.clamp(transitionProgress + (TRANSITION_SPEED * delta), 0,1);
        }
    }

    public static float getProgress() {
        if (currentPose==null) return Mth.clamp(transitionProgress,0,1);
        if (targetPose==null) return Mth.clamp(1-transitionProgress,0,1);

        if (transitionProgress>0.5) return Mth.clamp(2f*transitionProgress-1f,0,1);
        return Mth.clamp(1f-2f*transitionProgress,0,1);
    }

    public static Poses getPose() {
        if (currentPose==null) return targetPose;
        if (targetPose==null) return currentPose;

        if (transitionProgress>0.5) return targetPose;
        return currentPose;
    }
}
