package com.rox.emu.P6502;

import com.rox.emu.Memory;
import com.rox.emu.SimpleMemory;
import com.rox.emu.UnknownOpCodeException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static com.rox.emu.P6502.InstructionSet.*;
import static org.spockframework.util.Assert.fail;

public class CPUTest {
    private Memory memory;
    private CPU processor;

    @Before
    public void setup(){
        memory = new SimpleMemory(65534);
        memory.setByte(0x0, 0xFFFC);
        memory.setByte(0x0, 0xFFFD);

        processor = new CPU(memory);
        processor.reset();
    }

    @Test
    public void testStartup(){
        memory = new SimpleMemory(65534);
        memory.setByte(0xFFFC, 0x1);
        memory.setByte(0xFFFD, 0x1);

        processor = new CPU(memory);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertEquals(0x34, registers.getRegister(Registers.REG_STATUS)); //Status flags reset
        assertEquals(0x1, registers.getRegister(Registers.REG_PC_LOW));  //PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(0x1, registers.getRegister(Registers.REG_PC_HIGH)); // ...
        assertEquals(0xFF, registers.getRegister(Registers.REG_SP));     //Stack Pointer at top of stack
    }

    @Test
    public void testLDA(){
        int[] program = {OP_LDA_I, 0xAA};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, 0);
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testSTA(){
        int[] program = {OP_LDA_I, 0xAA, OP_STA_Z, 100};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, memory.getByte(100));
    }

    @Test
    public void testLDX(){
        int[] program = {OP_LDX_I, 0xAA};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x0, 0);
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testLDY(){
        int[] program = {OP_LDY_I, 0xAA};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(0x0, 0);
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testADC(){
        int[] program = {OP_CLC,
                         OP_LDA_I,
                         0x1,
                         OP_ADC_I,
                         0x1};
        memory.setMemory(0, program);

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x2, registers.getRegister(Registers.REG_ACCUMULATOR));  //Accumulator is 0x2 == (0x1 + 0x1) == (mem[0x1] + mem[0x3])
        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testADCWithCarry(){
        int[] program = {OP_SEC,
                         OP_LDA_I,
                         0x1,
                         OP_ADC_I,
                         0x1};
        memory.setMemory(0, program);

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x3, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testSBC(){
        int[] program = {OP_SEC, OP_LDA_I, 0xA, OP_SBC_I, 0x5};
        memory.setMemory(0, program);
        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x5, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testSBCWithCarry(){
        int[] program = {OP_CLC, OP_LDA_I, 0xA, OP_SBC_I, 0x5};
        memory.setMemory(0, program);
        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x4, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testAND(){
        int[] program = {OP_LDA_I,
                         0b00000101,
                         OP_AND_I,
                         0b00000101};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(0b00000101, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testOR(){
        int[] program = {OP_LDA_I,
                         0b00010101,
                         OP_OR_I,
                         0b00000101};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(0b00010101, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testEOR(){
        int[] program = {OP_LDA_I,
                         0b00010101,
                         OP_EOR_I,
                         0b00000101};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(0b00010000, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testSEC(){
        int[] program = {OP_SEC};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(true, registers.getStatusFlags()[0]);
    }

    @Test
    public void testCLC(){
        int[] program = {OP_SEC, OP_CLC};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert(processor.getRegisters().getFlag(Registers.STATUS_FLAG_CARRY));
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(false, registers.getStatusFlags()[0]);
    }

    @Test
    public void testCLV(){
        int[] program = {OP_LDA_I, 0x50, OP_ADC_I, 0x50, OP_CLV};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);
        assert(processor.getRegisters().getStatusFlags()[Registers.V]);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(false, registers.getStatusFlags()[Registers.V]);
    }

    @Test
    public void testINX(){
        int[] program = {OP_LDX_I, 0x01, OP_INX};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert(processor.getRegisters().getRegister(Registers.REG_X_INDEX) == 1);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(2, processor.getRegisters().getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testINY(){
        int[] program = {OP_LDY_I, 0x01, OP_INY};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert(processor.getRegisters().getRegister(Registers.REG_Y_INDEX) == 1);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(2, processor.getRegisters().getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testDEY(){
        int[] program = {OP_LDY_I, 0x01, OP_DEY};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert(processor.getRegisters().getRegister(Registers.REG_Y_INDEX) == 1);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(0, processor.getRegisters().getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testDEX(){
        int[] program = {OP_LDX_I, 0x01, OP_DEX};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert(processor.getRegisters().getRegister(Registers.REG_X_INDEX) == 1);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(0, processor.getRegisters().getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testInvalidOpCode(){
        int[] program = {999};
        memory.setMemory(0, program);

        try {
            processor.step();
            fail("Invalid opCode exception expected!");
        }catch(UnknownOpCodeException e){
            assertEquals(999, e.getOpCode());
        }
    }

    @Test
    public void testPHA(){
        int[] program = {OP_LDA_I, 0x99, OP_PHA};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        assert(processor.getRegisters().getRegister(Registers.REG_SP) == 0xFF);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(0xFE, processor.getRegisters().getRegister(Registers.REG_SP));
        assertEquals(0x99, memory.getByte(0xFF));
    }
}
