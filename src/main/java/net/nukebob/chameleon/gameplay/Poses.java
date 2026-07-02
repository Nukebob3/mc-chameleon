package net.nukebob.chameleon.gameplay;

import net.minecraft.core.Vec3i;

public enum Poses {
    T_POSE(Vec3i.ZERO,Vec3i.ZERO,Vec3i.ZERO,Vec3i.ZERO,Vec3i.ZERO,new Vec3i(0,0,-90),new Vec3i(0,0,90),Vec3i.ZERO,Vec3i.ZERO),
    ARCH(Vec3i.ZERO,Vec3i.ZERO,new Vec3i(28,0,0),new Vec3i(40,0,0),Vec3i.ZERO,new Vec3i(-140,0,0),new Vec3i(-140,0,0),Vec3i.ZERO,Vec3i.ZERO),
    FLAT(new Vec3i(90,0,0), new Vec3i(0,22,0), Vec3i.ZERO, Vec3i.ZERO, Vec3i.ZERO, new Vec3i(0,0, -110), new Vec3i(0,0,110), new Vec3i(0,0,-50), new Vec3i(0,0,50));

    private final Vec3i rootRot, rootOffset, waist, head, torso, leftArm, rightArm, leftLeg, rightLeg;
    Poses(Vec3i rootRot, Vec3i rootOffset, Vec3i waist, Vec3i head, Vec3i torso, Vec3i leftArm, Vec3i rightArm, Vec3i leftLeg, Vec3i rightLeg) {
        this.rootRot = rootRot;
        this.rootOffset = rootOffset;
        this.waist = waist;
        this.head = head;
        this.torso = torso;
        this.leftArm = leftArm;
        this.rightArm = rightArm;
        this.leftLeg = leftLeg;
        this.rightLeg = rightLeg;
    }

    public Vec3i getRootRot() {
        return rootRot;
    }

    public Vec3i getRootOffset() {
        return rootOffset;
    }

    public Vec3i getWaist() {
        return waist;
    }

    public Vec3i getHead() {
        return head;
    }

    public Vec3i getTorso() {
        return torso;
    }

    public Vec3i getLeftArm() {
        return leftArm;
    }

    public Vec3i getRightArm() {
        return rightArm;
    }

    public Vec3i getLeftLeg() {
        return leftLeg;
    }

    public Vec3i getRightLeg() {
        return rightLeg;
    }
}
