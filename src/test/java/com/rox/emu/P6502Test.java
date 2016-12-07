package com.rox.emu;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
    public void testLDA_I(){
        int[] program = {P6502.OPCODE_LDA_I, 0xAA};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0xAA, registers[P6502.ACC_REG]);
        assertEquals(0x0, registers[P6502.STATUS_FLAGS_REG] & 0);
        assertEquals(processor.getPC(), 2);
    }

    @Test
    public void testLDA_I_withZeroResult(){
        int[] program = {P6502.OPCODE_LDA_I, 0x0};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x0, registers[P6502.ACC_REG]);
        assertEquals(P6502.STATUS_FLAG_ZERO, registers[P6502.STATUS_FLAGS_REG] & P6502.STATUS_FLAG_ZERO);
        assertEquals(processor.getPC(), 2);
    }

    @Test
    public void testLDA_I_withNegativeResult(){
        int[] program = {P6502.OPCODE_LDA_I, 0x80};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x80, registers[P6502.ACC_REG]);
        assertEquals(P6502.STATUS_FLAG_NEGATIVE, registers[P6502.STATUS_FLAGS_REG] & P6502.STATUS_FLAG_NEGATIVE);
        assertEquals(processor.getPC(), 2);
    }

    @Test
    public void testADC_I(){
        int[] program = {P6502.OPCODE_LDA_I,
                         0x1,
                         P6502.OPCODE_ADC_I,
                         0x1};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x2, registers[P6502.ACC_REG]);  //Accumulator is 0x2 == (0x1 + 0x1) == (mem[0x1] + mem[0x3])
        assertEquals(processor.getPC(), 4);
        assertEquals(0x0, registers[P6502.STATUS_FLAGS_REG] & 0);
    }

    @Test
    public void testADC_I_withZeroResult(){
        int[] program = {P6502.OPCODE_LDA_I,
                         0x0,
                         P6502.OPCODE_ADC_I,
                         0x0};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x0, registers[P6502.ACC_REG]);
        assertEquals(processor.getPC(), 4);
        assertEquals(P6502.STATUS_FLAG_ZERO, registers[P6502.STATUS_FLAGS_REG] & P6502.STATUS_FLAG_ZERO);
    }

}
