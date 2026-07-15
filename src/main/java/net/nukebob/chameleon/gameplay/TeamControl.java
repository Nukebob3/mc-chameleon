package net.nukebob.chameleon.gameplay;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.nukebob.chameleon.MCChameleon;

public class TeamControl {
    private static PlayerTeam chameleons;
    private static PlayerTeam hunters;

    public static PlayerTeam getChameleonsTeam() {
        return chameleons;
    }

    public static PlayerTeam getChameleonsTeam(Scoreboard scoreboard) {
        return scoreboard.getPlayerTeam("chameleon");
    }

    public static PlayerTeam getHuntersTeam(Scoreboard scoreboard) {
        return scoreboard.getPlayerTeam("hunter");
    }

    public static void setChameleonsTeam(PlayerTeam chameleons) {
        TeamControl.chameleons = chameleons;
    }

    public static PlayerTeam getHuntersTeam() {
        return hunters;
    }

    public static void setHuntersTeam(PlayerTeam hunters) {
        TeamControl.hunters = hunters;
    }

    public static void nameTagVisibility(boolean normal) {
        PlayerTeam chameleonFound = MCChameleon.SERVER.getScoreboard().getPlayerTeam("chameleonFound");
        PlayerTeam chameleonNotFound = MCChameleon.SERVER.getScoreboard().getPlayerTeam("chameleonNotFound");
        chameleons.setNameTagVisibility(normal?Team.Visibility.HIDE_FOR_OTHER_TEAMS: Team.Visibility.ALWAYS);
        if (chameleonFound!=null) chameleonFound.setNameTagVisibility(normal?Team.Visibility.HIDE_FOR_OTHER_TEAMS: Team.Visibility.ALWAYS);
        if (chameleonNotFound!=null) chameleonNotFound.setNameTagVisibility(normal?Team.Visibility.HIDE_FOR_OTHER_TEAMS: Team.Visibility.ALWAYS);
        hunters.setNameTagVisibility(Team.Visibility.ALWAYS);
    }

    public static boolean isChameleonStrict(PlayerTeam team) {
        if (team==null) return false;
        return team.getName().equals("chameleon");
    }
    public static boolean isChameleon(PlayerTeam team) {
        if (team==null) return false;
        return team.getName().contains("chameleon");
    }

    public static boolean isChameleonUnfound(PlayerTeam team) {
        if (team==null) return false;
        return team.getName().equals("chameleon")||team.getName().equals("chameleonNotFound");
    }

    public static boolean isHunter(PlayerTeam team) {
        if (team==null) return false;
        return team.getName().contains("hunter");
    }

    public static boolean isHunterStrict(PlayerTeam team) {
        if (team==null) return false;
        return team.getName().equals("hunter");
    }

    public static boolean isLocked(PlayerTeam team) {
        if (team==null) return false;
        return team.getName().contains("Found") || team.getName().contains("NotFound") || team.getName().contains("Answer");
    }

    public static void assignChameleonAnswer(String player, boolean found) {
        PlayerTeam team;
        if (found) team = MCChameleon.SERVER.getScoreboard().getPlayerTeam("chameleonFound");
        else team = MCChameleon.SERVER.getScoreboard().getPlayerTeam("chameleonNotFound");

        MCChameleon.SERVER.getScoreboard().addPlayerToTeam(player, team);
    }

    public static void assignHunterAnswer(String player) {
        PlayerTeam team = MCChameleon.SERVER.getScoreboard().getPlayerTeam("hunterAnswer");

        MCChameleon.SERVER.getScoreboard().addPlayerToTeam(player, team);
    }

    public static void applyChameleonAttributes(ServerPlayer player) {
        applyCommonAttributes(player);
        player.getAttributes().getInstance(Attributes.SCALE).setBaseValue(0.5);
    }

    public static void applyHunterAttributes(ServerPlayer player) {
        applyCommonAttributes(player);
        player.getAttributes().getInstance(Attributes.SCALE).setBaseValue(1.0);
    }

    public static void applyLobbyAttributes(ServerPlayer player) {
        applyCommonAttributes(player);
        player.getAttributes().getInstance(Attributes.SCALE).setBaseValue(0.99);
        player.getAttributes().getInstance(Attributes.AIR_DRAG_MODIFIER).setBaseValue(1);
    }

    private static void applyCommonAttributes(ServerPlayer player) {
        player.getAttributes().getInstance(Attributes.AIR_DRAG_MODIFIER).setBaseValue(3.0);
        player.getAttributes().getInstance(Attributes.NAME_TAG_DISTANCE).setBaseValue(128);
        player.getAttributes().getInstance(Attributes.GRAVITY).setBaseValue(0.06);
        player.getAttributes().getInstance(Attributes.JUMP_STRENGTH).setBaseValue(0.25);
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setInvulnerable(true);
    }
}
