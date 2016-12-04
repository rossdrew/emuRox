package com.rox.emu;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

public class P6502Test {
    int [] memory;
    P6502 processor;

    @Before
    public void setup(){
        memory = new int[65534];
        memory[0xFFFC] = 0x0;
        memory[0xFFFD] = 0x0;

        processor = new P6502(memory);
        processor.reset();
    }

    @Test
    public void testStartup(){
        int[] registers = processor.getRegisters();

        assertThat(registers.length, is(8));
        assertEquals(0x34, registers[P6502.STATUS_FLAGS_REG]); //Status flags reset
        assertEquals(0x0, registers[P6502.PC_LO_REG]);         //PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(0x0, registers[P6502.PC_HI_REG]);
        assertEquals(0xFF, registers[P6502.SP_REG]);           //Stack Pointer at top of stack
    }

    @Test
    public void testImmediateLoadAccumulator(){
        memory[0x0] = P6502.OPCODE_LDA_I;
        memory[0x1] = 0xAA;
        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0xAA, registers[P6502.ACC_REG]);       //is the Accumulator loaded with desired value
        assertTrue(registers[P6502.STATUS_FLAGS_REG] < 64); //== 00xx xxxx
        assertEquals(processor.getPC(), 2);
    }

    @Test
    public void testImmediateAddToAccumulator(){
        memory[0x0] = P6502.OPCODE_LDA_I;
        memory[0x1] = 0x1;
        memory[0x3] = P6502.OPCODE_ADC_I;
        memory[0x4] = 0x1;
        processor.step();
        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x2, registers[P6502.ACC_REG]);  //Accumulator is 0x2 == (0x1 + 0x1) == (mem[0x1] + mem[0x4])
        //TODO PC is 5
        //TODO N,Z,C,V are correct
    }
}
