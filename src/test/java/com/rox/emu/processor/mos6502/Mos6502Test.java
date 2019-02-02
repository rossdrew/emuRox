package com.rox.emu.processor.mos6502;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.util.Program;
import org.junit.Before;
import org.junit.Test;

import static com.rox.emu.processor.mos6502.op.Mos6502OpCode.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.spockframework.util.Assert.fail;

public class Mos6502Test {
    private Memory memory;
    private Registers registers;
    private Mos6502 processor;

    @Before
    public void setUp() {
        registers = new Registers();
        memory = new SimpleMemory();
        memory.setByteAt(RoxWord.fromLiteral(0xFFFC), RoxByte.ZERO);
        memory.setByteAt(RoxWord.fromLiteral(0xFFFD), RoxByte.ZERO);

        processor = new Mos6502(memory, registers);
        processor.reset();
    }

    @Test
    public void testStartup() {
        memory = new SimpleMemory();
        memory.setByteAt(RoxWord.fromLiteral(0xFFFC), RoxByte.fromLiteral(1));
        memory.setByteAt(RoxWord.fromLiteral(0xFFFD), RoxByte.fromLiteral(1));

        processor = new Mos6502(memory, registers);
        processor.reset();

        Registers registers = processor.getRegisters();

        assertEquals(RoxByte.fromLiteral(0x34), registers.getRegister(Registers.Register.STATUS_FLAGS)); //Status flags reset
        assertEquals(RoxByte.fromLiteral(0x1), registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));  //PC Set to location pointed to by mem[FFFC:FFFD]
        assertEquals(RoxByte.fromLiteral(0x1), registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI)); // ...
        assertEquals(RoxByte.fromLiteral(0xFF), registers.getRegister(Registers.Register.STACK_POINTER_LOW));     //Stack Pointer at top of stack

        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.ACCUMULATOR)); //All cleared
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testReset() {
        Program program = new Program().with(LDA_I, 0xAA,
                                             LDX_I, 0xBB,
                                             LDX_I, 0xCC);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        memory.setByteAt(RoxWord.fromLiteral(0xFFFC), RoxByte.ZERO);
        memory.setByteAt(RoxWord.fromLiteral(0xFFFD), RoxByte.ZERO);

        Registers registers = processor.getRegisters();

        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0x99));
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_LOW, RoxByte.fromLiteral(0x99));
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_HI, RoxByte.fromLiteral(0x99));
        registers.setRegister(Registers.Register.STACK_POINTER_LOW, RoxByte.fromLiteral(0x99));
        registers.setRegister(Registers.Register.ACCUMULATOR, RoxByte.fromLiteral(0x99));
        registers.setRegister(Registers.Register.X_INDEX, RoxByte.fromLiteral(0x99));
        registers.setRegister(Registers.Register.Y_INDEX, RoxByte.fromLiteral(0x99));

        processor.step(3);
        processor.reset();

        assertEquals(RoxByte.fromLiteral(0x34), registers.getRegister(Registers.Register.STATUS_FLAGS));
        assertEquals(RoxByte.fromLiteral(0x0), registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));
        assertEquals(RoxByte.fromLiteral(0x0), registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));
        assertEquals(RoxByte.fromLiteral(0xFF), registers.getRegister(Registers.Register.STACK_POINTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testLDA() {
        Program program = new Program().with(LDA_I, 0xAA);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0xAA), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testSTA() {
        Program program = new Program().with(LDA_I,
                0xAA, STA_Z, 100);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xAA), memory.getByte(RoxWord.fromLiteral(100)));
    }

    @Test
    public void testAccumulatorSTA() {
        Program program = new Program().with(LDA_I, 0xAA, STA_ABS, 0xFF, 0x01);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xAA), memory.getByte(RoxWord.fromLiteral(0xFF01)));
    }

    @Test
    public void testZAtXIndexSTA() {
        Program program = new Program().with(LDA_I, 0xAA, LDX_I, 0x1, STA_Z_IX, 0x20);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xAA), memory.getByte(RoxWord.fromLiteral(0x21)));
    }

    @Test
    public void testSTX() {
        Program program = new Program().with(LDX_I, 0xAA, STX_Z, 100);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xAA), memory.getByte(RoxWord.fromLiteral(100)));
    }

    @Test
    public void testSTXAbsolute() {
        Program program = new Program().with(LDX_I, 0xAA, STX_ABS, 0x02, 0x20);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xAA), memory.getByte(RoxWord.fromLiteral(0x220)));
    }

    @Test
    public void testSTY() {
        Program program = new Program().with(LDY_I, 0xAA, STY_Z, 100);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xAA), memory.getByte(RoxWord.fromLiteral(100)));
    }

    @Test
    public void testSTYAbsolute() {
        Program program = new Program().with(LDY_I, 0xAA, STY_ABS, 0x02, 0x20);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xAA), memory.getByte(RoxWord.fromLiteral(0x220)));
    }

    @Test
    public void testLDX() {
        Program program = new Program().with(LDX_I, 0xAA);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0xAA), registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testLDY() {
        Program program = new Program().with(LDY_I, 0xAA);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step();

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0xAA), registers.getRegister(Registers.Register.Y_INDEX));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testADC() {
        Program program = new Program().with(CLC,
                                             LDA_I,
                                             0x1,
                                             ADC_I,
                                             0x1);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0x2), registers.getRegister(Registers.Register.ACCUMULATOR));  //Accumulator is 0x2 == (0x1 + 0x1) == (mem[0x1] + mem[0x3])
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        
    }

    @Test
    public void testADCWithCarry() {
        Program program = new Program().with(SEC,
                         LDA_I,
                         0x1,
                         ADC_I,
                         0x1);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0x3), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testSBC() {
        Program program = new Program().with(SEC, LDA_I, 0xA, SBC_I, 0x5);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0x5), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testSBCWithCarry() {
        Program program = new Program().with(CLC,
                         LDA_I, 0xA,
                         SBC_I, 0x5);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0x4), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testAND() {
        Program program = new Program().with(LDA_I,
                0b00000101,
                AND_I,
                0b00000101);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0b00000101), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testOR() {
        Program program = new Program().with(LDA_I,
                0b00010101,
                ORA_I,
                0b00000101);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0b00010101), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testEOR() {
        Program program = new Program().with(LDA_I,
                0b00010101,
                EOR_I,
                0b00000101);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(2);

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0b00010000), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
    }

    @Test
    public void testSEC() {
        Program program = new Program().with(SEC);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(true, registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testCLC() {
        Program program = new Program().with(SEC, CLC);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getFlag(Registers.Flag.CARRY));
        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(false, registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testCLV() {
        Program program = new Program().with(LDA_I, 0x50, ADC_I, 0x50, CLV);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);
        assert (registers.getFlag(Registers.Flag.OVERFLOW));
        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(false, registers.getFlag(Registers.Flag.OVERFLOW));
    }

    @Test
    public void testINX() {
        Program program = new Program().with(LDX_I, 0x01, INX);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.Register.X_INDEX).equals(RoxByte.fromLiteral(1)));
        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(2), processor.getRegisters().getRegister(Registers.Register.X_INDEX));
    }

    @Test
    public void testINY() {
        Program program = new Program().with(LDY_I, 0x01, INY);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.Register.Y_INDEX).equals(RoxByte.fromLiteral(1)));
        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(2), processor.getRegisters().getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testINC() {
        Program program = new Program().with(LDA_I, 1, STA_Z, 0x20, INC_Z, 0x20);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);
        assert !registers.getFlag(Registers.Flag.CARRY);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(2), memory.getByte(RoxWord.fromLiteral(0x20)));
        assertFalse(registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testDEC() {
        Program program = new Program().with(LDA_I, 9, STA_Z, 0x20, DEC_Z, 0x20);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        boolean wasCarrySet = registers.getFlag(Registers.Flag.CARRY);

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(8), memory.getByte(RoxWord.fromLiteral(0x20)));
        assertEquals(wasCarrySet, registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testDEY() {
        Program program = new Program().with(LDY_I, 0x01, DEY);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.Register.Y_INDEX).equals(RoxByte.fromLiteral(1)));
        assert !registers.getFlag(Registers.Flag.CARRY);
        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.ZERO, processor.getRegisters().getRegister(Registers.Register.Y_INDEX));
        assertFalse(registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testDEX() {
        Program program = new Program().with(LDX_I, 0x01, DEX);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.Register.X_INDEX).equals(RoxByte.fromLiteral(1)));
        assert !registers.getFlag(Registers.Flag.CARRY);
        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.ZERO, processor.getRegisters().getRegister(Registers.Register.X_INDEX));
        assertFalse(registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testInvalidOpCode() {
        Program program = new Program().with(231);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        try {
            processor.step();
            fail("Invalid opCode exception expected!");
        } catch (UnknownOpCodeException e) {
            assertEquals("231", e.getOpCode());
        }
    }

    @Test
    public void testPHA() {
        Program program = new Program().with(LDA_I, 0x99, PHA);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();
        assert (processor.getRegisters().getRegister(Registers.Register.STACK_POINTER_LOW).equals(RoxByte.fromLiteral(0xFF)));
        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xFE), processor.getRegisters().getRegister(Registers.Register.STACK_POINTER_LOW));
        assertEquals(RoxByte.fromLiteral(0x99), memory.getByte(RoxWord.fromLiteral(0x01FF)));
    }

    @Test
    public void testPLA() {
        Program program = new Program().with(LDA_I, 0x99,
                                             PHA,
                                             LDA_I, 0x11,
                                             PLA);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);
        assert (processor.getRegisters().getRegister(Registers.Register.ACCUMULATOR).equals(RoxByte.fromLiteral(0x99))) : "0x99 is not loaded to Accumulator as expected";
        processor.step();
        assert (processor.getRegisters().getRegister(Registers.Register.ACCUMULATOR).equals(RoxByte.fromLiteral(0x11))) : "0x11 is not loaded to Accumulator as expected";
        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xFF), processor.getRegisters().getRegister(Registers.Register.STACK_POINTER_LOW));
        assertEquals("0x99 (-103) was expected to have been popped from the stack with PLA", RoxByte.fromLiteral(0x99), processor.getRegisters().getRegister(Registers.Register.ACCUMULATOR));
    }

    @Test
    public void testASL() {
        Program program = new Program().with(LDA_I, 0b01010101, ASL_A);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals("Expected 10101010, got " + processor.getRegisters().getRegister(Registers.Register.ACCUMULATOR).toBinaryString(),
                RoxByte.fromLiteral(0b10101010), processor.getRegisters().getRegister(Registers.Register.ACCUMULATOR));
    }

    @Test
    public void testLSR(){
        Program program = new Program().with(LDA_I, 0b01011010, LSR_A);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals("Expected 00101101, got " + processor.getRegisters().getRegister(Registers.Register.ACCUMULATOR).toBinaryString(),
                RoxByte.fromLiteral(0b00101101), processor.getRegisters().getRegister(Registers.Register.ACCUMULATOR));
    }

    @Test
    public void testNOP(){
        Program program = new Program().with(NOP, NOP, NOP);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        for (int i=1; i<=program.getLength(); i++){
            processor.step();
            assertEquals(RoxWord.fromLiteral(i), registers.getPC());
        }
    }

    @Test
    public void testJMP(){
        Program program = new Program().with(LDX_I, 0x8,
                         JMP_ABS, 0x0, 0x7,
                         LDY_I, 0x9,
                         LDA_I, 0x10);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x8), registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x0), registers.getRegister(Registers.Register.Y_INDEX));
        assertEquals(RoxByte.fromLiteral(0x10), registers.getRegister(Registers.Register.ACCUMULATOR));
    }

    @Test
    public void testBCC(){
        Program program = new Program().with(CLC, BCC, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x97), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testBCS(){
        Program program = new Program().with(SEC, BCS, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x97), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testBNE(){
        Program program = new Program().with(LDA_I, 0x1, BNE, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x1), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x97), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testBEQ(){
        Program program = new Program().with(LDA_I, 0x0, BEQ, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x97), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testROL(){
        Program program = new Program().with(SEC, LDA_I, 0b00000001, ROL_A);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b00000011), registers.getRegister(Registers.Register.ACCUMULATOR));
    }

    @Test
    public void testROR(){
        Program program = new Program().with(SEC, LDA_I, 0b00000010, ROR_A);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b10000001), registers.getRegister(Registers.Register.ACCUMULATOR));
    }

    @Test
    public void testBMI(){
        Program program = new Program().with(LDA_I, 0b11111110, BMI, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b11111110), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x97), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testBPL(){
        Program program = new Program().with(LDA_I, 0b00000001, BPL, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(3);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b00000001), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x97), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testBVS(){
        Program program = new Program().with(LDA_I, 0x50, ADC_I, 0x50, BVS, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xA0), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x97), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testBVC(){
        Program program = new Program().with(LDA_I, 0x0, ADC_I, 0x10, BVC, 0x4, LDA_I, 0x99, LDX_I, 0x98, LDY_I, 0x97);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x10), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x97), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testTAX(){
        Program program = new Program().with(LDA_I, 0x0F, TAX);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x0F), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.fromLiteral(0x0F), registers.getRegister(Registers.Register.X_INDEX));
    }

    @Test
    public void testTAY(){
        Program program = new Program().with(LDA_I, 0x0F, TAY);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x0F), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.fromLiteral(0x0F), registers.getRegister(Registers.Register.Y_INDEX));
    }

    @Test
    public void testTYA(){
        Program program = new Program().with(LDY_I, 0x0D, TYA);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x0D), registers.getRegister(Registers.Register.Y_INDEX));
        assertEquals(RoxByte.fromLiteral(0x0D), registers.getRegister(Registers.Register.ACCUMULATOR));
    }

    @Test
    public void testTXA(){
        Program program = new Program().with(LDX_I, 0x0D, TXA);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x0D), registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0x0D), registers.getRegister(Registers.Register.ACCUMULATOR));
    }

    @Test
    public void testTXS(){
        Program program = new Program().with(LDX_I, 0xAA, TXS);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xAA), registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.fromLiteral(0xAA), registers.getRegister(Registers.Register.STACK_POINTER_LOW));
    }

    @Test
    public void testTSX(){
        Program program = new Program().with(TSX);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0xFF), registers.getRegister(Registers.Register.X_INDEX));
    }

    @Test
    public void testBIT(){
        Program program = new Program().with(LDA_I, 0x01,
                                             STA_Z, 0x20,
                                             LDA_I, 0x01,
                                             BIT_Z, 0x20);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(4);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(true, registers.getFlag(Registers.Flag.ZERO));
        assertEquals(false, registers.getFlag(Registers.Flag.NEGATIVE));
        assertEquals(false, registers.getFlag(Registers.Flag.OVERFLOW));
    }

    @Test
    public void testCMP(){
        Program program = new Program().with(LDA_I, 0x10, CMP_I, 0x10);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x10), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(true, registers.getFlag(Registers.Flag.ZERO));
        assertEquals(false, registers.getFlag(Registers.Flag.NEGATIVE));
        assertEquals(true, registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testCPX(){
        Program program = new Program().with(LDX_I, 0x10, CPX_I, 0x10);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x10), registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(true, registers.getFlag(Registers.Flag.ZERO));
        assertEquals(false, registers.getFlag(Registers.Flag.NEGATIVE));
        assertEquals(true, registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testCPY(){
        Program program = new Program().with(LDY_I, 0x10, CPY_I, 0x10);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(2);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x10), registers.getRegister(Registers.Register.Y_INDEX));
        assertEquals(true, registers.getFlag(Registers.Flag.ZERO));
        assertEquals(false, registers.getFlag(Registers.Flag.NEGATIVE));
        assertEquals(true, registers.getFlag(Registers.Flag.CARRY));
    }

    @Test
    public void testPHP(){
        Program program = new Program().with(PHP);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step();

        int stackLsb = registers.getRegister(Registers.Register.STACK_POINTER_LOW).getRawValue() + 1;
        RoxWord stackLocation = RoxWord.fromLiteral(0x100 | stackLsb);
        RoxByte stackValue = memory.getByte(stackLocation);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(registers.getRegister(Registers.Register.STATUS_FLAGS), stackValue);
    }

    @Test
    public void testPLP(){
        Program program = new Program().with(PHP, PLP);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0b11111111));
        processor.step(1);
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0b00000000));
        processor.step(1);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b11111111), registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testCLI(){
        Program program = new Program().with(CLI);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0b00000100));
        processor.step(1);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b00000000), registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testSEI(){
        Program program = new Program().with(SEI);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0b11111011));
        processor.step(1);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b11111111), registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testSED(){
        Program program = new Program().with(SED);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0b11110111));
        processor.step(1);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b11111111), registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testCLD(){
        Program program = new Program().with(CLD);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        //Load status, push to stack then clear it and pull it from stack
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0b00001000));
        processor.step(1);

        assertEquals(RoxWord.fromLiteral(program.getLength()), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0b00000000), registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testJSR(){
        Program program = new Program().with(JSR, 0x02, 0x0F);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        processor.step(1);

        assertEquals(RoxWord.fromLiteral(0x020F), registers.getPC());
        assertEquals(RoxByte.fromLiteral(0x02), registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI)); //Jump address
        assertEquals(RoxByte.fromLiteral(0x0F), registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));

        assertEquals(RoxByte.fromLiteral(0x00), memory.getByte(RoxWord.fromLiteral(0x1FF))); //Return address
        assertEquals(RoxByte.fromLiteral(0x03), memory.getByte(RoxWord.fromLiteral(0x1FE)));
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
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        Registers registers = processor.getRegisters();

        while (registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW).getRawValue() < program.getLength())
            processor.step();

        assertEquals(RoxByte.fromLiteral(10), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
    }

    @Test
    public void testBRK(){
        Program program = new Program().with(BRK);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        memory.setByteAt(RoxWord.fromLiteral(0xFFFE), RoxByte.ZERO);                                     //New PC
        memory.setByteAt(RoxWord.fromLiteral(0xFFFF), RoxByte.ZERO);

        Registers registers = processor.getRegisters();
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.ZERO);         //Sample register values

        processor.step(1);

        assertEquals(RoxByte.fromLiteral(0xFC), registers.getRegister(Registers.Register.STACK_POINTER_LOW));

        //PC (on Stack)
        assertEquals(RoxByte.fromLiteral(0x03), memory.getByte(RoxWord.fromLiteral(0x1FE)));
        assertEquals(RoxByte.ZERO, memory.getByte(RoxWord.fromLiteral(0x1FF)));

        //Status (on stack) with B set
        assertTrue(memory.getByte(RoxWord.fromLiteral(0x1FD)).isBitSet(Registers.Flag.BREAK.getIndex()));

        //PC is set to value of [FFFE:FFFF]
        assertEquals(memory.getByte(RoxWord.fromLiteral(0xFFFE)), registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));
        assertEquals(memory.getByte(RoxWord.fromLiteral(0xFFFF)), registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));
    }

    @Test
    public void testIRQ(){
        Program program = new Program().with(LDA_I, 1,
                         LDA_I, 2,
                         LDA_I, 3);
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
        memory.setByteAt(RoxWord.fromLiteral(0xFFFE), RoxByte.fromLiteral(0x01)); //->PCH
        memory.setByteAt(RoxWord.fromLiteral(0xFFFF), RoxByte.fromLiteral(0x10)); //->PCL

        Registers registers = processor.getRegisters();
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0b00000000));         //Sample register values

        processor.step(1);
        processor.irq();

        assertEquals(RoxByte.fromLiteral(0xFC), registers.getRegister(Registers.Register.STACK_POINTER_LOW));

        //PC (on Stack)
        assertEquals(RoxByte.fromLiteral(0x02), memory.getByte(RoxWord.fromLiteral(0x1FE)));
        assertEquals(RoxByte.fromLiteral(0x00), memory.getByte(RoxWord.fromLiteral(0x1FF)));

        //Status (on stack) with B set
        assertTrue(memory.getByte(RoxWord.fromLiteral(0x1FD)).isBitSet(Registers.Flag.IRQ_DISABLE.getIndex()));

        //PC is set to value of [FFFE:FFFF]
        assertEquals(memory.getByte(RoxWord.fromLiteral(0xFFFE)), registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));
        assertEquals(memory.getByte(RoxWord.fromLiteral(0xFFFF)), registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));
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

            memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());
            Registers registers = processor.getRegisters();

            processor.step(27);

            assertEquals(RoxByte.fromLiteral(0x1C), memory.getByte(RoxWord.fromLiteral(RESAD_0)));
    }
}
