package net.nukebob.chameleon;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.networking.Networking;
import net.nukebob.chameleon.render.ChameleonOutputTargets;
import net.nukebob.chameleon.screen.PaintScreen;
import net.nukebob.chameleon.util.UvPicker;
import org.lwjgl.glfw.GLFW;

public class MCChameleonClient implements ClientModInitializer {
    public static int[] localSkinCache = new int[1504];
    public static int mouseX = 0;
    public static int mouseY = 0;
    public static int uvCol = 0;

    public static int selectedColour=0xFFFFFFFF;
    public static float brushSize=1;

    @Override
    public void onInitializeClient() {
        Networking.registerClientReceivers();

        LevelRenderEvents.BEFORE_GIZMOS.register((ctx) -> {
            ChameleonOutputTargets.clearUvPickerTarget();
        });

        LevelRenderEvents.END_MAIN.register((ctx) -> {
            UvPicker.pickPixel(ChameleonOutputTargets.UV_PICKER_TARGET.getRenderTarget(), mouseX, mouseY, (result) -> {
                uvCol = result;
            });
        });

        KeyMapping.Category CATEGORY = KeyMapping.Category.register(
                MCChameleon.id("hider")
        );

        KeyMapping sendToChatKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.mc-chameleon.open_paint",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_Y,
                        CATEGORY
                ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sendToChatKey.consumeClick()) {
                if (client.player != null) {
                    Minecraft.getInstance().setScreenAndShow(new PaintScreen());
                }
            }
            if (!(client.gui.screen() instanceof PaintScreen)) ChameleonOrbitCamera.checkAutoDisable(client.player);
            else if (client.player!=null) ChameleonOrbitCamera.preventDisable(client.player);
        });
    }
}
