package com.rox.emu.nes.apu;

import com.rox.emu.timing.ClockWatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * An NES PPU Sequencer.
 * Fires events of <EventType> to listeners based on received {@link ClockWatcher} ticks
 * iterating over a given script.
 * @param <EventType> for the type of events that will be fired
 */
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

        eventIndex = (eventIndex+1) % script.length;
    }

    @Override
    public void stop() {
        //Not implemented
    }

    public void addEventWatcher(final EventWatcher<EventType> eventWatcher) {
        watchers.add(eventWatcher);
    }

    public interface EventWatcher<EventType> {
        void eventNotification(final EventType event);
    }
}
