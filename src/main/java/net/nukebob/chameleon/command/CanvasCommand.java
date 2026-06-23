package net.nukebob.chameleon.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.nukebob.chameleon.networking.Skins;

import java.util.Collection;

public class CanvasCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("chameleon:canvas")
            .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
            .then(Commands.literal("blank")
                    .executes(ctx -> {
                        if (ctx.getSource().getEntity() instanceof Player player) {
                            Skins.blank(player.getUUID());
                            ctx.getSource().sendSuccess(() -> Component.literal("set player's canvas to blank"), false);
                        } else {

                        }
                        return 1;
                    })
                    .then(Commands.argument("selector", EntityArgument.players())
                            .executes(ctx -> {
                                Collection<ServerPlayer> selectors = EntityArgument.getPlayers(ctx, "selector");
                                selectors.forEach(player -> Skins.blank(player.getUUID()));
                                ctx.getSource().sendSuccess(() -> Component.literal("set players' canvas to blank"), false);
                                return 1;
                            })))
            .then(Commands.literal("clear")
                    .executes(ctx -> {
                        if (ctx.getSource().getEntity() instanceof Player player) {
                            Skins.remove(player.getUUID());
                            ctx.getSource().sendSuccess(() -> Component.literal("cleared player's canvas to blank"), false);
                        } else {

                        }
                        return 1;
                    })
                    .then(Commands.argument("selector", EntityArgument.players())
                            .executes(ctx -> {
                                Collection<ServerPlayer> selectors = EntityArgument.getPlayers(ctx, "selector");
                                selectors.forEach(player -> Skins.remove(player.getUUID()));
                                ctx.getSource().sendSuccess(() -> Component.literal("cleared players' canvas to blank"), false);
                                return 1;
                            })));
}
