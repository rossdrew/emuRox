package com.rox.emu.P6502;

import com.rox.emu.P6502.CPU;
import junit.framework.Assert;
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
        Registers registers = processor.getRegisters();

        assertEquals(0x34, registers.getRegister(registers.REG_STATUS)); //Status flags reset
        assertEquals(0x0, registers.getRegister(registers.REG_PC_LOW));  //PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(0x0, registers.getRegister(registers.REG_PC_HIGH)); // ...
        assertEquals(0xFF, registers.getRegister(registers.REG_SP));     //Stack Pointer at top of stack
    }

    @Test
    public void testLDA_I(){
        int[] program = {CPU.OP_LDA_I, 0xAA};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(registers.REG_STATUS) & 0);
        assertEquals(2, registers.getPC());
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

        Registers registers = processor.getRegisters();
        assertEquals(0x2, registers.getRegister(registers.REG_ACCUMULATOR));  //Accumulator is 0x2 == (0x1 + 0x1) == (mem[0x1] + mem[0x3])
        assertEquals(4, registers.getPC());
        assertEquals(0x0, registers.getRegister(registers.REG_STATUS) & 0);
    }

    @Test
    public void testAND_I(){
        //TODO
    }

    @Test
    public void testOR_I(){
        //TODO
    }

    @Test
    public void testEOR_I(){
        //TODO
    }

    @Test
    public void testSEC(){
        int[] program = {CPU.OP_SEC};
        System.arraycopy(program, 0, memory, 0, program.length);
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(1, registers.getPC());
        assertEquals(true, registers.getStatusFlags()[0]);
    }
}