package net.nukebob.chameleon.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.nukebob.chameleon.config.GameConfig;
import net.nukebob.chameleon.gameplay.GameMap;

public class SetMapCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("chameleon:set_map")
            .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
            .executes(ctx -> {
                GameConfig.loadConfig().mapSpawn = ctx.getSource().getPosition();
                GameConfig.loadConfig().mapSpawnRotation = ctx.getSource().getRotation();
                GameConfig.loadConfig().mapLevel = ctx.getSource().getLevel().dimension().identifier();

                ctx.getSource().sendSuccess(() -> Component.literal("Set Map for Game"), true);
                return 1;
            })
            .then(Commands.argument("map", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        builder.suggest("minecraft");
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        String map = StringArgumentType.getString(ctx, "map");
                        switch (map) {
                            case "minecraft" -> {
                                GameMap m = GameMap.MINECRAFT;
                                GameConfig.loadConfig().mapSpawn = m.spawn;
                                GameConfig.loadConfig().mapSpawnRotation = m.rotation;
                                GameConfig.loadConfig().mapLevel = m.level.identifier();
                            }
                            default -> {}
                        }
                        ctx.getSource().sendSuccess(() -> Component.literal("Set Map for Game"), true);
                        return 1;
                    }));
}
