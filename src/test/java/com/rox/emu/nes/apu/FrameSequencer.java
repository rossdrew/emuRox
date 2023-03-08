package com.rox.emu.nes.apu;

import com.rox.emu.nes.apu.Sequencer.EventWatcher;
import com.rox.emu.timing.ClockWatcher;

/**
 * A linked {@link Divider} and {@link Sequencer} & effectively acting like a {@link Sequencer}
 *
 * @param <EventType> describing the type of events generated
 */
public class FrameSequencer<EventType> implements ClockWatcher {
    private final Divider divider;
    private final Sequencer<EventType> sequencer;

    public FrameSequencer(final int period,
                          final EventType[] script) {
        divider = new Divider(period);
        sequencer = new Sequencer<>(script);
        divider.addClockWatcher(sequencer);
    }

    public void addEventWatcher(final EventWatcher<EventType> eventWatcher) {
        sequencer.addEventWatcher(eventWatcher);
    }

    @Override
    public void start() {
        divider.start();
    }

    @Override
    public void tick() {
        divider.tick();
    }

    @Override
    public void stop() {
        divider.stop();
    }
}
