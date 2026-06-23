package net.nukebob.chameleon;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.minecraft.server.MinecraftServer;
import net.nukebob.chameleon.command.CanvasCommand;
import net.nukebob.chameleon.networking.Networking;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.texture.ChameleonTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCChameleon implements ModInitializer {
	public static final String MOD_ID = "mc-chameleon";

	public static MinecraftServer SERVER;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SERVER=server;
		});

		LOGGER.info("MC Chameleon loaded!");

		ClientPlayConnectionEvents.JOIN.register((clientPacketListener, packetSender, minecraft) -> {
			ChameleonTexture texture = new ChameleonTexture(minecraft.player.getUUID());
			texture.init();
			texture.upload();
		});

		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
			//minecraft.player.sendSystemMessage(Component.literal(minecraft.player.getUUID().toString()));
		});

		CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
			commandDispatcher.register(commandDispatcher.register(CanvasCommand.command).createBuilder());
		}));

		Payloads.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	public static Identifier idSkin(String path) {
		return Identifier.fromNamespaceAndPath("mc-chameleon-skin", path);
	}
}
