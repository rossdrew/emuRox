package com.rox.emu.P6502;

import com.rox.emu.P6502.CPU;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CPUTest {
    int [] memory;
    CPU processor;

    @Before
    public void setup(){
        memory = new int[65534];
        memory[0xFFFC] = 0x0;
        memory[0xFFFD] = 0x0;

        processor = new CPU(memory);
        processor.reset();
    }

    @Test
    public void testStartup(){
        int[] registers = processor.getRegisters();

        assertThat(registers.length, is(8));
        assertEquals(0x34, registers[CPU.REG_STATUS]); //Status flags reset
        assertEquals(0x0, registers[CPU.REG_PC_LOW]);         //PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(0x0, registers[CPU.REG_PC_HIGH]);
        assertEquals(0xFF, registers[CPU.REG_SP]);           //Stack Pointer at top of stack
    }

    @Test
    public void testLDA_I(){
        int[] program = {CPU.OP_LDA_I, 0xAA};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0xAA, registers[CPU.REG_ACCUMULATOR]);
        assertEquals(0x0, registers[CPU.REG_STATUS] & 0);
        assertEquals(processor.getPC(), 2);
    }

    @Test
    public void testLDA_I_withZeroResult(){
        int[] program = {CPU.OP_LDA_I, 0x0};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x0, registers[CPU.REG_ACCUMULATOR]);
        assertEquals(CPU.STATUS_FLAG_ZERO, registers[CPU.REG_STATUS] & CPU.STATUS_FLAG_ZERO);
        assertEquals(processor.getPC(), 2);
    }

    @Test
    public void testLDA_I_withNegativeResult(){
        int[] program = {CPU.OP_LDA_I, 0x80};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x80, registers[CPU.REG_ACCUMULATOR]);
        assertEquals(CPU.STATUS_FLAG_NEGATIVE, registers[CPU.REG_STATUS] & CPU.STATUS_FLAG_NEGATIVE);
        assertEquals(processor.getPC(), 2);
    }

    @Test
    public void testADC_I(){
        int[] program = {CPU.OP_LDA_I,
                         0x1,
                         CPU.OP_ADC_I,
                         0x1};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x2, registers[CPU.REG_ACCUMULATOR]);  //Accumulator is 0x2 == (0x1 + 0x1) == (mem[0x1] + mem[0x3])
        assertEquals(processor.getPC(), 4);
        assertEquals(0x0, registers[CPU.REG_STATUS] & 0);
    }

    @Test
    public void testADC_I_withZeroResult(){
        int[] program = {CPU.OP_LDA_I,
                0x0,
                CPU.OP_ADC_I,
                0x0};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x0, registers[CPU.REG_ACCUMULATOR]);
        assertEquals(processor.getPC(), 4);
        assertEquals(CPU.STATUS_FLAG_ZERO, registers[CPU.REG_STATUS] & CPU.STATUS_FLAG_ZERO);
    }

    @Test
    public void testADC_I_withNegativeResult(){
        int[] program = {CPU.OP_LDA_I,
                0x7F,
                CPU.OP_ADC_I,
                0x1};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0x80, registers[CPU.REG_ACCUMULATOR]);
        assertEquals(processor.getPC(), 4);
        assertEquals(CPU.STATUS_FLAG_NEGATIVE, registers[CPU.REG_STATUS] & CPU.STATUS_FLAG_NEGATIVE);
    }

    @Test
    public void testADC_I_withCarry(){
        int[] program = {CPU.OP_LDA_I,
                0xFF,
                CPU.OP_ADC_I,
                0xFF};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        int[] registers = processor.getRegisters();
        assertEquals(0xFE, registers[CPU.REG_ACCUMULATOR]);
        assertEquals(processor.getPC(), 4);
        assertEquals(CPU.STATUS_FLAG_CARRY, registers[CPU.REG_STATUS] & CPU.STATUS_FLAG_CARRY);
    }

}
