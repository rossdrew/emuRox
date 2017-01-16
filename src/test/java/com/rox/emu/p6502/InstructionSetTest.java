package com.rox.emu.p6502;

import org.junit.Test;

import static org.junit.Assert.*;

public class InstructionSetTest {
    @Test
    public void testGetValidName() throws Exception {
        for (int i : InstructionSet.instructionSet){
            assertFalse("Instruction " + i + " has no textual description", InstructionSet.getOpCodeName(i).startsWith("<Unknown Opcode"));
        }
    }

    @Test
    public void testGetInvalidName() throws Exception {
        assertTrue("Instruction 256 should be invalid", InstructionSet.getOpCodeName(256).startsWith("<Unknown Opcode"));

    }
}