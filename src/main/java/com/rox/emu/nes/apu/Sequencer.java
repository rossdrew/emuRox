package com.rox.emu.nes.apu;

import com.rox.emu.timing.ClockWatcher;

import java.util.ArrayList;
import java.util.List;

public class Sequencer<EventType> implements ClockWatcher {
    private final EventType[] script;
    private final List<EventWatcher<EventType>> watchers = new ArrayList<>();

    private int eventIndex = 0;

    public Sequencer(final EventType[] script) {
        this.script = script;
    }

    @Override
    public void start() {
        //Not implemented
    }

    @Override
    public void tick() {
        if (script[eventIndex] != null) {
            for (EventWatcher<EventType> watcher : watchers) {
                watcher.eventNotification(script[eventIndex]);
            }
        }
        eventIndex++;
    }

    @Override
    public void stop() {
        //Not implemented
    }

    public void addEventWatcher(final EventWatcher eventWatcher) {
        watchers.add(eventWatcher);
    }

    public interface EventWatcher<EventType> {
        void eventNotification(final EventType event);
    }
}
