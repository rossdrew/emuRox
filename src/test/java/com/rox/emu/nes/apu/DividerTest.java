package com.rox.emu.nes.apu;

import com.rox.emu.timing.ClockWatcher;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class DividerTest {
    @Test
    public void createDivider(){
        final Divider divider = new Divider(1);
        assertNotNull(divider);
    }

    @Test
    public void acceptsWatchers() {
        final Divider divider = new Divider(1);
        final ClockWatcher mockClockWatcher = mock(ClockWatcher.class);
        divider.addClockWatcher(mockClockWatcher);

        verify(mockClockWatcher, never()).tick();
    }

    @Test
    public void passesTickOn() {
        final Divider divider = new Divider(1);
        final ClockWatcher mockClockWatcher = mock(ClockWatcher.class);
        divider.addClockWatcher(mockClockWatcher);

        divider.tick();

        verify(mockClockWatcher, times(1)).tick();
    }

    @Test
    public void noTicksTillThreshold() {
        final int testedPeriod = 3;
        final Divider divider = new Divider(testedPeriod);
        final ClockWatcher mockClockWatcher = mock(ClockWatcher.class);
        divider.addClockWatcher(mockClockWatcher);

        for (int tickNumber=0; tickNumber<testedPeriod; tickNumber++){
            verify(mockClockWatcher, never()).tick();
            divider.tick();
        }

        verify(mockClockWatcher, times(1)).tick();
    }

    @Test
    public void tickOnEveryThreshold() {
        final int testedPeriod = 3;
        final int testedThresholds = 10;
        final Divider divider = new Divider(testedPeriod);
        final ClockWatcher mockClockWatcher = mock(ClockWatcher.class);
        divider.addClockWatcher(mockClockWatcher);

        verify(mockClockWatcher, never()).tick();
        for (int threshold=0; threshold < testedThresholds; threshold++){
            ticks(divider, testedPeriod);

            verify(mockClockWatcher, times(threshold+1)).tick();
        }
    }

    @Test
    public void testPeriodChangeEffectOnCurrentCycle() {
        final int testedPeriod = 3;
        final Divider divider = new Divider(testedPeriod);
        final ClockWatcher mockClockWatcher = mock(ClockWatcher.class);
        divider.addClockWatcher(mockClockWatcher);

        ticks(divider, 2);
        divider.changePeriod(100);
        divider.tick();

        verify(mockClockWatcher, times(1)).tick();
    }

    @Test
    public void testPeriodChangeEffectOnFollowingCycle() {
        final int testedPeriod = 3;
        final Divider divider = new Divider(testedPeriod);
        final ClockWatcher mockClockWatcher = mock(ClockWatcher.class);
        divider.addClockWatcher(mockClockWatcher);

        ticks(divider, 2);
        divider.changePeriod(10);
        divider.tick();
        verify(mockClockWatcher, times(1)).tick();
        ticks(divider,10);
        verify(mockClockWatcher, times(2)).tick();
    }

    @Test
    public void resetWorks() {
        final int testedPeriod = 3;
        final Divider divider = new Divider(testedPeriod);
        final ClockWatcher mockClockWatcher = mock(ClockWatcher.class);
        divider.addClockWatcher(mockClockWatcher);

        ticks(divider, 2);
        divider.reset();
        divider.tick();
        verify(mockClockWatcher, never()).tick();
        ticks(divider, 4);
        verify(mockClockWatcher, times(1)).tick();
    }

    //Helper method to do multiple ticks
    private void ticks(Divider divider, int ticks){
        for (int tick=0; tick<ticks; tick++)
            divider.tick();
    }
}
