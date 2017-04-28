package com.rox.emu.p6502.op;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void testConversionToYIndex(){
        assertEquals(AddressingMode.ZERO_PAGE_Y, AddressingMode.ZERO_PAGE.yIndexed());
        assertEquals(AddressingMode.ABSOLUTE_Y, AddressingMode.ABSOLUTE.yIndexed());
        assertEquals(AddressingMode.INDIRECT_Y, AddressingMode.INDIRECT.yIndexed());
    }
}
