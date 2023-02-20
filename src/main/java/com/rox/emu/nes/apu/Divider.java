package com.rox.emu.nes.apu;

import com.rox.emu.timing.ClockWatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A NES APU Divider module.*
 * Divides some frequency by a given factor and outputs a {@link ClockWatcher} tick for every <i>n</i> ticks provided.
 */
public class Divider implements ClockWatcher {
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
}
