package com.rox.emu.timing;

/** Some class which wants to be informed when a clock progresses */
public interface ClockWatcher {
    void start();
    void tick();
    void stop();
}
