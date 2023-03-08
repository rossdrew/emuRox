package com.rox.emu.nes.apu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FrameSequencerTest {
    @Mock
    public Sequencer.EventWatcher<String> eventWatcherMock;

    @Test
    public void creation(){
        final FrameSequencer<String> frameSequencer = new FrameSequencer<>(1, new String[0]);
        assertNotNull(frameSequencer);
    }

    @Test
    public void outputsEvent(){
        final FrameSequencer<String> frameSequencer = new FrameSequencer<>(1, new String[] {"A"});
        frameSequencer.addEventWatcher(eventWatcherMock);

        frameSequencer.tick();

        verify(eventWatcherMock, times(1)).eventNotification("A");
    }

    @Test
    public void noEventsOutputOutsideOfDividedRange(){
        final FrameSequencer<String> frameSequencer = new FrameSequencer<>(2, new String[] {"A"});
        frameSequencer.addEventWatcher(eventWatcherMock);

        frameSequencer.tick();

        verify(eventWatcherMock, never()).eventNotification(any());
    }

    @Test
    public void eventsOutputAfterDividedRange(){
        final FrameSequencer<String> frameSequencer = new FrameSequencer<>(2, new String[] {"A"});
        frameSequencer.addEventWatcher(eventWatcherMock);

        ticks(frameSequencer, 2);

        verify(eventWatcherMock, times(1)).eventNotification("A");
    }

    @Test
    public void sequenceWrapsAndNullsNotOutput(){
        final FrameSequencer<String> frameSequencer = new FrameSequencer<>(5, new String[] {"A",null,"B", null, "C"});
        frameSequencer.addEventWatcher(eventWatcherMock);

        ticks(frameSequencer, 30);

        verify(eventWatcherMock, times(4)).eventNotification(any(String.class));
        verify(eventWatcherMock, times(2)).eventNotification("A");
        verify(eventWatcherMock, times(1)).eventNotification("B");
        verify(eventWatcherMock, times(1)).eventNotification("C");
    }

    //Helper method to do multiple ticks
    private void ticks(FrameSequencer<String> sequencer, int ticks){
        for (int tick=0; tick<ticks; tick++)
            sequencer.tick();
    }
}
