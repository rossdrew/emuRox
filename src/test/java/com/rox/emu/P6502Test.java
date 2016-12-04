package com.rox.emu;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

public class P6502Test {
    @Test
    public void testStartup(){
        int[] memory = new int[65534];
        memory[0xFFFC] = 0x0;
        memory[0xFFFD] = 0x1;
        P6502 processor = new P6502(memory);
        processor.reset();                                      //6 cycles

        int[] registers = processor.getRegisters();

        assertThat(registers.length, is(8));
        assertEquals(0x34, registers[P6502.STATUS_FLAGS_REG]); //Status flags reset
        assertEquals(0x1, registers[P6502.PC_LO_REG]);         //PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(0x0, registers[P6502.PC_HI_REG]);
        assertEquals(0xFF, registers[P6502.SP_REG]);
    }

    @Test
    public void testImmediateLoadAccumulator(){
        int[] memory = new int[65534];
        memory[0xFFFC] = 0x0;
        memory[0xFFFD] = 0x1;
        P6502 processor = new P6502(memory);
        processor.reset();                      //6 cycles

        memory[0x1] = P6502.OPCODE_ADC_I;       //load LDA Immediate instruction (0xA9) into mem
        memory[0x2] = 0xAA;
        processor.step();                       //clock step to execute

        int[] registers = processor.getRegisters();
        assertEquals(0xAA, registers[P6502.ACC_REG]);       //is the Accumulator loaded with desired value
        assertTrue(registers[P6502.STATUS_FLAGS_REG] < 64); //== 00xx xxxx
        assertEquals(processor.getPC(), 3);
    }
}
