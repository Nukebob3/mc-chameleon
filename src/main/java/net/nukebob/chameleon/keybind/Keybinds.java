package net.nukebob.chameleon.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.nukebob.chameleon.MCChameleon;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            MCChameleon.id("hider")
    );
    public static final KeyMapping openPaintScreen = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mc-chameleon.open_paint",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F,
                    CATEGORY
            ));
    public static final KeyMapping openPoseScreen = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mc-chameleon.pose",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_R,
                    CATEGORY
            ));
    public static final KeyMapping whistle = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mc-chameleon.whistle",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_1,
                    CATEGORY
            ));
    public static final KeyMapping cameraLock = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mc-chameleon.cameraLock",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_5,
                    CATEGORY
            ));
    public static final KeyMapping freeCam = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mc-chameleon.free_cam",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_4,
                    CATEGORY
            ));

    public static void init() {}
}
