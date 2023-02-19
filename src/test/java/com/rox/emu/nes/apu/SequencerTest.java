package com.rox.emu.nes.apu;

import com.rox.emu.nes.apu.Sequencer.EventWatcher;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SequencerTest {
    @Test
    public void creation(){
        final Sequencer<String> sequencer = new Sequencer<>(new String[0]);
        assertNotNull(sequencer);
    }

    @Test
    public void outputsEvent(){
        final Sequencer<String> sequencer = new Sequencer<>(new String[] {"A"});
        final EventWatcher<String> eventWatcherMock = mock(EventWatcher.class);
        sequencer.addEventWatcher(eventWatcherMock);

        sequencer.tick();

        verify(eventWatcherMock, times(1)).eventNotification("A");
    }

    @Test
    public void outputsSequentialEvents(){
        final Sequencer<String> sequencer = new Sequencer<>(new String[] {"A", "B"});
        final EventWatcher<String> eventWatcherMock = mock(EventWatcher.class);
        sequencer.addEventWatcher(eventWatcherMock);

        sequencer.tick();
        sequencer.tick();

        verify(eventWatcherMock, times(1)).eventNotification("A");
        verify(eventWatcherMock, times(1)).eventNotification("B");
        verify(eventWatcherMock, times(2)).eventNotification(any(String.class));
    }

    @Test
    public void emptyScriptEntryDoesntOutputEvent(){
        final Sequencer<String> sequencer = new Sequencer<>(new String[] {null, "B"});
        final EventWatcher<String> eventWatcherMock = mock(EventWatcher.class);
        sequencer.addEventWatcher(eventWatcherMock);

        sequencer.tick();
        sequencer.tick();

        verify(eventWatcherMock, times(1)).eventNotification("B");
    }
}
