package net.nukebob.chameleon.gameplay;

import java.util.function.Consumer;

public class Timer {
    private final Consumer<Integer> onChange;
    private final Runnable onComplete;
    private int time;
    private int tick;

    public Timer(int time, Consumer<Integer> onChange, Runnable onComplete) {
        this.time = time;
        this.onChange = onChange;
        this.onComplete = onComplete;
    }

    public void setTime(int time) {
        onChange.accept(time);
                tick = 0;
        this.time = time;
    }

    public void tick() {
        tick++;
        if (tick>=20) {
            tick=0;
            time--;
            onChange.accept(time);

            if (time==0) {
                onComplete.run();
            }
        }
    }
}
