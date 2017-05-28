package com.rox.emu.processor.ppu;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnitQuickcheck.class)
public class PPUTest {
    @Test
    public void testCreation(){
        final PPU ppu = new PPU(new SimpleMemory());
        assertNotNull(ppu);
    }

    @Property(trials = 10)
    public void testControlRegister1(@InRange(min = "0", max = "255") int byteValue){
        final Memory mem = new SimpleMemory();
        final PPU ppu = new PPU(mem);

        mem.setByteAt(0x2000, byteValue);

        assertEquals(byteValue, ppu.getRegister(PPU.Register.REG_CTRL_1));
    }

    @Property(trials = 10)
    public void testControlRegister2(@InRange(min = "0", max = "255") int byteValue){
        final Memory mem = new SimpleMemory();
        final PPU ppu = new PPU(mem);

        mem.setByteAt(0x2001, byteValue);

        assertEquals(byteValue, ppu.getRegister(PPU.Register.REG_CTRL_2));
    }
}
