package net.nukebob.chameleon.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.nukebob.chameleon.config.GameConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class GameConfigCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command = createDynamicConfigCommand();

    private static LiteralArgumentBuilder<CommandSourceStack> createDynamicConfigCommand() {
        LiteralArgumentBuilder<CommandSourceStack> baseCommand = Commands.literal("chameleon:config")
                .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR));

        GameConfig config = GameConfig.loadConfig();

        for (Field field : GameConfig.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;

            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            var fieldNode = Commands.literal(fieldName);

            if (fieldType == boolean.class || fieldType == Boolean.class) {
                fieldNode.then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> setValue(ctx, field, BoolArgumentType.getBool(ctx, "value"))));
            }
            else if (fieldType == int.class || fieldType == Integer.class) {
                fieldNode.then(Commands.argument("value", IntegerArgumentType.integer())
                        .executes(ctx -> setValue(ctx, field, IntegerArgumentType.getInteger(ctx, "value"))));
            }
            else if (fieldType == float.class || fieldType == Float.class) {
                fieldNode.then(Commands.argument("value", FloatArgumentType.floatArg())
                        .executes(ctx -> setValue(ctx, field, FloatArgumentType.getFloat(ctx, "value"))));
            }
            else if (fieldType == String.class) {
                fieldNode.then(Commands.argument("value", StringArgumentType.string())
                        .executes(ctx -> setValue(ctx, field, StringArgumentType.getString(ctx, "value"))));
            }

            baseCommand.then(fieldNode);
        }

        return baseCommand;
    }

    private static int setValue(CommandContext<CommandSourceStack> ctx, Field field, Object value) {
        try {
            GameConfig configInstance = GameConfig.loadConfig();

            field.set(configInstance, value);

            GameConfig.saveConfig();

            ctx.getSource().sendSuccess(() -> Component.literal("Successfully set §a" + field.getName() + "§r to §b" + value), true);
            return 1;
        } catch (IllegalAccessException e) {
            ctx.getSource().sendFailure(Component.literal("Failed to update configuration property: " + field.getName()));
            return 0;
        }
    }
}
