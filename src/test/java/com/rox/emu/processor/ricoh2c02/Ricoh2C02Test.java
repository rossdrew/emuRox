package com.rox.emu.processor.ricoh2c02;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.rox.emu.processor.ricoh2c02.Ricoh2C02Registers.Register.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(JUnitQuickcheck.class)
public class Ricoh2C02Test {
    @Test
    public void testCreation(){
        final Ricoh2C02 ppu = new Ricoh2C02(new SimpleMemory(), new SimpleMemory());
        assertNotNull(ppu);
    }

    @Property(trials = 10)
    public void testGetControlRegister(@InRange(min = "0", max = "255") int byteValue){
        final Memory vRam = new SimpleMemory();
        final Memory cpuRam = mock(Memory.class);
        final Ricoh2C02 ppu = new Ricoh2C02(vRam, cpuRam);

        for (Ricoh2C02Registers.Register register : Ricoh2C02Registers.Register.values()) {
            when(cpuRam.getByte(register.getMemoryMappedLocation())).thenReturn(byteValue);
            assertEquals(byteValue, ppu.getRegister(register));
        }
    }

    @Property(trials = 10)
    public void testSetControlRegister(@InRange(min = "0", max = "255") int byteValue){
        final Memory vRam = new SimpleMemory();
        final Memory cpuRam = mock(Memory.class);
        final Ricoh2C02 ppu = new Ricoh2C02(vRam, cpuRam);

        for (Ricoh2C02Registers.Register register : Ricoh2C02Registers.Register.values()) {
            ppu.setRegister(register, byteValue);
            verify(cpuRam, times(1)).setByteAt(register.getMemoryMappedLocation(), byteValue);
        }
    }


}
