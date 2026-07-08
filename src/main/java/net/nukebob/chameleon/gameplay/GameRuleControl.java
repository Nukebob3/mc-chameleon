package net.nukebob.chameleon.gameplay;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRules;

public class GameRuleControl {
    public static void setGameRules(MinecraftServer server) {
        server.getGameRules().set(GameRules.FALL_DAMAGE, false, server);
        server.getGameRules().set(GameRules.DROWNING_DAMAGE, false, server);
        server.getGameRules().set(GameRules.FIRE_DAMAGE, false, server);
        server.getGameRules().set(GameRules.FREEZE_DAMAGE, false, server);
        server.getGameRules().set(GameRules.SPAWN_MOBS, false, server);
        server.getGameRules().set(GameRules.MOB_DROPS, false, server);
        server.getGameRules().set(GameRules.MOB_GRIEFING, false, server);
        server.getGameRules().set(GameRules.SPREAD_VINES, false, server);
        server.getGameRules().set(GameRules.COMMAND_BLOCK_OUTPUT, false, server);
        server.getGameRules().set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0, server);
        server.getGameRules().set(GameRules.LOCATOR_BAR, false, server);
        server.getGameRules().set(GameRules.SPAWN_MONSTERS, false, server);
        server.getGameRules().set(GameRules.SPAWN_PATROLS, false, server);
        server.getGameRules().set(GameRules.SPAWN_PHANTOMS, false, server);
        server.getGameRules().set(GameRules.SPAWN_WARDENS, false, server);
        server.getGameRules().set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
        server.getGameRules().set(GameRules.SPAWNER_BLOCKS_WORK, false, server);
        server.getGameRules().set(GameRules.SPECTATORS_GENERATE_CHUNKS, false, server);
    }
}
