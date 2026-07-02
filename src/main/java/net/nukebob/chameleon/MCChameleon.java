package net.nukebob.chameleon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.nukebob.chameleon.command.CanvasCommand;
import net.nukebob.chameleon.command.GameConfigCommand;
import net.nukebob.chameleon.config.GameConfig;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.networking.Networking;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.networking.Skins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
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
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			GameConfig.saveConfig();
		});


		LOGGER.info("MC Chameleon loaded!");

		CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
			commandDispatcher.register(CanvasCommand.command);
			commandDispatcher.register(GameConfigCommand.command);
		}));

		ServerPlayerEvents.JOIN.register(player -> {
			Skins.skinMap.forEach((uuid, skin) -> {
				ServerPlayNetworking.send(player, new Payloads.ClientBoundUpdatePixelsPayload(uuid, skin));
			});
			POSES.forEach(((uuid, poseTracker) -> {
				ServerPlayNetworking.send(player, new Payloads.ClientBoundPosePayload(uuid, poseTracker.getPose()==null?-1:poseTracker.getPose().ordinal()));
			}));
		});

		Payloads.register();
		Networking.registerServerReceivers();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	public static Identifier idSkin(String path) {
		return Identifier.fromNamespaceAndPath("mc-chameleon-skin", path);
	}
}
