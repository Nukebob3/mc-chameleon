package net.nukebob.chameleon.gameplay;

import net.nukebob.chameleon.config.GameConfig;

public class Game {
    public static int whistleTime;
    public static GameState state;
    public static int time;
    private static Timer timer;

    public static void start() {
        state = GameState.PREGAME;
        whistleTime = GameConfig.loadConfig().whistleFrequency;

        timer = new Timer(15, time -> {
            Game.time=time;
            if (state.equals(GameState.PREGAME)) return;
            whistleTime--;
            if (whistleTime <= 0) {
                whistleTime = GameConfig.loadConfig().whistleFrequency;
            }
        }, Game::endTimer);
    }

    private static void endTimer() {
        switch (state) {
            case PREGAME -> {
                state=GameState.HIDE;
                timer.setTime(GameConfig.loadConfig().hideTime);
            }
            case HIDE -> {}
            case SEEK -> {}
            case ANSWER -> {}
        }
    }

    public static void tick() {
        if (timer!=null)
            timer.tick();
    }
}
