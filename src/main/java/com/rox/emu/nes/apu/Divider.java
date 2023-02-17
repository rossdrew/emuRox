package com.rox.emu.nes.apu;

import com.rox.emu.timing.Clock;

import java.util.ArrayList;
import java.util.List;

public class Divider implements Clock.ClockWatcher {
    private final List<ClockWatcher> watchers = new ArrayList<>();

    private int period;
    private int countDown = 0;

    Divider(int period){
        this.period = period;
        reset();
    }

    public void addClockWatcher(final ClockWatcher watcher) {
        this.watchers.add(watcher);
    }

    public void changePeriod(final int period){
        this.period = period;
    }

    public void reset() {
        countDown = period;
    }

    @Override
    public void start() {
        //Not implemented
    }

    @Override
    public void tick() {
        if (--countDown < 1) {
            watchers.forEach(ClockWatcher::tick);
            reset();
        }
    }

    @Override
    public void stop() {
        //Not implemented
    }

    /** Some class which wants to be informed when the clock progresses */
    public interface ClockWatcher {
        void tick();
    }
}
