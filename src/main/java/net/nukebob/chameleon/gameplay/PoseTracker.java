package net.nukebob.chameleon.gameplay;

import net.minecraft.util.Mth;

public class PoseTracker {
    private Poses currentPose;
    private Poses targetPose;
    private float transitionProgress;
    private static final float TRANSITION_SPEED = 0.25f;

    public PoseTracker() {
        currentPose = null;
        targetPose = null;
        transitionProgress = 0.0f;
    }

    public void setTargetPose(Poses pose) {
        if (targetPose != pose) {
            currentPose = targetPose;
            targetPose = pose;
            transitionProgress = 0.0f;
        }
    }

    public Poses getTargetPos() {
        return targetPose;
    }

    public void update(float delta) {
        if (transitionProgress < 1.0f) {
            transitionProgress = Mth.clamp(transitionProgress + (TRANSITION_SPEED * delta), 0,1);
        }
    }

    public float getProgress() {
        if (currentPose==null) return Mth.clamp(transitionProgress,0,1);
        if (targetPose==null) return Mth.clamp(1-transitionProgress,0,1);

        float x = transitionProgress;
        float x2 = x*x;
        return 1.0f + x2 * (-16.0f + x * (32.0f - 16.0f * x));
    }

    public Poses getPose() {
        if (currentPose==null) return targetPose;
        if (targetPose==null) return currentPose;

        if (transitionProgress>0.5) return targetPose;
        return currentPose;
    }

    public Poses getTargetPose() {
        return targetPose;
    }
}
