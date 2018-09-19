package com.rox.emu.processor.mos6502.op.util;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.op.Mos6502AddressingMode;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

public class AddressingModeTest {
    @Test
    public void testValues(){
        for (Mos6502AddressingMode addressingMode : Mos6502AddressingMode.values()){
            final String description = addressingMode.getDescription();
            final int bytes = addressingMode.getInstructionBytes();

            assertFalse(description==null || description.isEmpty());
            assertTrue(bytes > 0);
        }
    }

    @Test
    public void testConversionToXIndex(){
        assertEquals(Mos6502AddressingMode.ZERO_PAGE_X, Mos6502AddressingMode.ZERO_PAGE.xIndexed());
        assertEquals(Mos6502AddressingMode.ABSOLUTE_X, Mos6502AddressingMode.ABSOLUTE.xIndexed());
        assertEquals(Mos6502AddressingMode.INDIRECT_X, Mos6502AddressingMode.INDIRECT.xIndexed());
    }

    @Test
    public void testInvalidConversionToXIndex(){
        try {
            Mos6502AddressingMode.ACCUMULATOR.xIndexed();
            fail("Accumulator cannot be X indexed");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testConversionToYIndex(){
        assertEquals(Mos6502AddressingMode.ZERO_PAGE_Y, Mos6502AddressingMode.ZERO_PAGE.yIndexed());
        assertEquals(Mos6502AddressingMode.ABSOLUTE_Y, Mos6502AddressingMode.ABSOLUTE.yIndexed());
        assertEquals(Mos6502AddressingMode.INDIRECT_Y, Mos6502AddressingMode.INDIRECT.yIndexed());
    }

    @Test
    public void testInvalidConversionToYIndex(){
        try {
            Mos6502AddressingMode.ACCUMULATOR.yIndexed();
            fail("Accumulator cannot be Y indexed");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testToStringIsACompleteDescription(){
        for (Mos6502AddressingMode addressingMode : Mos6502AddressingMode.values()) {
            for (String descriptiveWord : addressingMode.name().split("_")) {
                assertTrue(addressingMode.toString().toLowerCase().contains(descriptiveWord.toLowerCase()));
            }
        }
    }
}
