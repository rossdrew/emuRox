package com.rox.emu.processor.ricoh2c02;

import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import org.junit.Test;

import static com.rox.emu.processor.ricoh2c02.Ricoh2C02Registers.Register.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class Ricoh2C02RegistersTest {
    @Test
    public void testSimpleRead(){
        final Memory cpuMemory = mock(SimpleMemory.class);
        final Ricoh2C02Registers registers = new Ricoh2C02Registers(cpuMemory);

        when(cpuMemory.getByte(REG_CTRL_1.getMemoryMappedLocation())).thenReturn(42);

        assertEquals(42, registers.getRegister(REG_CTRL_1));
    }

    @Test
    public void testSimpleWrite(){
        final Memory cpuMemory = mock(SimpleMemory.class);
        final Ricoh2C02Registers registers = new Ricoh2C02Registers(cpuMemory);

        registers.setRegister(REG_CTRL_1, 99);

        verify(cpuMemory, times(1)).setByteAt(REG_CTRL_1.getMemoryMappedLocation(), 99);
    }
}
