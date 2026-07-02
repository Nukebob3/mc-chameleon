package net.nukebob.chameleon;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.gameplay.IdleTracker;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.networking.Networking;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.render.ChameleonOutputTargets;
import net.nukebob.chameleon.screen.PaintScreen;
import net.nukebob.chameleon.sound.ChameleonSounds;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.util.UvPicker;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MCChameleonClient implements ClientModInitializer {
    public static int[] localSkinCache = new int[1504];
    public static int mouseX = 0;
    public static int mouseY = 0;
    public static int uvCol = 0;

    public static int selectedColour=0xFFFFFFFF;
    public static float brushSize=1;

    public static boolean wasOpenPaintScreenDown = false;
    private static boolean wasWhistleDown = false;
    private static boolean wasCameraLockDown = false;
    private static boolean wasFreeCamDown = false;

    public static final Map<UUID, PoseTracker> POSES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        Networking.registerClientReceivers();
        ChameleonSounds.initialize();

        LevelRenderEvents.BEFORE_GIZMOS.register((ctx) -> {
            ChameleonOutputTargets.clearUvPickerTarget();
        });

        LevelRenderEvents.END_MAIN.register((ctx) -> {
            UvPicker.pickPixel(ChameleonOutputTargets.UV_PICKER_TARGET.getRenderTarget(), mouseX, mouseY, (result) -> {
                uvCol = result;
            });
            for (PoseTracker tracker : POSES.values()) {
                tracker.update(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks());
            }
        });

        KeyMapping.Category CATEGORY = KeyMapping.Category.register(
                MCChameleon.id("hider")
        );

        KeyMapping openPaintScreen = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.mc-chameleon.open_paint",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_F,
                        CATEGORY
                ));
        KeyMapping openPoseScreen = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.mc-chameleon.pose",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_R,
                        CATEGORY
                ));
        KeyMapping whistle = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.mc-chameleon.whistle",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_1,
                        CATEGORY
                ));
        KeyMapping cameraLock = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.mc-chameleon.cameraLock",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_5,
                        CATEGORY
                ));
        KeyMapping freeCam = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.mc-chameleon.freeCam",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_4,
                        CATEGORY
                ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player==null) return;

            if (openPaintScreen.isDown()) {
                if (!wasOpenPaintScreenDown) {
                    if (ChameleonTexture.skins.containsKey(client.player.getUUID())) {
                        wasOpenPaintScreenDown=true;
                        client.setScreenAndShow(new PaintScreen());
                    }
                    wasOpenPaintScreenDown=true;
                }
            } else {
                wasOpenPaintScreenDown = false;
            }
            if (whistle.isDown()) {
                if (!wasWhistleDown) {
                    if (ChameleonTexture.skins.containsKey(client.player.getUUID())) {
                        ClientPlayNetworking.send(new Payloads.ServerBoundWhistle());
                    }
                    wasWhistleDown = true;
                }
            } else {
                wasWhistleDown = false;
            }
            ChameleonOrbitCamera camera = ChameleonOrbitCamera.getInstance();
            if (camera==null) return;
            if (camera.isActive()) {
                camera.tick();
            }
            if (cameraLock.isDown()) {
                if (!wasCameraLockDown&&client.getCameraEntity()!=null) {
                    if (!camera.isActive()) {
                        ChameleonOrbitCamera.recreate();
                        camera = ChameleonOrbitCamera.getInstance();
                        camera.setActive(true);
                        camera.setInvisible(true);
                        camera.syncToEntityLookDirection(client.getCameraEntity().getYRot(), client.getCameraEntity().getXRot());
                        if (client.level!=null) client.level.addEntity(camera);
                        client.setCameraEntity(camera);
                    } else {
                        camera.setActive(false);
                        if (client.level!=null) client.level.removeEntity(camera.getId(), Entity.RemovalReason.DISCARDED);
                        client.setCameraEntity(client.player);
                    }
                    wasCameraLockDown = true;
                }
            } else {
                wasCameraLockDown = false;
            }
            if (freeCam.isDown()) {
                if (!wasFreeCamDown&&camera.isActive()) {
                    camera.setFreeCam(!camera.isInFreeCam());
                    camera.setPos(camera.getNonFreeCamPosition());
                    wasFreeCamDown = true;
                }
            } else {
                wasFreeCamDown = false;
            }
            UUID uuid = client.player.getUUID();
            PoseTracker poseTracker = POSES.computeIfAbsent(uuid, k -> new PoseTracker());
            if (openPoseScreen.consumeClick()) {
                Poses currentPose = poseTracker.getTargetPos();

                Poses nextPose = (currentPose == null) ? Poses.T_POSE : switch (currentPose) {
                    case T_POSE -> Poses.ARCH;
                    case ARCH -> Poses.FLAT;
                    case FLAT -> null;
                };

                poseTracker.setTargetPose(nextPose);
                ClientPlayNetworking.send(new Payloads.ServerBoundPosePayload(nextPose));
            }
            if (client.player.isSprinting()) {
                poseTracker.setTargetPose(null);
                ClientPlayNetworking.send(new Payloads.ServerBoundPosePayload(null));
            }
            if (!(client.gui.screen() instanceof PaintScreen)&&!camera.isInFreeCam()) {
                if (IdleTracker.checkAutoDisable(client.player)) {
                    if (camera.isActive()) {
                        camera.deactivate(client.player);
                        client.setCameraEntity(client.player);
                    }
                }
            }
            else IdleTracker.preventDisable(client.player);
        });
    }
}
