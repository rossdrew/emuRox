package com.rox.emu.processor.mos6502;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.op.AddressingMode;
import com.rox.emu.processor.mos6502.op.OpCode;
import com.rox.emu.processor.mos6502.util.Compiler;
import com.rox.emu.processor.mos6502.util.Program;
import org.junit.Test;
import org.junit.runner.RunWith;
import spock.lang.Ignore;

import java.util.Arrays;

import static com.rox.emu.processor.mos6502.util.Compiler.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class CompilerTest {
    @Test
    public void testPrefixExtraction(){
        try {
            assertEquals("$", extractFirstOccurrence(PREFIX_REGEX, "$10"));
            assertEquals("#$", extractFirstOccurrence(PREFIX_REGEX, "#$10"));
            assertEquals("$", extractFirstOccurrence(PREFIX_REGEX, "$AA"));
            assertEquals("#$", extractFirstOccurrence(PREFIX_REGEX, "#$AA"));
            assertEquals("($", extractFirstOccurrence(PREFIX_REGEX, "($AA,X)"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testValueExtraction(){
        try {
            assertEquals("10", extractFirstOccurrence(VALUE_REGEX, "$10"));
            assertEquals("10", extractFirstOccurrence(VALUE_REGEX, "#$10"));
            assertEquals("AA", extractFirstOccurrence(VALUE_REGEX, "$AA"));
            assertEquals("AA", extractFirstOccurrence(VALUE_REGEX, "#$AA"));
            assertEquals("AA", extractFirstOccurrence(VALUE_REGEX, "($AA,X)"));

            assertEquals("A", extractFirstOccurrence(VALUE_REGEX, "($A,X)"));
            assertEquals("BBB", extractFirstOccurrence(VALUE_REGEX, "($BBB,X)"));
            assertEquals("CCCC", extractFirstOccurrence(VALUE_REGEX, "($CCCC,X)"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testPostfixExtraction(){
        try {
            assertEquals(",X", extractFirstOccurrence(POSTFIX_REGEX, "$10,X"));
            assertEquals(",Y", extractFirstOccurrence(POSTFIX_REGEX, "$AA,Y"));
            assertEquals(",X)", extractFirstOccurrence(POSTFIX_REGEX, "($AA,X)"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testInvalidPostfixExtraction(){
        final String[] invalidPostfixes = new String[] {"($AA,Y)"};

        for (String invalidPostfix : invalidPostfixes) {
            assertTrue(extractFirstOccurrence(POSTFIX_REGEX, invalidPostfix).isEmpty());
        }
    }

    @Test
    public void testLabelExtraction(){
        try {
            assertEquals("TEST:", extractFirstOccurrence(LABEL_REGEX, "TEST:"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testImpliedInstructions(){
        OpCode.streamOf(AddressingMode.IMPLIED).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName());

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertEquals("Wrong byte value for " + opcode.getOpCodeName() + "(" + opcode.getByteValue() + ")", opcode.getByteValue(), bytes[0]);
        });
    }

    @Test
    public void testSingleDigitArgument(){
        OpCode.streamOf(AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "A");

            final Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xA}, bytes);
        });
    }

    @Test
    public void testDoubleDigitArgument(){
        OpCode.streamOf(AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "AB");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xAB}, bytes);
        });
    }

    @Test
    public void testTripleDigitArgument(){
        OpCode.streamOf(AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "ABC");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xABC}, bytes);
        });
    }

    @Test
    public void testQuadrupleDigitArgument(){
        OpCode.streamOf(AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "ABCD");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xABCD}, bytes);
        });
    }

    @Property
    public void testImmediateInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.IMMEDIATE).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + IMMEDIATE_VALUE_PREFIX + hexByte);

            Program program = compiler.compileProgram(); 
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testAccumulatorInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.ACCUMULATOR).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + ACCUMULATOR_PREFIX + hexByte);

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte);

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageXInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.ZERO_PAGE_X).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte+ ",X");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageYInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.ZERO_PAGE_Y).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte + ",Y");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testAbsoluteInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        OpCode.streamOf(AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord);

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), wordValue}, bytes);
        });
    }

    @Property
    public void testAbsoluteXInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        OpCode.streamOf(AddressingMode.ABSOLUTE_X).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord + ",X");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + " 0x" + hexWord + "' was wrong.", new int[] {opcode.getByteValue(), wordValue}, bytes);
        });
    }

    @Property
    public void testAbsoluteYInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        OpCode.streamOf(AddressingMode.ABSOLUTE_Y).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord + ",Y");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals("Output for '" + opcode.toString() + " 0x" + hexWord + "' was wrong.", new int[] {opcode.getByteValue(), wordValue}, bytes);
        });
    }

    @Property
    public void testIndirectXInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.INDIRECT_X).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " (" + VALUE_PREFIX + hexByte + ",X)");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals(new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testIndirectYInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.INDIRECT_Y).forEach((opcode)->{
            final Compiler compiler = new Compiler(opcode.getOpCodeName() + " (" + VALUE_PREFIX + hexByte + "),Y");

            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();

            assertArrayEquals(new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Test
    public void testChainedInstruction(){
        final Compiler compiler = new Compiler("SEC LDA " + IMMEDIATE_VALUE_PREFIX + "47");
        final Program program = compiler.compileProgram();
        int[] bytes = program.getProgramAsByteArray();

        int[] expected = new int[] {OpCode.SEC.getByteValue(), OpCode.LDA_I.getByteValue(), 0x47};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstruction(){
        final Compiler compiler = new Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = program.getProgramAsByteArray();

        int[] expected = new int[] {OpCode.LDA_I.getByteValue(), 0x47, OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstructions(){
        final Compiler compiler = new Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 CLC LDA " + IMMEDIATE_VALUE_PREFIX + "10 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = program.getProgramAsByteArray();

        int[] expected = new int[] {OpCode.LDA_I.getByteValue(), 0x47, OpCode.CLC.getByteValue(), OpCode.LDA_I.getByteValue(), 0x10, OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstructionsWithLabel(){
        final Compiler compiler = new Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 LABELA: CLC LDA " + IMMEDIATE_VALUE_PREFIX + "10 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = program.getProgramAsByteArray();

        int[] expected = new int[] {OpCode.LDA_I.getByteValue(), 0x47, OpCode.CLC.getByteValue(), OpCode.LDA_I.getByteValue(), 0x10, OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
        assertEquals(1, program.getLabels().size());
        assertEquals(2, program.getLocationOf("LABELA:"));
    }

    @Test
    public void testProgram(){
        final Compiler compiler = new Compiler("LDA #$14 ADC #$5 STA $20");
        final Program program = compiler.compileProgram();
        int[] bytes = program.getProgramAsByteArray();

        int[] expected = new int[] {OpCode.LDA_I.getByteValue(), 0x14,
                                    OpCode.ADC_I.getByteValue(), 0x5,
                                    OpCode.STA_Z.getByteValue(), 0x20};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testIntegration(){
        final Compiler compiler = new Compiler("LDA #$14 ADC #$5 STA $20");
        Program program = compiler.compileProgram();
        final int[] programByte = program.getProgramAsByteArray();

        Memory memory = new SimpleMemory();
        CPU processor = new CPU(memory);
        processor.reset();
        memory.setBlock(0, programByte);

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x19, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x19, memory.getByte(0x20));
        assertEquals(programByte.length, registers.getPC());
    }

    @Test
    @Ignore
    public void testAbsoluteAddressing(){
        final Compiler compiler = new Compiler("LDA #$1C STA $100 INC $100");
        Program program = compiler.compileProgram();
        final int[] programByte = program.getProgramAsByteArray();

        Memory memory = new SimpleMemory();
        CPU processor = new CPU(memory);
        processor.reset();
        memory.setBlock(0, programByte);

        processor.step(3);

        assertEquals(0x1D, memory.getByte(0x100));
    }

    @Test
    public void firstInvalidOpcode(){
        final Compiler compiler = new Compiler("ROX");

        try {
            compiler.compileProgram();
            fail("Exception expected, 'ROX' is an invalid OpCode");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
        }
    }

    @Test
    public void opCode1JaCoCoCoverage(){
        final Compiler compiler = new Compiler("\0ADC $10");

        try {
            compiler.compileProgram();
            fail("Exception expected.  This should not pass a String switch statement");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
        }
    }

    @Test
    public void opCode2JaCoCoCoverage(){
        final Compiler compiler = new Compiler("\0BRK");

        try {
            compiler.compileProgram();
            fail("Exception expected.  This should not pass a String switch statement");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testInvalidValuePrefix(){
        try {
            final Compiler compiler = new Compiler("ADC @$10");
            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();
            fail("Invalid value prefix should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }

    @Test
    public void testInvalidIndirectIndexingMode(){
        try {
            final Compiler compiler = new Compiler("ADC " + INDIRECT_PREFIX + "10(");
            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();
            fail("Invalid prefix for an indirect value should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }

    @Test
    public void testOversizedValue(){
        try {
            final Compiler compiler = new Compiler("ADC " + VALUE_PREFIX + "12345");
            Program program = compiler.compileProgram();
            int[] bytes = program.getProgramAsByteArray();
            fail("Argument size over " + 0xFFFF + " should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }
}
