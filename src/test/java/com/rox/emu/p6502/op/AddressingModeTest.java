package com.rox.emu.p6502.op;

import org.junit.Test;

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
}