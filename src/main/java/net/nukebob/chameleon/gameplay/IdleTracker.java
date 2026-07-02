package net.nukebob.chameleon.gameplay;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class IdleTracker {
    public static double lastX, lastY, lastZ;
    public static boolean hasLastPosition = false;

    public static void preventDisable(Player player) {
        IdleTracker.lastX = player.getX();
        IdleTracker.lastY = player.getY();
        IdleTracker.lastZ = player.getZ();
        IdleTracker.hasLastPosition = true;
    }

    public static boolean checkAutoDisable(Player player) {
        if (player==null) return false;

        boolean keyHeld = Minecraft.getInstance().options.keyUp.isDown()
                || Minecraft.getInstance().options.keyDown.isDown()
                || Minecraft.getInstance().options.keyLeft.isDown()
                || Minecraft.getInstance().options.keyRight.isDown()
                || Minecraft.getInstance().options.keyJump.isDown();

        boolean positionChanged = IdleTracker.hasLastPosition && (
                Math.abs(player.getX() - IdleTracker.lastX) > 1e-4
                        || Math.abs(player.getY() - IdleTracker.lastY) > 1e-4
                        || Math.abs(player.getZ() - IdleTracker.lastZ) > 1e-4
        );

        IdleTracker.lastX = player.getX();
        IdleTracker.lastY = player.getY();
        IdleTracker.lastZ = player.getZ();
        IdleTracker.hasLastPosition = true;

        return keyHeld || positionChanged;
    }
}
