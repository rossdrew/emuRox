package com.rox.emu.P6502;

import com.rox.emu.UnknownOpCodeException;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static com.rox.emu.P6502.InstructionSet.*;
import static org.spockframework.util.Assert.fail;

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
        memory = new int[65534];
        memory[0xFFFC] = 0x1;
        memory[0xFFFD] = 0x1;

        processor = new CPU(memory);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertEquals(0x34, registers.getRegister(registers.REG_STATUS)); //Status flags reset
        assertEquals(0x1, registers.getRegister(registers.REG_PC_LOW));  //PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(0x1, registers.getRegister(registers.REG_PC_HIGH)); // ...
        assertEquals(0xFF, registers.getRegister(registers.REG_SP));     //Stack Pointer at top of stack
    }

    @Test
    public void testLDA(){
        int[] program = {OP_LDA_I, 0xAA};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(registers.REG_STATUS) & 0);
        assertEquals(2, registers.getPC());
    }

    @Test
    public void testLDX(){
        int[] program = {OP_LDX_I, 0xAA};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(registers.REG_X_INDEX));
        assertEquals(0x0, registers.getRegister(registers.REG_STATUS) & 0);
        assertEquals(2, registers.getPC());
    }

    @Test
    public void testLDY(){
        int[] program = {OP_LDY_I, 0xAA};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(registers.REG_Y_INDEX));
        assertEquals(0x0, registers.getRegister(registers.REG_STATUS) & 0);
        assertEquals(2, registers.getPC());
    }

    @Test
    public void testADC(){
        int[] program = {OP_LDA_I,
                         0x1,
                         OP_ADC_I,
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
    public void testAND(){
        int[] program = {OP_LDA_I,
                0b00000101,
                OP_AND_I,
                0b00000101};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0b00000101, registers.getRegister(registers.REG_ACCUMULATOR));
        assertEquals(4, registers.getPC());
        assertEquals(0x0, registers.getRegister(registers.REG_STATUS) & 0);
    }

    @Test
    public void testOR(){
        int[] program = {OP_LDA_I,
                0b00010101,
                OP_OR_I,
                0b00000101};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0b00010101, registers.getRegister(registers.REG_ACCUMULATOR));
        assertEquals(4, registers.getPC());
        assertEquals(0x0, registers.getRegister(registers.REG_STATUS) & 0);
    }

    @Test
    public void testEOR(){
        int[] program = {OP_LDA_I,
                0b00010101,
                OP_EOR_I,
                0b00000101};
        System.arraycopy(program, 0, memory, 0, program.length);

        processor.step();
        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0b00010000, registers.getRegister(registers.REG_ACCUMULATOR));
        assertEquals(4, registers.getPC());
        assertEquals(0x0, registers.getRegister(registers.REG_STATUS) & 0);
    }

    @Test
    public void testSEC(){
        int[] program = {OP_SEC};
        System.arraycopy(program, 0, memory, 0, program.length);
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(1, registers.getPC());
        assertEquals(true, registers.getStatusFlags()[0]);
    }

    @Test
    public void testCLC(){
        int[] program = {OP_CLC};
        System.arraycopy(program, 0, memory, 0, program.length);
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(1, registers.getPC());
        assertEquals(false, registers.getStatusFlags()[0]);
    }

    @Test
    public void testInvalidOpCode(){
        int[] program = {999};
        System.arraycopy(program, 0, memory, 0, program.length);

        try {
            processor.step();
            fail("Invalid opCode exception expected!");
        }catch(UnknownOpCodeException e){
            assertEquals(999, e.getOpCode());
        }
    }
}
