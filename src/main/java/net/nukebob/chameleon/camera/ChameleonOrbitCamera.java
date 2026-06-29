package net.nukebob.chameleon.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ChameleonOrbitCamera {
    private static boolean active = false;
    private static float yaw = 0f;
    private static float pitch = 0f;

    private static float distance = 2.0f;

    private static double lastX, lastY, lastZ;
    private static boolean hasLastPosition = false;

    public static void preventDisable(Player player) {
        lastX = player.getX();
        lastY = player.getY();
        lastZ = player.getZ();
        hasLastPosition = true;
    }

    public static void checkAutoDisable(Player player) {
        if (!active||player==null) return;

        boolean keyHeld = Minecraft.getInstance().options.keyUp.isDown()
                || Minecraft.getInstance().options.keyDown.isDown()
                || Minecraft.getInstance().options.keyLeft.isDown()
                || Minecraft.getInstance().options.keyRight.isDown()
                || Minecraft.getInstance().options.keyJump.isDown();

        boolean positionChanged = hasLastPosition && (
                Math.abs(player.getX() - lastX) > 1e-4
                        || Math.abs(player.getY() - lastY) > 1e-4
                        || Math.abs(player.getZ() - lastZ) > 1e-4
        );

        lastX = player.getX();
        lastY = player.getY();
        lastZ = player.getZ();
        hasLastPosition = true;

        if (keyHeld || positionChanged) {
            active = false;
            player.setYRot(yaw);
            player.setXRot(pitch);

            player.yRotO = yaw;
            player.xRotO = pitch;

            player.setYHeadRot(yaw);
            player.yHeadRotO = yaw;
        }
    }

    public static Vec3 getPosition() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Vec3.ZERO;
        }

        Vec3 pivot = minecraft.player.getEyePosition(1.0f);

        float yawRadians = yaw * (float) (Math.PI / 180.0);
        float pitchRadians = pitch * (float) (Math.PI / 180.0);

        double cosPitch = Mth.cos(pitchRadians);

        double offsetX = -Mth.sin(yawRadians) * cosPitch * distance;
        double offsetY = -Mth.sin(pitchRadians) * distance;
        double offsetZ = Mth.cos(yawRadians) * cosPitch * distance;

        return new Vec3(
                pivot.x - offsetX,
                pivot.y - offsetY,
                pivot.z - offsetZ
        );
    }

    public static float getDistance() { return distance; }
    public static void setDistance(float value) { distance = Math.min(3f,Math.max(1f, value)); }

    public static boolean isActive() { return active; }
    public static void setActive(boolean value) { active = value; }

    public static float getYaw() { return yaw; }
    public static float getPitch() { return pitch; }

    public static void rotate(float dYaw, float dPitch) {
        yaw += dYaw;
        pitch = Math.max(-90f, Math.min(90f, pitch + dPitch));
    }

    public static void syncToEntityLookDirection(float entityYaw, float entityPitch) {
        yaw = entityYaw;
        pitch = entityPitch;
    }
}