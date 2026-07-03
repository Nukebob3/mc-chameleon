package net.nukebob.chameleon.gameplay;

import net.minecraft.world.scores.Team;

public class TeamControl {
    private static Team chameleons;
    private static Team hunters;

    public static Team getChameleonsTeam() {
        return chameleons;
    }

    public static void setChameleonsTeam(Team chameleons) {
        TeamControl.chameleons = chameleons;
    }

    public static Team getHuntersTeam() {
        return hunters;
    }

    public static void setHuntersTeam(Team hunters) {
        TeamControl.hunters = hunters;
    }

    public static boolean isChameleon(Team team) {
        if (team==null) return false;
        return team.getName().equals("chameleon");
    }

    public static boolean isHunter(Team team) {
        if (team==null) return false;
        return team.getName().equals("hunter");
    }
}
