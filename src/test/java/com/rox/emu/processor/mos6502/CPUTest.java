package com.rox.emu.processor.mos6502;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.util.Program;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.rox.emu.processor.mos6502.op.OpCode.*;
import static junit.framework.TestCase.assertEquals;
import static org.spockframework.util.Assert.fail;

public class CPUTest {

    private Memory memory;
    private CPU processor;

    @Before
    public void setUp() {
        memory = new SimpleMemory();
        memory.setByteAt(0xFFFC, 0);
        memory.setByteAt(0xFFFD, 0);

        processor = new CPU(memory);
        processor.reset();
    }

    @Test
    public void testStartup() {
        memory = new SimpleMemory();
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
        Program program = new Program().with(LDA_I, 0xAA,
                                             LDX_I, 0xBB,
                                             LDX_I, 0xCC);
        memory.setBlock(0, program.getProgramAsByteArray());
        memory.setByteAt(0xFFFC, 0x0);
        memory.setByteAt(0xFFFD, 0x0);

        Registers registers = processor.getRegisters();

        registers.setRegister(Registers.REG_STATUS, 0x99);
        registers.setRegister(Registers.REG_PC_LOW, 0x99);
        registers.setRegister(Registers.REG_PC_HIGH, 0x99);
        registers.setRegister(Registers.REG_SP, 0x99);
        registers.setRegister(Registers.REG_ACCUMULATOR, 0x99);
        registers.setRegister(Registers.REG_X_INDEX, 0x99);
        registers.setRegister(Registers.REG_Y_INDEX, 0x99);

        processor.step(3);
        processor.reset();

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
        Program program = new Program().with(LDA_I, 0xAA);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, 0);
        assertEquals(program.getLength(), registers.getPC());
    }

    @Test
    public void testSTA() {
        Program program = new Program().with(LDA_I,
                0xAA, STA_Z, 100);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xAA, memory.getByte(100));
    }

    @Test
    public void testAccumulatorSTA() {
        Program program = new Program().with(LDA_I, 0xAA, STA_ABS, 0xFF, 0x01);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xAA, memory.getByte(0xFF01));
    }

    @Test
    public void testZAtXIndexSTA() {
        Program program = new Program().with(LDA_I, 0xAA, LDX_I, 0x1, STA_Z_IX, 0x20);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xAA, memory.getByte(0x21));
    }

    @Test
    public void testSTX() {
        Program program = new Program().with(LDX_I, 0xAA, STX_Z, 100);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xAA, memory.getByte(100));
    }

    @Test
    public void testSTXAbsolute() {
        Program program = new Program().with(LDX_I, 0xAA, STX_ABS, 0x02, 0x20);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xAA, memory.getByte(0x220));
    }

    @Test
    public void testSTY() {
        Program program = new Program().with(LDY_I, 0xAA, STY_Z, 100);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xAA, memory.getByte(100));
    }

    @Test
    public void testSTYAbsolute() {
        Program program = new Program().with(LDY_I, 0xAA, STY_ABS, 0x02, 0x20);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xAA, memory.getByte(0x220));
    }

    @Test
    public void testLDX() {
        Program program = new Program().with(LDX_I, 0xAA);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x0, 0);
        assertEquals(program.getLength(), registers.getPC());
    }

    @Test
    public void testLDY() {
        Program program = new Program().with(LDY_I, 0xAA);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(0xAA, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(0x0, 0);
        assertEquals(program.getLength(), registers.getPC());
    }

    @Test
    public void testADC() {
        Program program = new Program().with(CLC,
                                             LDA_I,
                                             0x1,
                                             ADC_I,
                                             0x1);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x2, registers.getRegister(Registers.REG_ACCUMULATOR));  //Accumulator is 0x2 == (0x1 + 0x1) == (mem[0x1] + mem[0x3])
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testADCWithCarry() {
        Program program = new Program().with(SEC,
                         LDA_I,
                         0x1,
                         ADC_I,
                         0x1);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x3, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testSBC() {
        Program program = new Program().with(SEC, LDA_I, 0xA, SBC_I, 0x5);
        memory.setBlock(0, program.getProgramAsByteArray());
        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x5, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
    }

    @Test
    public void testSBCWithCarry() {
        Program program = new Program().with(CLC,
                         LDA_I, 0xA,
                         SBC_I, 0x5);
        memory.setBlock(0, program.getProgramAsByteArray());
        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x4, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
    }

    @Test
    public void testAND() {
        Program program = new Program().with(LDA_I,
                0b00000101,
                AND_I,
                0b00000101);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(0b00000101, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testOR() {
        Program program = new Program().with(LDA_I,
                0b00010101,
                ORA_I,
                0b00000101);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(0b00010101, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testEOR() {
        Program program = new Program().with(LDA_I,
                0b00010101,
                EOR_I,
                0b00000101);
        memory.setBlock(0, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(0b00010000, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0, 0);
    }

    @Test
    public void testSEC() {
        Program program = new Program().with(SEC);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(true, registers.getStatusFlags()[0]);
    }

    @Test
    public void testCLC() {
        Program program = new Program().with(SEC, CLC);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getFlag(Registers.STATUS_FLAG_CARRY));
        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(false, registers.getStatusFlags()[0]);
    }

    @Test
    public void testCLV() {
        Program program = new Program().with(LDA_I, 0x50, ADC_I, 0x50, CLV);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);
        assert (processor.getRegisters().getStatusFlags()[Registers.V]);
        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(false, registers.getStatusFlags()[Registers.V]);
    }

    @Test
    public void testINX() {
        Program program = new Program().with(LDX_I, 0x01, INX);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_X_INDEX) == 1);
        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(2, processor.getRegisters().getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testINY() {
        Program program = new Program().with(LDY_I, 0x01, INY);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_Y_INDEX) == 1);
        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(2, processor.getRegisters().getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testINC() {
        Program program = new Program().with(LDA_I, 1, STA_Z, 0x20, INC_Z, 0x20);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(2, memory.getByte(0x20));
    }

    @Test
    public void testDEC() {
        Program program = new Program().with(LDA_I, 9, STA_Z, 0x20, DEC_Z, 0x20);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(8, memory.getByte(0x20));
    }

    @Test
    public void testDEY() {
        Program program = new Program().with(LDY_I, 0x01, DEY);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_Y_INDEX) == 1);
        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0, processor.getRegisters().getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testDEX() {
        Program program = new Program().with(LDX_I, 0x01, DEX);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_X_INDEX) == 1);
        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0, processor.getRegisters().getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testInvalidOpCode() {
        Program program = new Program().with(231);
        memory.setBlock(0, program.getProgramAsByteArray());

        try {
            processor.step();
            fail("Invalid opCode exception expected!");
        } catch (UnknownOpCodeException e) {
            assertEquals(231, e.getOpCode());
        }
    }

    @Test
    public void testPHA() {
        Program program = new Program().with(LDA_I, 0x99, PHA);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_SP) == 0xFF);
        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xFE, processor.getRegisters().getRegister(Registers.REG_SP));
        assertEquals(0x99, memory.getByte(0x01FF));
    }

    @Test
    public void testPLA() {
        Program program = new Program().with(LDA_I, 0x99, PHA, LDA_I, 0x11, PLA);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);
        assert (processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR) == 0x99);
        processor.step();
        assert (processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR) == 0x11);
        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xFF, processor.getRegisters().getRegister(Registers.REG_SP));
        assertEquals(0x99, processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testASL() {
        Program program = new Program().with(LDA_I, 0b01010101, ASL_A);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals("Expected 10101010, got " + Integer.toBinaryString(processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR)),
                0b10101010, processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testLSR(){
        Program program = new Program().with(LDA_I, 0b01011010, LSR_A);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals("Expected 00101101, got " + Integer.toBinaryString(processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR)),
                0b00101101, processor.getRegisters().getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testNOP(){
        Program program = new Program().with(NOP, NOP, NOP);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        for (int i=1; i<=program.getLength(); i++){
            processor.step();
            assertEquals(i, registers.getPC());
        }
    }

    @Test
    public void testJMP(){
        Program program = new Program().with(LDX_I, 0x8,
                         JMP_ABS, 0x0, 0x7,
                         LDY_I, 0x9,
                         LDA_I, 0x10);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x8, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x0, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(0x10, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testBCC(){
        Program program = new Program().with(CLC, BCC, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBCS(){
        Program program = new Program().with(SEC, BCS, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBNE(){
        Program program = new Program().with(LDA_I, 0x1, BNE, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x1, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBEQ(){
        Program program = new Program().with(LDA_I, 0x0, BEQ, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testROL(){
        Program program = new Program().with(SEC, LDA_I, 0b00000001, ROL_A);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b00000011, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testROR(){
        Program program = new Program().with(SEC, LDA_I, 0b00000010, ROR_A);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b10000001, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testBMI(){
        Program program = new Program().with(LDA_I, 0b11111110, BMI, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b11111110, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBPL(){
        Program program = new Program().with(LDA_I, 0b00000001, BPL, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b00000001, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBVS(){
        Program program = new Program().with(LDA_I, 0x50, ADC_I, 0x50, BVS, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xA0, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testBVC(){
        Program program = new Program().with(LDA_I, 0x0, ADC_I, 0x10, BVC, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x10, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x97, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testTAX(){
        Program program = new Program().with(LDA_I, 0x0F, TAX);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0F, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0F, registers.getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testTAY(){
        Program program = new Program().with(LDA_I, 0x0F, TAY);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0F, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x0F, registers.getRegister(Registers.REG_Y_INDEX));
    }

    @Test
    public void testTYA(){
        Program program = new Program().with(LDY_I, 0x0D, TYA);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0D, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(0x0D, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testTXA(){
        Program program = new Program().with(LDX_I, 0x0D, TXA);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x0D, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0x0D, registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    @Test
    public void testTXS(){
        Program program = new Program().with(LDX_I, 0xAA, TXS);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xAA, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(0xAA, registers.getRegister(Registers.REG_SP));
    }

    @Test
    public void testTSX(){
        Program program = new Program().with(TSX);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0xFF, registers.getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testBIT(){
        Program program = new Program().with(LDA_I, 0x01, STA_Z, 0x20, LDA_I, 0x01, BIT_Z, 0x20);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_ZERO));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_OVERFLOW));
    }

    @Test
    public void testCMP(){
        Program program = new Program().with(LDA_I, 0x10, CMP_I, 0x10);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x10, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_ZERO));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_CARRY));
    }

    @Test
    public void testCPX(){
        Program program = new Program().with(LDX_I, 0x10, CPX_I, 0x10);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x10, registers.getRegister(Registers.REG_X_INDEX));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_ZERO));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_CARRY));
    }

    @Test
    public void testCPY(){
        Program program = new Program().with(LDY_I, 0x10, CPY_I, 0x10);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0x10, registers.getRegister(Registers.REG_Y_INDEX));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_ZERO));
        assertEquals(false, registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
        assertEquals(true, registers.getFlag(Registers.STATUS_FLAG_CARRY));
    }

    @Test
    public void testPHP(){
        Program program = new Program().with(PHP);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();

        int stackLoc = (registers.getRegister(Registers.REG_SP) + 1);
        int stackValue = (memory.getByte(0x100 | stackLoc));

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(registers.getRegister(Registers.REG_STATUS), stackValue);
    }

    @Test
    public void testPLP(){
        Program program = new Program().with(PHP, PLP);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b11111111);
        processor.step(1);
        registers.setRegister(Registers.REG_STATUS, 0b00000000);
        processor.step(1);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b11111111, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testCLI(){
        Program program = new Program().with(CLI);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b00000100);
        processor.step(1);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b00000000, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testSEI(){
        Program program = new Program().with(SEI);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b11111011);
        processor.step(1);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b11111111, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testSED(){
        Program program = new Program().with(SED);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b11110111);
        processor.step(1);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b11111111, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testCLD(){
        Program program = new Program().with(CLD);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.REG_STATUS, 0b00001000);
        processor.step(1);

        assertEquals(program.getLength(), registers.getPC());
        assertEquals(0b00000000, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testJSR(){
        Program program = new Program().with(JSR, 0x02, 0x0F);
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(1);

        assertEquals(0x020F, registers.getPC());
        assertEquals(0x02, registers.getRegister(Registers.REG_PC_HIGH)); //Jump address
        assertEquals(0x0F, registers.getRegister(Registers.REG_PC_LOW));

        assertEquals(0x00, memory.getByte(0x1FF)); //Return address
        assertEquals(0x03, memory.getByte(0x1FE));
    }

    @Test(timeout = 2000)
    public void testLoop(){
        Program program = new Program().with(LDX_I, 10,        //Loop counter
                         LDA_I, 0,         //Sum
                         CLC,              //LOOP: Clear cary before ADC
                         ADC_I, 1,         //Add one
                         DEX,              //advance loop counter
                         CPX_I, 0,         //is it the end of the loop?
                         BNE, 0b11110111); //If not, go again
        memory.setBlock(0, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        while (registers.getRegister(Registers.REG_PC_LOW) < program.getLength())
            processor.step();

        assertEquals(10, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0, registers.getRegister(Registers.REG_X_INDEX));
    }

    @Test
    public void testBRK(){
        Program program = new Program().with(BRK);
        memory.setBlock(0, program.getProgramAsByteArray());
        memory.setByteAt(0xFFFE, 0);                                     //New PC
        memory.setByteAt(0xFFFF, 0);

        Registers registers = processor.getRegisters();
        registers.setRegister(Registers.REG_STATUS, 0b00000000);         //Sample register values

        processor.step(1);

        assertEquals(0xFC, registers.getRegister(Registers.REG_SP));

        //PC (on Stack)
        assertEquals(0x03, memory.getByte(0x1FE));
        assertEquals(0x00, memory.getByte(0x1FF));

        //Status (on stack) with B set
        assertEquals(Registers.STATUS_FLAG_BREAK, memory.getByte(0x1FD));

        //PC is set to value of [FFFE:FFFF]
        assertEquals(memory.getByte(0xFFFE), registers.getRegister(Registers.REG_PC_HIGH));
        assertEquals(memory.getByte(0xFFFF), registers.getRegister(Registers.REG_PC_LOW));
    }

    @Test
    public void testIRQ(){
        Program program = new Program().with(LDA_I, 1,
                         LDA_I, 2,
                         LDA_I, 3);
        memory.setBlock(0, program.getProgramAsByteArray());
        memory.setByteAt(0xFFFE, 0x01); //->PCH
        memory.setByteAt(0xFFFF, 0x10); //->PCL

        Registers registers = processor.getRegisters();
        registers.setRegister(Registers.REG_STATUS, 0b00000000);         //Sample register values

        processor.step(1);
        processor.irq();

        assertEquals(0xFC, registers.getRegister(Registers.REG_SP));

        //PC (on Stack)
        assertEquals(0x02, memory.getByte(0x1FE));
        assertEquals(0x00, memory.getByte(0x1FF));

        //Status (on stack) with B set
        assertEquals(Registers.STATUS_FLAG_IRQ_DISABLE, memory.getByte(0x1FD));

        //PC is set to value of [FFFE:FFFF]
        assertEquals(memory.getByte(0xFFFE), registers.getRegister(Registers.REG_PC_HIGH));
        assertEquals(memory.getByte(0xFFFF), registers.getRegister(Registers.REG_PC_LOW));
    }

    @Test
    public void testMultiplicationLoop(){
            int data_offset = 0x32;
            int MPD = data_offset + 0x10;
            int MPR = data_offset + 0x11;
            int TMP = data_offset + 0x20;
            int RESAD_0 = data_offset + 0x30;
            int RESAD_1 = data_offset + 0x31;

            int valMPD = 7;
            int valMPR = 4;

            Program program = new Program().with(LDA_I, valMPD,
                             STA_Z, MPD,
                             LDA_I, valMPR,
                             STA_Z, MPR,
                             LDA_I, 0,         //<---- start
                             STA_Z, TMP,       //Clear
                             STA_Z, RESAD_0,   //...
                             STA_Z, RESAD_1,   //...
                             LDX_I, 8,         //X counts each bit

                             LSR_Z, MPR,       //:MULT(18) LSR(MPR)
                             BCC, 13,          //Test carry and jump (forward 13) to NOADD

                             LDA_Z, RESAD_0,   //RESAD -> A
                             CLC,              //Prepare to add
                             ADC_Z, MPD,       //+MPD
                             STA_Z, RESAD_0,   //Save result
                             LDA_Z, RESAD_1,   //RESAD+1 -> A
                             ADC_Z, TMP,       //+TMP
                             STA_Z, RESAD_1,   //RESAD+1 <- A
                             ASL_Z, MPD,       //:NOADD(35) ASL(MPD)
                             ROL_Z, TMP,       //Save bit from MPD
                             DEX,              //--X
                             BNE, 0b11100111 //Test equal and jump (back 24) to MULT
            );

            memory.setBlock(0, program.getProgramAsByteArray());
            Registers registers = processor.getRegisters();

            processor.step(27);

            System.out.println("RESAD = " + Integer.toBinaryString(memory.getByte(RESAD_0)) + "|" + Integer.toBinaryString(memory.getByte(RESAD_1)));
            System.out.println("MPD = " + memory.getByte(MPD));
            System.out.println("MPR = " + memory.getByte(MPR));
            System.out.println("TMP = " + memory.getByte(TMP));
            System.out.println("[A] = " + registers.getRegister(Registers.REG_ACCUMULATOR));

            assertEquals(0x1C, memory.getByte(RESAD_0));
    }
}
