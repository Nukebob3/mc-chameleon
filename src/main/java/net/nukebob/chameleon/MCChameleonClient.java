package net.nukebob.chameleon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.gameplay.IdleTracker;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.keybind.Keybinds;
import net.nukebob.chameleon.networking.Networking;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.render.ChameleonOutputTargets;
import net.nukebob.chameleon.screen.PaintScreen;
import net.nukebob.chameleon.sound.ChameleonSounds;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.util.UvPicker;

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
        Keybinds.init();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            for (Identifier entryId : DebugScreenEntries.allEntries().keySet()) {
                client.debugEntries.setStatus(entryId, DebugScreenEntryStatus.NEVER);
            }
        });

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player==null) return;

            wasOpenPaintScreenDown = handleKeyEdge(Keybinds.openPaintScreen, wasOpenPaintScreenDown, () -> {
                if (ChameleonTexture.skins.containsKey(client.player.getUUID())) client.setScreenAndShow(new PaintScreen());
            });
            wasWhistleDown = handleKeyEdge(Keybinds.whistle, wasWhistleDown, () -> {
                if (ChameleonTexture.skins.containsKey(client.player.getUUID())) ClientPlayNetworking.send(new Payloads.ServerBoundWhistle());
            });

            ChameleonOrbitCamera camera = ChameleonOrbitCamera.getInstance();
            if (camera==null) return;
            if (camera.isActive()) camera.tick();

            wasCameraLockDown = handleKeyEdge(Keybinds.cameraLock, wasCameraLockDown, () -> {
                if (client.getCameraEntity() == null) return;
                if (!camera.isActive()) {
                    ChameleonOrbitCamera.recreate();
                    ChameleonOrbitCamera cam = ChameleonOrbitCamera.getInstance();
                    cam.setActive(true);
                    cam.setInvisible(true);
                    cam.syncToEntityLookDirection(client.getCameraEntity().getYRot(), client.getCameraEntity().getXRot());
                    if (client.level != null) client.level.addEntity(cam);
                    client.setCameraEntity(cam);
                } else {
                    camera.setActive(false);
                    if (client.level != null) client.level.removeEntity(camera.getId(), Entity.RemovalReason.DISCARDED);
                    client.setCameraEntity(client.player);
                }
            });

            wasFreeCamDown = handleKeyEdge(Keybinds.freeCam, wasFreeCamDown, () -> {
                if (camera.isActive()) {
                    camera.setFreeCam(!camera.isInFreeCam());
                    camera.setPos(camera.getNonFreeCamPosition());
                }
            });

            UUID uuid = client.player.getUUID();
            PoseTracker poseTracker = POSES.computeIfAbsent(uuid, k -> new PoseTracker());
            if (Keybinds.openPoseScreen.consumeClick()) {
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

    private static boolean handleKeyEdge(KeyMapping key, boolean wasDown, Runnable onPress) {
        if (key.isDown()) {
            if (!wasDown) onPress.run();
            return true;
        }
        return false;
    }
}
