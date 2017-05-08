package com.rox.emu.p6502.op;

import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

public class AddressingModeTest {
    @Test
    public void testValues(){
        for (AddressingMode addressingMode : AddressingMode.values()){
            final String description = addressingMode.getDescription();
            final int bytes = addressingMode.getInstructionBytes();

            assertFalse(description==null || description.isEmpty());
            assertTrue(bytes > 0);
        }
    }

    @Test
    public void testConversionToXIndex(){
        assertEquals(AddressingMode.ZERO_PAGE_X, AddressingMode.ZERO_PAGE.xIndexed());
        assertEquals(AddressingMode.ABSOLUTE_X, AddressingMode.ABSOLUTE.xIndexed());
        assertEquals(AddressingMode.INDIRECT_X, AddressingMode.INDIRECT.xIndexed());
    }

    @Test
    public void testInvalidConversionToXIndex(){
        try {
            AddressingMode.ACCUMULATOR.xIndexed();
            fail("Accumulator cannot be X indexed");
        }catch(RuntimeException e){
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testConversionToYIndex(){
        assertEquals(AddressingMode.ZERO_PAGE_Y, AddressingMode.ZERO_PAGE.yIndexed());
        assertEquals(AddressingMode.ABSOLUTE_Y, AddressingMode.ABSOLUTE.yIndexed());
        assertEquals(AddressingMode.INDIRECT_Y, AddressingMode.INDIRECT.yIndexed());
    }

    @Test
    public void testInvalidConversionToYIndex(){
        try {
            AddressingMode.ACCUMULATOR.yIndexed();
            fail("Accumulator cannot be Y indexed");
        }catch(RuntimeException e){
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }
}
