package net.nukebob.chameleon.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.nukebob.chameleon.gameplay.Game;

public class StartCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("chameleon:start_game")
            .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
            .executes(ctx -> {
                Game.start();
                return 1;
            });
}
