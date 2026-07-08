package net.nukebob.chameleon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.TeamColor;
import net.nukebob.chameleon.command.CanvasCommand;
import net.nukebob.chameleon.command.GameConfigCommand;
import net.nukebob.chameleon.config.GameConfig;
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
		//TODO - replace all checks of ChameleonTexture.skins.containsKey... with the team of chameleon or hunters

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SERVER=server;
			GameConfig.loadConfig();

			PlayerTeam chameleon = server.getScoreboard().getPlayerTeam("chameleon");
			PlayerTeam hunter = server.getScoreboard().getPlayerTeam("hunter");
			if (chameleon==null) chameleon = server.getScoreboard().addPlayerTeam("chameleon");
			if (hunter==null) hunter = server.getScoreboard().addPlayerTeam("hunter");

			chameleon.setColor(Optional.of(TeamColor.WHITE));
			chameleon.setAllowFriendlyFire(false);
			chameleon.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
			chameleon.setCollisionRule(Team.CollisionRule.NEVER);

			hunter.setColor(Optional.of(TeamColor.RED));
			hunter.setAllowFriendlyFire(false);
			hunter.setNameTagVisibility(Team.Visibility.ALWAYS);
			hunter.setCollisionRule(Team.CollisionRule.NEVER);

			TeamControl.setChameleonsTeam(chameleon);
			TeamControl.setHuntersTeam(hunter);

			GameRuleControl.setGameRules(server);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			GameConfig.saveConfig();
		});


		LOGGER.info("MC Chameleon loaded!");

		CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
			commandDispatcher.register(CanvasCommand.command);
			commandDispatcher.register(GameConfigCommand.command);
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
		});
		ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> {
			POSES.remove(listener.player.getUUID());
			Skins.remove(listener.player.getUUID());
		});

		Payloads.register();
		ChameleonSounds.initialize();
		Networking.registerServerReceivers();
		ChameleonItems.init();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	public static Identifier idSkin(String path) {
		return Identifier.fromNamespaceAndPath("mc-chameleon-skin", path);
	}
}
