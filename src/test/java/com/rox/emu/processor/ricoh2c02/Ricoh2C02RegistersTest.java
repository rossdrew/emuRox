package com.rox.emu.processor.ricoh2c02;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
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

        RoxWord location = RoxWord.fromLiteral(CTRL_1.getMemoryMappedLocation());
        when(cpuMemory.getByte(location)).thenReturn(RoxByte.fromLiteral(42));

        assertEquals(RoxByte.fromLiteral(42), registers.getRegister(CTRL_1));
    }

    @Test
    public void testSimpleWrite(){
        final Memory cpuMemory = mock(SimpleMemory.class);
        final Ricoh2C02Registers registers = new Ricoh2C02Registers(cpuMemory);

        registers.setRegister(CTRL_1, RoxByte.fromLiteral(99));

        RoxWord location = RoxWord.fromLiteral(CTRL_1.getMemoryMappedLocation());
        verify(cpuMemory, times(1)).setByteAt(location, RoxByte.fromLiteral(99));
    }
}
