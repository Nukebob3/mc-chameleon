package net.nukebob.chameleon.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.gameplay.TeamControl;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.screen.PaintScreen;
import net.nukebob.chameleon.texture.ChameleonTexture;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class Keybinds {
    public static boolean wasOpenPaintScreenDown = false;
    private static boolean wasWhistleDown = false;
    private static boolean wasCameraLockDown = false;
    private static boolean wasFreeCamDown = false;
    private static boolean wasToggleNamePlateDown = false;

    private static boolean wasSwapSpectateTargetLeft = false;
    private static boolean wasSwapSpectateTargetRight = false;

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
    public static final KeyMapping toggleNameplate = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mc-chameleon.toggle_nameplate",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_2,
                    CATEGORY
            )
    );

    public static void init() {}

    public static void register(Minecraft client) {
        wasOpenPaintScreenDown = handleKeyEdge(Keybinds.openPaintScreen, wasOpenPaintScreenDown, () -> {
            if (ChameleonTexture.skins.containsKey(client.player.getUUID())&& TeamControl.isChameleonStrict(client.player.getTeam())) client.gui.setScreen(new PaintScreen());
        });
        wasWhistleDown = handleKeyEdge(Keybinds.whistle, wasWhistleDown, () -> whistle(client));
        ChameleonOrbitCamera camera = ChameleonOrbitCamera.getInstance();
        wasCameraLockDown = handleKeyEdge(Keybinds.cameraLock, wasCameraLockDown, () -> {
            if (!TeamControl.isChameleon(client.player.getTeam())&&!ChameleonOrbitCamera.getInstance().isActive()) return;
            if (GameType.SPECTATOR.equals(client.player.gameMode())) return;
            if (TeamControl.isLocked(client.player.getTeam())) return;

            if (client.getCameraEntity() == null) return;
            if (!camera.isActive()) {
                ChameleonOrbitCamera.recreate();
                ChameleonOrbitCamera cam = ChameleonOrbitCamera.getInstance();
                cam.setActive(true);
                cam.setInvisible(true);
                cam.spectateWho=client.player;
                cam.syncToEntityLookDirection(client.getCameraEntity().getYRot(), client.getCameraEntity().getXRot());
                if (client.level != null) client.level.addEntity(cam);
                client.setCameraEntity(cam);
            } else {
                camera.setActive(false);
                client.player.getAttributes().getInstance(Attributes.CAMERA_DISTANCE).setBaseValue(camera.getDistance()*2);
                camera.setId(-333);
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

        wasToggleNamePlateDown = handleKeyEdge(Keybinds.toggleNameplate, wasToggleNamePlateDown, Keybinds::toggleNameplate);

        wasSwapSpectateTargetLeft = handleKeyEdge(client.options.keyAttack, wasSwapSpectateTargetLeft, () -> {
            if (client.level==null) return;
            camera.setSpectatorTarget(MCChameleonClient.getSpectateTarget(client.level, client.player, camera.spectateWho, true));
        });
        wasSwapSpectateTargetRight = handleKeyEdge(client.options.keyUse, wasSwapSpectateTargetRight, () -> {
            if (client.level==null) return;
            camera.setSpectatorTarget(MCChameleonClient.getSpectateTarget(client.level, client.player, camera.spectateWho, false));
        });
        if (Keybinds.openPoseScreen.consumeClick()) pose(client);
    }

    private static boolean handleKeyEdge(KeyMapping key, boolean wasDown, Runnable onPress) {
        if (key.isDown()) {
            if (!wasDown) onPress.run();
            return true;
        }
        return false;
    }

    public static void whistle(Minecraft client) {
        if (GameType.SPECTATOR.equals(client.player.gameMode())) return;
        if (TeamControl.isChameleon(client.player.getTeam())) ClientPlayNetworking.send(new Payloads.ServerBoundWhistle());
    }
    public static void toggleNameplate() {
        MCChameleonClient.namePlatesDisplay = !MCChameleonClient.namePlatesDisplay;
    }
    public static void pose(Minecraft client) {
        UUID uuid = client.player.getUUID();
        PoseTracker poseTracker = MCChameleonClient.POSES.computeIfAbsent(uuid, k -> new PoseTracker());
        if (!TeamControl.isChameleonStrict(client.player.getTeam())) return;
        if (client.player.isSpectator()) return;

        Poses currentPose = poseTracker.getTargetPos();

        Poses nextPose = (currentPose == null) ? Poses.T_POSE : switch (currentPose) {
            case T_POSE -> Poses.ARCH;
            case ARCH -> Poses.FLAT;
            case FLAT -> null;
        };

        poseTracker.setTargetPose(nextPose);
        ClientPlayNetworking.send(new Payloads.ServerBoundPosePayload(nextPose));
        client.player.refreshDimensions();
        if (nextPose==null) MCChameleonClient.climbing = false;
    }
}
