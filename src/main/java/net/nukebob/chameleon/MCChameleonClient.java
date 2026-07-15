package net.nukebob.chameleon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.gameplay.IdleTracker;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.gameplay.TeamControl;
import net.nukebob.chameleon.keybind.Keybinds;
import net.nukebob.chameleon.networking.Networking;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.render.ChameleonOutputTargets;
import net.nukebob.chameleon.render.GunBeamRenderer;
import net.nukebob.chameleon.screen.PaintScreen;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.util.UvPicker;

import java.util.*;

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
    private static boolean wasToggleNamePlateDown = false;

    private static boolean wasSwapSpectateTargetLeft = false;
    private static boolean wasSwapSpectateTargetRight = false;

    public static boolean wasSprinting = false;
    public static boolean wasJumping = false;

    public static boolean namePlatesDisplay = true;

    public static final Map<UUID, PoseTracker> POSES = new HashMap<>();

    public static boolean climbing = false;

    public static List<GunBeamRenderer> shots = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        Networking.registerClientReceivers();
        Keybinds.init();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            for (Identifier entryId : DebugScreenEntries.allEntries().keySet()) {
                client.debugEntries.setStatus(entryId, DebugScreenEntryStatus.NEVER);
            }
            //client.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.ALWAYS_ON);
        });

        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            localSkinCache = new int[1504];
            if (client.player==null) return;
            ChameleonTexture.skins.remove(client.player.getUUID());
            client.getTextureManager().release(MCChameleon.idSkin(client.player.getUUID().toString()));
        });

        LevelRenderEvents.BEFORE_GIZMOS.register((ctx) -> {
            ChameleonOutputTargets.clearUvPickerTarget();
        });

        LevelRenderEvents.END_MAIN.register((ctx) -> {
            UvPicker.pickPixel(ChameleonOutputTargets.UV_PICKER_TARGET.getRenderTarget(), mouseX, mouseY, (result) -> {
                uvCol = result;
            });
            POSES.entrySet().removeIf(entry -> !ChameleonTexture.skins.containsKey(entry.getKey()));
            POSES.values().forEach(tracker -> tracker.update(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks()));

            if (Minecraft.getInstance().player==null) return;
            double cameraDist = Minecraft.getInstance().player.getAttributes().getInstance(Attributes.CAMERA_DISTANCE).getBaseValue();
            double target = 4.0;
            double newDist = cameraDist + (target - cameraDist) * 0.1;
            if (cameraDist!=4.0) Minecraft.getInstance().player.getAttributes().getInstance(Attributes.CAMERA_DISTANCE).setBaseValue(newDist);

            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

            shots.forEach(shot -> {
                shot.render(ctx.poseStack(), ctx.submitNodeCollector(), ctx.levelState().cameraRenderState);
                shot.age(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks());
            });
            shots.removeIf(shot -> shot.getAge()>5f);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player==null) return;

            wasOpenPaintScreenDown = handleKeyEdge(Keybinds.openPaintScreen, wasOpenPaintScreenDown, () -> {
                if (ChameleonTexture.skins.containsKey(client.player.getUUID())&&TeamControl.isChameleonStrict(client.player.getTeam())) client.gui.setScreen(new PaintScreen());
            });
            wasWhistleDown = handleKeyEdge(Keybinds.whistle, wasWhistleDown, () -> {
                if (TeamControl.isChameleon(client.player.getTeam())) ClientPlayNetworking.send(new Payloads.ServerBoundWhistle());
            });

            ChameleonOrbitCamera camera = ChameleonOrbitCamera.getInstance();
            if (camera==null) return;
            if (camera.isActive()) camera.tick();

            if (!TeamControl.isChameleon(client.player.getTeam())&&camera.isActive()) {
                camera.setActive(false);
                client.setCameraEntity(client.player);
            }
            if (client.gui.screen() instanceof PaintScreen && (client.player.isSpectator()||!TeamControl.isChameleon(client.player.getTeam()))) client.gui.setScreen(null);

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

            wasToggleNamePlateDown = handleKeyEdge(Keybinds.toggleNameplate, wasToggleNamePlateDown, () -> {
                namePlatesDisplay = !namePlatesDisplay;
            });

            wasSwapSpectateTargetLeft = handleKeyEdge(client.options.keyAttack, wasSwapSpectateTargetLeft, () -> {
                if (client.level==null) return;
                camera.setSpectatorTarget(getSpectateTarget(client.level, client.player, camera.spectateWho, true));
            });
            wasSwapSpectateTargetRight = handleKeyEdge(client.options.keyUse, wasSwapSpectateTargetRight, () -> {
                if (client.level==null) return;
                camera.setSpectatorTarget(getSpectateTarget(client.level, client.player, camera.spectateWho, false));
            });

            if ((camera.spectateWho!=null&&GameType.SPECTATOR.equals(camera.spectateWho.gameMode())) && client.level!=null) {
                camera.setSpectatorTarget(getSpectateTarget(client.level, client.player, camera.spectateWho, false));
            }

            UUID uuid = client.player.getUUID();
            PoseTracker poseTracker = POSES.computeIfAbsent(uuid, k -> new PoseTracker());
            if (Keybinds.openPoseScreen.consumeClick()) {
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
                if (nextPose==null) climbing = false;
            }
            if (client.player.isSprinting()) {
                if (client.player.isSpectator()||!TeamControl.isChameleonStrict(client.player.getTeam())) return;
                poseTracker.setTargetPose(null);
                ClientPlayNetworking.send(new Payloads.ServerBoundPosePayload(null));
                client.player.refreshDimensions();
            }
            if (!(client.gui.screen() instanceof PaintScreen)&&!camera.isInFreeCam()) {
                if (IdleTracker.checkAutoDisable(client.player)) {
                    if (TeamControl.isLocked(client.player.getTeam())) {
                        IdleTracker.preventDisable(client.player);
                        return;
                    }
                    if (camera.isActive()&&!GameType.SPECTATOR.equals(client.player.gameMode())) {
                        camera.deactivate(client.player);
                        client.setCameraEntity(client.player);
                    } else if (GameType.SPECTATOR.equals(client.player.gameMode())) {
                        IdleTracker.preventDisable(client.player);
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

    private static Player getSpectateTarget(Level level, Player local, Player target, boolean left) {
        if (level==null || level.players().isEmpty()) return null;

        List<Player> players = new ArrayList<>(level.players());
        //players.removeIf(player -> (!TeamControl.isHunter(player.getTeam())&&!TeamControl.isChameleon(player.getTeam()))||!GameType.ADVENTURE.equals(player.gameMode()));

        players.removeIf(player -> {
            boolean isPlayer = TeamControl.isChameleon(player.getTeam())||TeamControl.isHunter(player.getTeam());
            boolean isSpectator = player.isSpectator();
            boolean infection = TeamControl.getChameleonsTeam(level.getScoreboard())!=null&&!player.equals(local)&&TeamControl.isChameleon(player.getTeam())&&TeamControl.getChameleonsTeam(level.getScoreboard()).getNameTagVisibility().equals(Team.Visibility.NEVER);
            return !isPlayer||isSpectator||infection;
        });

        if (players.isEmpty()) return null;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).equals(target==null?local:target)) {
                return players.get(left?(i==0?players.size()-1:i-1):(i==players.size()-1?0:i+1));
            }
        }
        return players.getFirst();
    }
}
