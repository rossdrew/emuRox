package com.rox.emu.p6502;

import com.rox.emu.Memory;
import com.rox.emu.SimpleMemory;
import com.rox.emu.UnknownOpCodeException;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static com.rox.emu.p6502.InstructionSet.*;
import static org.spockframework.util.Assert.fail;

public class CPUTest {
    private Memory memory;
    private CPU processor;

    @Before
    public void setUp() {
        memory = new SimpleMemory(65534);
        memory.setByteAt(0x0, 0xFFFC);
        memory.setByteAt(0x0, 0xFFFD);

        processor = new CPU(memory);
        processor.reset();
    }

    @Test
    public void testStartup() {
        memory = new SimpleMemory(65534);
        memory.setByteAt(0xFFFC, 0x1);
        memory.setByteAt(0xFFFD, 0x1);

        processor = new CPU(memory);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertEquals(0x34, registers.getRegister(Registers.REG_STATUS)); //Status flags reset
        assertEquals(0x1, registers.getRegister(Registers.REG_PC_LOW));  //PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(0x1, registers.getRegister(Registers.REG_PC_HIGH)); // ...
        assertEquals(0xFF, registers.getRegister(Registers.REG_SP));     //Stack Pointer at top of stack

        assertEquals(0, registers.getRegister(Registers.REG_ACCUMULATOR)); //All cleared
        assertEquals(0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testReset() {
        int[] program = {OP_LDA_I, 0xAA, OP_LDX_I, 0xBB, OP_LDX_I, 0xCC};
        memory.setMemory(0, program);
        memory.setByteAt(0xFFFC, 0x0);
        memory.setByteAt(0xFFFD, 0x0);

        processor.step(3);
        processor.reset();
        Registers registers = processor.getRegisters();

        assertEquals(0x34, registers.getRegister(Registers.REG_STATUS));
        assertEquals(0x0, registers.getRegister(Registers.REG_PC_LOW));
        assertEquals(0x0, registers.getRegister(Registers.REG_PC_HIGH));
        assertEquals(0xFF, registers.getRegister(Registers.REG_SP));
        assertEquals(0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testLDA() {
        int[] program = {OP_LDA_I, 0xAA};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, 0);
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testSTA() {
        int[] program = {OP_LDA_I, 0xAA, OP_STA_Z, 100};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, memory.getByte(100));
    }

    @Test
    public void testAccumulatorSTA() {
        int[] program = {OP_LDA_I, 0xAA, OP_STA_ABS, 0xFF, 0x01};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, memory.getByte(0xFF01));
    }

    @Test
    public void testZAtXIndexSTA() {
        int[] program = {OP_LDA_I, 0xAA, OP_LDX_I, 0x1, OP_STA_Z_IX, 0x20};
        memory.setMemory(0, program);

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, memory.getByte(0x21));
    }

    @Test
    public void testSTX() {
        int[] program = {OP_LDX_I, 0xAA, OP_STX_Z, 100};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, memory.getByte(100));
    }

    @Test
    public void testSTX_ABS() {
        int[] program = {OP_LDX_I, 0xAA, OP_STX_ABS, 0x02, 0x20};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, memory.getByte(0x220));
    }

    @Test
    public void testSTY() {
        int[] program = {OP_LDY_I, 0xAA, OP_STY_Z, 100};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, memory.getByte(100));
    }

    @Test
    public void testSTY_ABS() {
        int[] program = {OP_LDY_I, 0xAA, OP_STY_ABS, 0x02, 0x20};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, memory.getByte(0x220));
    }

    @Test
    public void testLDX() {
        int[] program = {OP_LDX_I, 0xAA};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x0, 0);
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testLDY() {
        int[] program = {OP_LDY_I, 0xAA};
        memory.setMemory(0, program);

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(0x0, 0);
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testADC() {
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
    public void testADCWithCarry() {
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
    public void testSBC() {
        int[] program = {OP_SEC, OP_LDA_I, 0xA, OP_SBC_I, 0x5};
        memory.setMemory(0, program);
        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x5, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testSBCWithCarry() {
        int[] program = {OP_CLC, OP_LDA_I, 0xA, OP_SBC_I, 0x5};
        memory.setMemory(0, program);
        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x4, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void testAND() {
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
    public void testOR() {
        int[] program = {OP_LDA_I,
                0b00010101,
                OP_ORA_I,
                0b00000101};
        memory.setMemory(0, program);

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(0b00010101, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testEOR() {
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
    public void testSEC() {
        int[] program = {OP_SEC};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(true, registers.getStatusFlags()[0]);
    }

    @Test
    public void testCLC() {
        int[] program = {OP_SEC, OP_CLC};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getFlag(Registers.STATUS_FLAG_CARRY));
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(false, registers.getStatusFlags()[0]);
    }

    @Test
    public void testCLV() {
        int[] program = {OP_LDA_I, 0x50, OP_ADC_I, 0x50, OP_CLV};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);
        assert (processor.getRegisters().getStatusFlags()[Registers.V]);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(false, registers.getStatusFlags()[Registers.V]);
    }

    @Test
    public void testINX() {
        int[] program = {OP_LDX_I, 0x01, OP_INX};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_X_INDEX) == 1);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(2, processor.getRegisters().getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testINY() {
        int[] program = {OP_LDY_I, 0x01, OP_INY};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_Y_INDEX) == 1);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(2, processor.getRegisters().getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testINC() {
        int[] program = {OP_LDA_I, 1, OP_STA_Z, 0x20, OP_INC_Z, 0x20};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(2, memory.getByte(0x20));
    }

    @Test
    public void testDEC() {
        int[] program = {OP_LDA_I, 9, OP_STA_Z, 0x20, OP_DEC_Z, 0x20};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(8, memory.getByte(0x20));
    }

    @Test
    public void testDEY() {
        int[] program = {OP_LDY_I, 0x01, OP_DEY};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_Y_INDEX) == 1);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(0, processor.getRegisters().getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testDEX() {
        int[] program = {OP_LDX_I, 0x01, OP_DEX};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_X_INDEX) == 1);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(0, processor.getRegisters().getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testInvalidOpCode() {
        int[] program = {999};
        memory.setMemory(0, program);

        try {
            processor.step();
            fail("Invalid opCode exception expected!");
        } catch (UnknownOpCodeException e) {
            assertEquals(999, e.getOpCode());
        }
    }

    @Test
    public void testPHA() {
        int[] program = {OP_LDA_I, 0x99, OP_PHA};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_SP) == 0xFF);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(0xFE, processor.getRegisters().getRegister(Registers.REG_SP));
        assertEquals(0x99, memory.getByte(0x01FF));
    }

    @Test
    public void testPLA() {
        int[] program = {OP_LDA_I, 0x99, OP_PHA, OP_LDA_I, 0x11, OP_PLA};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);
        assert (processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR) == 0x99);
        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR) == 0x11);
        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(0xFF, processor.getRegisters().getRegister(Registers.REG_SP));
        assertEquals(0x99, processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testASL() {
        int[] program = {OP_LDA_I, 0b01010101, OP_ASL_A};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals("Expected 10101010, got " + Integer.toBinaryString(processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR)),
                0b10101010, processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testLSR(){
        int[] program = {OP_LDA_I, 0b01011010, OP_LSR_A};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals("Expected 00101101, got " + Integer.toBinaryString(processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR)),
                0b00101101, processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testNOP(){
        int[] program = {OP_NOP, OP_NOP, OP_NOP};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        for (int i=1; i<=program.length; i++){
            processor.step();
            assertEquals(i, registers.getPC());
        }
    }

    @Test
    public void testJMP(){
        int[] program = {OP_LDX_I, 0x8,
                         OP_JMP_ABS, 0x0, 0x7,
                         OP_LDY_I, 0x9,
                         OP_LDA_I, 0x10};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x8, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x0, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(0x10, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testBCC(){
        int[] program = {OP_CLC, OP_BCC, 0x4, OP_LDA_I, 0x99, OP_LDX_I, 0x98, OP_LDY_I, 0x97};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBCS(){
        int[] program = {OP_SEC, OP_BCS, 0x4, OP_LDA_I, 0x99, OP_LDX_I, 0x98, OP_LDY_I, 0x97};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBNE(){
        int[] program = {OP_LDA_I, 0x1, OP_BNE, 0x4, OP_LDA_I, 0x99, OP_LDX_I, 0x98, OP_LDY_I, 0x97};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x1, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBEQ(){
        int[] program = {OP_LDA_I, 0x0, OP_BEQ, 0x4, OP_LDA_I, 0x99, OP_LDX_I, 0x98, OP_LDY_I, 0x97};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testROL(){
        int[] program = {OP_SEC, OP_LDA_I, 0b00000001, OP_ROL_A};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b00000011, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testROR(){
        int[] program = {OP_SEC, OP_LDA_I, 0b00000010, OP_ROR_A};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b10000001, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testBMI(){
        int[] program = {OP_LDA_I, 0b11111110, OP_BMI, 0x4, OP_LDA_I, 0x99, OP_LDX_I, 0x98, OP_LDY_I, 0x97};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b11111110, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBPL(){
        int[] program = {OP_LDA_I, 0b00000001, OP_BPL, 0x4, OP_LDA_I, 0x99, OP_LDX_I, 0x98, OP_LDY_I, 0x97};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b00000001, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBVS(){
        int[] program = {OP_LDA_I, 0x50, OP_ADC_I, 0x50, OP_BVS, 0x4, OP_LDA_I, 0x99, OP_LDX_I, 0x98, OP_LDY_I, 0x97};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(program.length, registers.getPC());
        assertEquals(0xA0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBVC(){
        int[] program = {OP_LDA_I, 0x0, OP_ADC_I, 0x10, OP_BVC, 0x4, OP_LDA_I, 0x99, OP_LDX_I, 0x98, OP_LDY_I, 0x97};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x10, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testTAX(){
        int[] program = {OP_LDA_I, 0x0F, OP_TAX};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x0F, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0F, registers.getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testTAY(){
        int[] program = {OP_LDA_I, 0x0F, OP_TAY};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x0F, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0F, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testTYA(){
        int[] program = {OP_LDY_I, 0x0D, OP_TYA};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x0D, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(0x0D, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testTXA(){
        int[] program = {OP_LDX_I, 0x0D, OP_TXA};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x0D, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x0D, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testTXS(){
        int[] program = {OP_LDX_I, 0xAA, OP_TXS};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals(0xAA, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0xAA, registers.getRegister(Registers.REG_SP));
    }

    @Test
    public void testTSX(){
        int[] program = {OP_TSX};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(program.length, registers.getPC());
        assertEquals(0xFF, registers.getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testBIT(){
        int[] program = {OP_LDA_I, 0x01, OP_STA_Z, 0x20, OP_LDA_I, 0x01, OP_BIT_Z, 0x20};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(program.length, registers.getPC());
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_ZERO));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_OVERFLOW));
    }

    @Test
    public void testCMP(){
        int[] program = {OP_LDA_I, 0x10, OP_CMP_I, 0x10};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x10, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_ZERO));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_CARRY));
    }

    @Test
    public void testCPX(){
        int[] program = {OP_LDX_I, 0x10, OP_CPX_I, 0x10};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x10, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_ZERO));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_CARRY));
    }

    @Test
    public void testCPY(){
        int[] program = {OP_LDY_I, 0x10, OP_CPY_I, 0x10};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.length, registers.getPC());
        assertEquals(0x10, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_ZERO));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_CARRY));
    }

    @Test
    public void testPHP(){
        int[] program = {OP_PHP};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step();

        int stackLoc = (registers.getRegister(Registers.REG_SP) + 1);
        int stackValue = (memory.getByte(0x100 | stackLoc));

        assertEquals(program.length, registers.getPC());
        assertEquals(registers.getRegister(Registers.REG_STATUS), stackValue);
    }

    @Test
    public void testPLP(){
        int[] program = {OP_PHP, OP_PLP};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b11111111);
        processor.step(1);
        registers.setRegister(Registers.REG_STATUS, 0b00000000);
        processor.step(1);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b11111111, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testCLI(){
        int[] program = {OP_CLI};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b00000100);
        processor.step(1);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b00000000, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testSEI(){
        int[] program = {OP_SEI};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b11111011);
        processor.step(1);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b11111111, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testSED(){
        int[] program = {OP_SED};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b11110111);
        processor.step(1);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b11111111, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testCLD(){
        int[] program = {OP_CLD};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b00001000);
        processor.step(1);

        assertEquals(program.length, registers.getPC());
        assertEquals(0b00000000, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testJSR(){
        int[] program = {OP_JSR, 0x02, 0x0F};
        memory.setMemory(0, program);
        Registers registers = processor.getRegisters();

        processor.step(1);

        assertEquals(0x020F, registers.getPC());
        assertEquals(0x02, registers.getRegister(Registers.REG_PC_HIGH)); //Jump address
        assertEquals(0x0F, registers.getRegister(Registers.REG_PC_LOW));

        assertEquals(0x00, memory.getByte(0x1FF)); //Return address
        assertEquals(0x03, memory.getByte(0x1FE));
    }

    @Test
    @Ignore
    public void testMultiplicationLoop(){
        try {
            int data_offset = 0x32;
            int MPD = data_offset + 0x10;
            int MPR = data_offset + 0x11;
            int TMP = data_offset + 0x20;
            int RESAD_0 = data_offset + 0x30;
            int RESAD_1 = data_offset + 0x31;

            int valMPD = 7;
            int valMPR = 4;

            int[] program = {OP_LDA_I, valMPD,
                             OP_STA_Z, MPD,
                             OP_LDA_I, valMPR,
                             OP_STA_Z, MPR,
                             OP_LDA_I, 0,         //<---- start
                             OP_STA_Z, TMP,       //Clear
                             OP_STA_Z, RESAD_0,   //...
                             OP_STA_Z, RESAD_1,   //...
                             OP_LDX_I, 8,         //X counts each bit

                             OP_LSR_Z, MPR,       //:MULT(18) LSR(MPR)
                             OP_BCC, 13,        //Test carry and jump (forward 13) to NOADD

                             OP_LDA_Z, RESAD_0,   //RESAD -> A
                             OP_CLC,              //Prepare to add
                             OP_ADC_Z, MPD,       //+MPD
                             OP_STA_Z, RESAD_0,   //Save result
                             OP_LDA_Z, RESAD_1,   //RESAD+1 -> A
                             OP_ADC_Z, TMP,       //+TMP
                             OP_STA_Z, RESAD_1,   //RESAD+1 <- A
                             OP_ASL_Z, MPD,       //:NOADD(35) ASL(MPD)
                             OP_ROL_Z, TMP,       //Save bit from MPD
                             OP_DEX,              //--X
                             OP_BNE, 0b11100111 //Test equal and jump (back 24) to MULT
            };

            memory.setMemory(0, program);
            Registers registers = processor.getRegisters();

            processor.step(64);

            System.out.println("RESAD = " + Integer.toBinaryString(memory.getByte(RESAD_0)) + "|" + Integer.toBinaryString(memory.getByte(RESAD_1)));
            System.out.println("MPD = " + memory.getByte(MPD));
            System.out.println("MPR = " + memory.getByte(MPR));
            System.out.println("TMP = " + memory.getByte(TMP));
            System.out.println("[A] = " + registers.getRegister(Registers.REG_ACCUMULATOR));
        }catch (Exception e) {
            TestCase.fail();
        }
    }
}
