package com.rox.emu.processor.ricoh2c02;

import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class Ricoh2C02Theories {
    @DataPoint
    public static int LOWEST_VALID_REGISTER_VALUE = 0;
    @DataPoint
    public static int HIGHEST_VALID_REGISTER_VALUE = 255;

    @Theory
    public void testGetControlRegister(int byteValue){
        assumeThat(byteValue, is(both(greaterThanOrEqualTo(LOWEST_VALID_REGISTER_VALUE)).and(lessThanOrEqualTo(HIGHEST_VALID_REGISTER_VALUE))));

        final Memory vRam = new SimpleMemory();
        final Memory sprRam = mock(Memory.class);
        final Memory cpuRam = mock(Memory.class);
        final Ricoh2C02 ppu = new Ricoh2C02(vRam, sprRam, cpuRam);

        for (Ricoh2C02Registers.Register register : Ricoh2C02Registers.Register.values()) {
            when(cpuRam.getByte(register.getMemoryMappedLocation())).thenReturn(byteValue);
            assertThat(byteValue, equalTo(ppu.getRegister(register)));
        }
    }
}
