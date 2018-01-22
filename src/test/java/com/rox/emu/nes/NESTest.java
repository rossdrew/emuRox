package com.rox.emu.nes;

import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.ricoh2c02.Ricoh2C02;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NESTest {
    private Mos6502 cpu;
    private Ricoh2C02 ppu;
    private Memory mem;

    private NES nes;

    @Before
    public void setup(){
        cpu = mock(Mos6502.class);
        ppu = mock(Ricoh2C02.class);
        mem = mock(Memory.class);

        nes = new NES(cpu, ppu, mem);
    }

    @Test
    public void testCreation(){
        assertNotNull(nes);
    }

    @Test
    public void testReset(){
        nes.reset();

        verify(mem, times(1)).setBlock(0xFFFC, new int[] {0x80, 0x00});
    }
}
