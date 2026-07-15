package net.nukebob.chameleon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.TeamColor;
import net.nukebob.chameleon.command.*;
import net.nukebob.chameleon.config.GameConfig;
import net.nukebob.chameleon.dimension.ChameleonDimensions;
import net.nukebob.chameleon.dimension.MapsPlacementData;
import net.nukebob.chameleon.gameplay.*;
import net.nukebob.chameleon.item.ChameleonItems;
import net.nukebob.chameleon.networking.Networking;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.networking.Skins;
import net.nukebob.chameleon.sound.ChameleonSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MCChameleon implements ModInitializer {
	public static final String MOD_ID = "mc-chameleon";

	public static MinecraftServer SERVER;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Map<UUID, PoseTracker> POSES = new HashMap<>();

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SERVER=server;
			GameConfig.loadConfig();

			PlayerTeam chameleon = server.getScoreboard().getPlayerTeam("chameleon");
			PlayerTeam chameleonFound = server.getScoreboard().getPlayerTeam("chameleonFound");
			PlayerTeam chameleonNotFound = server.getScoreboard().getPlayerTeam("chameleonNotFound");
			PlayerTeam hunter = server.getScoreboard().getPlayerTeam("hunter");
			PlayerTeam hunterAnswer = server.getScoreboard().getPlayerTeam("hunterAnswer");
			if (chameleon==null) chameleon = server.getScoreboard().addPlayerTeam("chameleon");
			if (chameleonFound==null) chameleonFound = server.getScoreboard().addPlayerTeam("chameleonFound");
			if (chameleonNotFound==null) chameleonNotFound = server.getScoreboard().addPlayerTeam("chameleonNotFound");
			if (hunter==null) hunter = server.getScoreboard().addPlayerTeam("hunter");
			if (hunterAnswer==null) hunterAnswer = server.getScoreboard().addPlayerTeam("hunterAnswer");

			chameleon.setColor(Optional.of(TeamColor.WHITE));
			chameleon.setAllowFriendlyFire(false);
			chameleon.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
			chameleon.setCollisionRule(Team.CollisionRule.NEVER);

			chameleonFound.setColor(Optional.of(TeamColor.AQUA));
			chameleonFound.setAllowFriendlyFire(false);
			chameleonFound.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
			chameleonFound.setCollisionRule(Team.CollisionRule.NEVER);

			chameleonNotFound.setColor(Optional.of(TeamColor.RED));
			chameleonNotFound.setAllowFriendlyFire(false);
			chameleonNotFound.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
			chameleonNotFound.setCollisionRule(Team.CollisionRule.NEVER);

			hunter.setColor(Optional.of(TeamColor.RED));
			hunter.setAllowFriendlyFire(false);
			hunter.setNameTagVisibility(Team.Visibility.ALWAYS);
			hunter.setCollisionRule(Team.CollisionRule.NEVER);

			hunterAnswer.setColor(Optional.of(TeamColor.RED));
			hunterAnswer.setAllowFriendlyFire(false);
			hunterAnswer.setNameTagVisibility(Team.Visibility.ALWAYS);
			hunterAnswer.setCollisionRule(Team.CollisionRule.NEVER);

			TeamControl.setChameleonsTeam(chameleon);
			TeamControl.setHuntersTeam(hunter);

			GameRuleControl.setGameRules(server);

			//make default map
			ServerLevel mapsLevel = server.getLevel(ChameleonDimensions.MAPS);
			if (mapsLevel != null) {
				MapsPlacementData placementData = mapsLevel.getDataStorage()
						.computeIfAbsent(MapsPlacementData.TYPE);

				if (!placementData.isPlaced()) {
				StructureTemplateManager structureTemplateManager = server.getStructureManager();
				boolean placed = structureTemplateManager.get(MCChameleon.id("minecraft_map"))
						.orElseThrow()
						.placeInWorld(mapsLevel, new BlockPos(0, 100, 0), BlockPos.ZERO, new StructurePlaceSettings(), mapsLevel.getRandom(), 0);
				if (placed) placementData.markPlaced();
				}
			}
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			GameConfig.saveConfig();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (Game.running) Game.tick();
		});

		CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
			commandDispatcher.register(CanvasCommand.command);
			commandDispatcher.register(GameConfigCommand.command);
			commandDispatcher.register(StartCommand.command);
			commandDispatcher.register(EndCommand.command);
			commandDispatcher.register(SetMapCommand.command);
		}));

		ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
			Skins.skinMap.forEach((uuid, skin) -> {
				ServerPlayNetworking.send(listener.player, new Payloads.ClientBoundUpdatePixelsPayload(uuid, skin));
			});
			POSES.forEach(((uuid, tracker) -> {
				Poses pose = tracker.getTargetPos();
				sender.sendPacket(new Payloads.ClientBoundPosePayload(uuid, pose == null ? -1 : pose.ordinal()));
			}));
			AttributeControl.setCommonAttributes(listener.player);
			TeamControl.applyLobbyAttributes(listener.player);
			listener.player.setGameMode(Game.running?GameType.SPECTATOR:GameType.ADVENTURE);
			server.getScoreboard().removePlayerFromTeam(listener.player.getPlainTextName());

		});
		ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> {
			POSES.remove(listener.player.getUUID());
			Skins.remove(listener.player.getUUID());
		});

		Payloads.register();
		ChameleonSounds.initialize();
		Networking.registerServerReceivers();
		ChameleonItems.init();
		ChameleonDimensions.registerDimensions();

		LOGGER.info("MC Chameleon loaded!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	public static Identifier idSkin(String path) {
		return Identifier.fromNamespaceAndPath("mc-chameleon-skin", path);
	}
}
