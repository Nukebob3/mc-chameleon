package net.nukebob.chameleon.gameplay;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.config.GameConfig;
import net.nukebob.chameleon.dimension.ChameleonDimensions;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.sound.ChameleonSounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class Game {
    Vec3 spawn;
    String dimension;

    public static boolean running = false;
    public static int whistleTime;
    public static GameState state;
    public static int time;
    private static Timer timer;

    public static void start() {
        running = true;
        GameConfig config = GameConfig.loadConfig();

        state = GameState.PREGAME;
        whistleTime = config.whistleFrequency;

        timer = new Timer(15, time -> {
            Game.time=time;
            if (state.equals(GameState.SEEK)) {
                whistleTime--;
                if (whistleTime <= 0) {
                    whistle(config);
                    whistleTime = config.whistleFrequency;
                }
            }

            update(config);
        }, () -> endTimer(config));
    }

    public static void update(GameConfig config) {
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            int maxTime = switch (state) {
                case PREGAME -> 15;
                case HIDE -> config.hideTime;
                case SEEK -> config.seekTime;
                case ANSWER -> config.answerCheckTime;
            };
            ServerPlayNetworking.send(player, new Payloads.ClientBoundGameHudUpdatePayload(time, maxTime, whistleTime, 0, 0));
        }
    }

    public static void whistle(GameConfig config) {
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            if (TeamControl.isChameleon(player.getTeam())) {
                player.level().playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        config.isWhistleSound ? ChameleonSounds.WHISTLE : ChameleonSounds.FART,
                        SoundSource.PLAYERS,
                        3f, 1f);
            }
        }
    }

    public static void end() {
        running = false;
        timer = null;
        lobbyTp();
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            ServerPlayNetworking.send(player, new Payloads.ClientBoundGameHudUpdatePayload(0, 0, 0, 0, 0));
        }
    }

    private static void endTimer(GameConfig config) {
        switch (state) {
            case PREGAME -> {
                state=GameState.HIDE;
                timer.setTime(config.hideTime);
                for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
                    player.setGameMode(GameType.ADVENTURE);
                }
                assignTeams(config);
                //hider tp
            }
            case HIDE -> {
                state=GameState.SEEK;
                timer.setTime(config.seekTime);
                //seeker tp
            }
            case SEEK -> {
                state=GameState.ANSWER;
                timer.setTime(config.answerCheckTime);
                for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
                    if (TeamControl.isChameleon(player.getTeam())) {
                        TeamControl.assignChameleonAnswer(player.getPlainTextName(), player.isSpectator());
                        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 0, true, false, false));
                    } else if (TeamControl.isHunter(player.getTeam())) {
                        TeamControl.assignHunterAnswer(player.getPlainTextName());
                    }
                }
            }
            case ANSWER -> {
                //lobby tp
                for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
                    player.removeAllEffects();
                }
                end();
            }
        }
    }

    public static void assignTeams(GameConfig config) {
        float huntPercent = config.hunterPercent;
        int hunters = 0;
        ArrayList<ServerPlayer> players = new ArrayList<>(PlayerLookup.all(MCChameleon.SERVER));
        int maxHunters = (int) (huntPercent*players.size());
        Collections.shuffle(players);
        for (ServerPlayer player : players) {
            ServerLevel level = MCChameleon.SERVER.getLevel(ChameleonDimensions.LOBBY);
            if (level==null) return;
            if (!player.level().equals(level)) continue;

            AABB box = new AABB(-89, 9, -43, -89 + 4, 9 + 3, -43 + 4);
            boolean onHunterPlatform = box.contains(player.position());

            if (onHunterPlatform) {
                MCChameleon.SERVER.getScoreboard().addPlayerToTeam(player.getPlainTextName(), TeamControl.getHuntersTeam());
                TeamControl.applyHunterAttributes(player);
                hunters++;
                if (hunters>=maxHunters) break;
            }
        }
        if (hunters<maxHunters)
            for (ServerPlayer player : players) {
                if (TeamControl.isHunter(player.getTeam())) continue;
                MCChameleon.SERVER.getScoreboard().addPlayerToTeam(player.getPlainTextName(), TeamControl.getHuntersTeam());
                TeamControl.applyHunterAttributes(player);
                hunters++;
                if (hunters>=maxHunters) break;
            }

        for (ServerPlayer player : players) {
            if (TeamControl.isHunter(player.getTeam())) continue;
            MCChameleon.SERVER.getScoreboard().addPlayerToTeam(player.getPlainTextName(), TeamControl.getChameleonsTeam());
            TeamControl.applyChameleonAttributes(player);
        }
    }

    public static void lobbyTp() {
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            MinecraftServer server = MCChameleon.SERVER;
            server.getScoreboard().removePlayerFromTeam(player.getPlainTextName());
            TeamControl.applyLobbyAttributes(player);
            player.setGameMode(GameType.ADVENTURE);

            ServerLevel level = server.getLevel(ChameleonDimensions.LOBBY);
            if (level==null) return;
            player.teleportTo(level, -86.5, 10.0, -43.5, Set.of(), 0, 0, true);
        }
    }

    public static void tick() {
        if (timer!=null)
            timer.tick();
    }
}
