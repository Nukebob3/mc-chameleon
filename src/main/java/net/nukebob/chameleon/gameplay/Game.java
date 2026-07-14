package net.nukebob.chameleon.gameplay;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.config.GameConfig;
import net.nukebob.chameleon.dimension.ChameleonDimensions;
import net.nukebob.chameleon.item.ChameleonItems;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.networking.Skins;
import net.nukebob.chameleon.sound.ChameleonSounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class Game {
    public static GameMap map;

    public static boolean running = false;
    public static int whistleTime;
    public static GameState state;
    public static int time;
    private static Timer timer;

    public static void start() {
        running = true;
        GameConfig config = GameConfig.loadConfig();

        map = new GameMap(
                ResourceKey.create(Registries.DIMENSION, GameConfig.loadConfig().mapLevel),
                GameConfig.loadConfig().mapSpawn,
                GameConfig.loadConfig().mapSpawnRotation
        );

        state = GameState.PREGAME;
        whistleTime = config.whistleFrequency;

        TeamControl.nameTagVisibility(true);

        timer = new Timer(15, time -> {
            Game.time=time;
            if (state.equals(GameState.SEEK)&&time!=config.seekTime) {
                whistleTime--;
                if (whistleTime <= 0&&time!=0) {
                    whistle(config);
                    whistleTime = config.whistleFrequency;
                }
            }

            update(config);
        }, () -> endTimer(config));

        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            player.removeAllEffects();
            MCChameleon.SERVER.getScoreboard().removePlayerFromTeam(player.getPlainTextName());
            Skins.remove(player.getUUID());
            TeamControl.applyLobbyAttributes(player);
            player.getInventory().clearContent();
            ServerPlayNetworking.send(player, new Payloads.ClientBoundGameHudPlayersPayload(0,0));
        }
    }

    public static void update(GameConfig config) {
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            int maxTime = switch (state) {
                case PREGAME -> 15;
                case HIDE -> config.hideTime;
                case SEEK -> config.seekTime;
                case ANSWER -> config.answerCheckTime;
            };
            String subtitle = switch (state) {
                case PREGAME -> "Hunter Selection";
                case HIDE -> "Until Search Starts";
                case SEEK -> "Search Time";
                case ANSWER -> "Answer Check";
            };
            ServerPlayNetworking.send(player, new Payloads.ClientBoundGameHudUpdatePayload(time, maxTime, whistleTime, subtitle));
            updatePlayerCount();
        }
    }

    public static void updatePlayerCount() {
        int hiders = 0;
        int seekers = 0;
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            if (!player.isSpectator()&&TeamControl.isChameleonUnfound(player.getTeam())) hiders++;
            if (TeamControl.isHunter(player.getTeam())) seekers++;
        }
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            ServerPlayNetworking.send(player, new Payloads.ClientBoundGameHudPlayersPayload(hiders, seekers));
        }

        if (hiders==0&&state.equals(GameState.SEEK)) {
            endTimer(GameConfig.loadConfig());
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
            Skins.remove(player.getUUID());
            player.setGameMode(GameType.ADVENTURE);
            MCChameleon.POSES.clear();
            ServerPlayNetworking.send(player, new Payloads.ClientBoundClearPosesPayload());
            player.setNoGravity(false);
            player.getInventory().clearContent();
            ServerPlayNetworking.send(player, new Payloads.ClientBoundGameHudUpdatePayload(0, 0, 0, ""));
            ServerPlayNetworking.send(player, new Payloads.ClientBoundGameHudPlayersPayload(0,0));
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
                for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
                    player.getInventory().clearContent();
                    if (TeamControl.isHunter(player.getTeam())) {
                        player.getInventory().setItem(0, new ItemStack(ChameleonItems.GUN));
                        player.getInventory().setSelectedSlot(0);
                    }
                    if (!TeamControl.isChameleon(player.getTeam())) continue;
                    Skins.blank(player.getUUID());
                }
                updatePlayerCount();
                TeamControl.nameTagVisibility(true);
                //hider tp
                for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
                    if (!TeamControl.isChameleon(player.getTeam())) continue;
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 60, 20));
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Hide Start!")));
                    playLocalSound(player, ChameleonSounds.BELL_START);


                    mapTp(player);
                }
            }
            case HIDE -> {
                state=GameState.SEEK;
                timer.setTime(config.seekTime);
                //seeker tp
                for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
                    player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 60, 20));
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Search Start!")));
                    playLocalSound(player, ChameleonSounds.BELL_START);

                    if (!TeamControl.isHunter(player.getTeam())) continue;

                    mapTp(player);
                }
            }
            case SEEK -> {
                state=GameState.ANSWER;
                timer.setTime(config.answerCheckTime);
                winTitle();
                for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
                    if (TeamControl.isChameleon(player.getTeam())) {
                        player.setNoGravity(true);
                        player.setDeltaMovement(Vec3.ZERO);
                        TeamControl.assignChameleonAnswer(player.getPlainTextName(), player.isSpectator());
                        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 0, true, false, false));
                    } else if (TeamControl.isHunter(player.getTeam())) {
                        TeamControl.assignHunterAnswer(player.getPlainTextName());
                        player.getAbilities().mayfly = true;
                        player.getAbilities().flying = true;
                        player.getAttributes().getInstance(Attributes.AIR_DRAG_MODIFIER).setBaseValue(1);
                        player.onUpdateAbilities();
                    }
                    player.setGameMode(GameType.ADVENTURE);
                }
                TeamControl.nameTagVisibility(false);
            }
            case ANSWER -> {
                //lobby tp
                for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
                    player.removeAllEffects();
                    Skins.remove(player.getUUID());
                }
                end();
            }
        }
    }

    public static void playLocalSound(ServerPlayer player, SoundEvent soundEvent) {
        player.connection.send(new ClientboundSoundPacket(Holder.direct(soundEvent), SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 1, 1, 0));
    }

    public static void mapTp(ServerPlayer player) {
        ServerLevel level = MCChameleon.SERVER.getLevel(map.level);
        if (level==null) return;
        player.teleportTo(level, map.spawn.x(), map.spawn.y(), map.spawn.z(), Set.of(), map.rotation.y, map.rotation.x, true);
    }

    public static void winTitle() {
        boolean hidersWin = false;
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            if (!player.isSpectator()&&TeamControl.isChameleon(player.getTeam())) {
                hidersWin=true;
                break;
            }
        }
        for (ServerPlayer player : PlayerLookup.all(MCChameleon.SERVER)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 60, 20));
            if (hidersWin) player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Chameleons Win!")));
            else player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Hunters Win!")));
            playLocalSound(player, ChameleonSounds.BELL_END);
        }
    }

    public static void assignTeams(GameConfig config) {
        float huntPercent = config.hunterPercent;
        int hunters = 0;
        ArrayList<ServerPlayer> players = new ArrayList<>(PlayerLookup.all(MCChameleon.SERVER));
        int maxHunters = (int) (huntPercent*players.size());
        if (players.size()>1&&maxHunters==0) maxHunters=1;
        else if (players.size()==1) maxHunters = 0;
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
            player.removeAllEffects();
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
