package com.rox.emu.processor.mos6502;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.UnknownTokenException;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.op.Mos6502AddressingMode;
import com.rox.emu.processor.mos6502.op.OpCode;
import com.rox.emu.processor.mos6502.util.Mos6502Compiler;
import com.rox.emu.processor.mos6502.util.Program;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.rox.emu.processor.mos6502.util.Mos6502Compiler.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class Mos6502CompilerTest {
    @Test
    public void testPrefixExtraction(){
        try {
            assertEquals("$", extractFirstOccurrence(ARG_PREFIX_REGEX, "$10"));
            assertEquals("#$", extractFirstOccurrence(ARG_PREFIX_REGEX, "#$10"));
            assertEquals("$", extractFirstOccurrence(ARG_PREFIX_REGEX, "$AA"));
            assertEquals("#$", extractFirstOccurrence(ARG_PREFIX_REGEX, "#$AA"));
            assertEquals("($", extractFirstOccurrence(ARG_PREFIX_REGEX, "($AA,X)"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testValueExtraction(){
        try {
            assertEquals("10", extractFirstOccurrence(ARG_VALUE_REGEX, "$10"));
            assertEquals("10", extractFirstOccurrence(ARG_VALUE_REGEX, "#$10"));
            assertEquals("AA", extractFirstOccurrence(ARG_VALUE_REGEX, "$AA"));
            assertEquals("AA", extractFirstOccurrence(ARG_VALUE_REGEX, "#$AA"));
            assertEquals("AA", extractFirstOccurrence(ARG_VALUE_REGEX, "($AA,X)"));

            assertEquals("A", extractFirstOccurrence(ARG_VALUE_REGEX, "($A,X)"));
            assertEquals("BBB", extractFirstOccurrence(ARG_VALUE_REGEX, "($BBB,X)"));
            assertEquals("CCCC", extractFirstOccurrence(ARG_VALUE_REGEX, "($CCCC,X)"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testPostfixExtraction(){
        try {
            assertEquals(",X", extractFirstOccurrence(ARG_POSTFIX_REGEX, "$10,X"));
            assertEquals(",Y", extractFirstOccurrence(ARG_POSTFIX_REGEX, "$AA,Y"));
            assertEquals(",X)", extractFirstOccurrence(ARG_POSTFIX_REGEX, "($AA,X)"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testInvalidPostfixExtraction(){
        final String[] invalidPostfixes = new String[] {"($AA,Y)"};

        for (String invalidPostfix : invalidPostfixes) {
            assertTrue(extractFirstOccurrence(ARG_POSTFIX_REGEX, invalidPostfix).isEmpty());
        }
    }

    @Test
    public void testLabelExtractionMethod(){
        try {
            assertEquals("TEST:", extractFirstOccurrence(LABEL_REGEX, "TEST:"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testLabelExtractionInCompilation(){
        final Mos6502Compiler compiler = new Mos6502Compiler("MyLabel: SEC");

        final Program program = compiler.compileProgram();
        assertEquals(1, program.getLabels().size());
        assertEquals(0, program.getLocationOf("MyLabel"));
    }

    @Test
    public void testRelativeNavigationBackwards(){
        final Mos6502Compiler compiler = new Mos6502Compiler("MyLabel: SEC BCS MyLabel");

        final int[] expectedResult = new int[] {OpCode.SEC.getByteValue(),
                                                OpCode.BCS.getByteValue(),
                                                0b11111101};

        final Program program = compiler.compileProgram();
        final int[] actualResult = toIntArray(program.getProgramAsByteArray());

        assertTrue("Expected " + Arrays.toString(expectedResult) + ", got " + Arrays.toString(actualResult), Arrays.equals(actualResult, expectedResult));
    }

    @Test
    public void testRelativeNavigationForwards(){
        final Mos6502Compiler compiler = new Mos6502Compiler("SEC BCS MyLabel NOP MyLabel: SED");

        final int[] expectedResult = new int[] {OpCode.SEC.getByteValue(),
                                                OpCode.BCS.getByteValue(),
                                                0b00000001,
                                                OpCode.NOP.getByteValue(),
                                                OpCode.SED.getByteValue()};

        final Program program = compiler.compileProgram();
        final int[] actualResult = toIntArray(program.getProgramAsByteArray());

        assertTrue("Expected " + Arrays.toString(expectedResult) + ", got " + Arrays.toString(actualResult), Arrays.equals(actualResult, expectedResult));
    }

    @Test
    public void testNumericalLabelError(){
        try {
            final Mos6502Compiler compiler = new Mos6502Compiler("MyLabel: SEC BCS 42");
            compiler.compileProgram();
            fail("Expected compilation to fail, '42' is not a valid label");
        }catch (RuntimeException e){
            assertTrue(e.getMessage().contains("BCS"));
            assertTrue(e.getMessage().contains("42"));
        }
    }

    @Test
    public void testImpliedInstructions(){
        OpCode.streamOf(Mos6502AddressingMode.IMPLIED).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName());

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertEquals("Wrong byte value for " + opcode.getOpCodeName() + "(" + opcode.getByteValue() + ")", opcode.getByteValue(), bytes[0]);
        });
    }

    @Test
    public void testSingleDigitArgument(){
        OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "A");

            final Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xA}, bytes);
        });
    }

    @Test
    public void testDoubleDigitArgument(){
        OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "AB");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xAB}, bytes);
        });
    }

    @Test
    public void testTripleDigitArgument(){
        OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "ABC");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xA, 0xBC}, bytes);
        });
    }

    @Test
    public void testQuadrupleDigitArgument(){
        OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "ABCD");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xAB, 0xCD}, bytes);
        });
    }

    @Property
    public void testImmediateInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(Mos6502AddressingMode.IMMEDIATE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + IMMEDIATE_VALUE_PREFIX + hexByte);

            Program program = compiler.compileProgram(); 
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testAccumulatorInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(Mos6502AddressingMode.ACCUMULATOR).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + ACCUMULATOR_PREFIX + hexByte);

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte);

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageXInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE_X).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte+ ",X");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageYInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE_Y).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte + ",Y");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testAbsoluteInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        final int highByte = (wordValue >> 8) & 0xFF;
        final int lowByte = wordValue & 0xFF;

        OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord);

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), highByte, lowByte}, bytes);
        });
    }

    @Property
    public void testAbsoluteXInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        final int highByte = (wordValue >> 8) & 0xFF;
        final int lowByte = wordValue & 0xFF;

        OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE_X).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord + ",X");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + " 0x" + hexWord + "' was wrong.", new int[] {opcode.getByteValue(), highByte, lowByte}, bytes);
        });
    }

    @Property
    public void testAbsoluteYInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        final int highByte = (wordValue >> 8) & 0xFF;
        final int lowByte = wordValue & 0xFF;

        OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE_Y).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord + ",Y");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode.toString() + " 0x" + hexWord + "' was wrong.", new int[] {opcode.getByteValue(), highByte, lowByte}, bytes);
        });
    }

    @Property
    public void testIndirectXInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(Mos6502AddressingMode.INDIRECT_X).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " (" + VALUE_PREFIX + hexByte + ",X)");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals(new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testIndirectYInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(Mos6502AddressingMode.INDIRECT_Y).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " (" + VALUE_PREFIX + hexByte + "),Y");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals(new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Test
    public void testChainedInstruction(){
        final Mos6502Compiler compiler = new Mos6502Compiler("SEC LDA " + IMMEDIATE_VALUE_PREFIX + "47");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {OpCode.SEC.getByteValue(), OpCode.LDA_I.getByteValue(), 0x47};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstruction(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {OpCode.LDA_I.getByteValue(), 0x47, OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstructions(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 CLC LDA " + IMMEDIATE_VALUE_PREFIX + "10 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {OpCode.LDA_I.getByteValue(), 0x47, OpCode.CLC.getByteValue(), OpCode.LDA_I.getByteValue(), 0x10, OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstructionsWithLabel(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 LABELA: CLC LDA " + IMMEDIATE_VALUE_PREFIX + "10 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {OpCode.LDA_I.getByteValue(), 0x47, OpCode.CLC.getByteValue(), OpCode.LDA_I.getByteValue(), 0x10, OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
        assertEquals(1, program.getLabels().size());
        assertEquals(2, program.getLocationOf("LABELA"));
    }

    @Test
    public void testProgram(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA #$14 ADC #$5 STA $20");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {OpCode.LDA_I.getByteValue(), 0x14,
                                    OpCode.ADC_I.getByteValue(), 0x5,
                                    OpCode.STA_Z.getByteValue(), 0x20};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testIntegration(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA #$14 ADC #$5 STA $20");
        Program program = compiler.compileProgram();
        final int[] programByte = toIntArray(program.getProgramAsByteArray());

        Memory memory = new SimpleMemory();
        Mos6502 processor = new Mos6502(memory);
        processor.reset();
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(RoxByte.fromLiteral(0x19), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.fromLiteral(0x19), memory.getByte(RoxWord.fromLiteral(0x20)));
        assertEquals(RoxWord.fromLiteral(programByte.length), registers.getPC());
    }

    @Test
    public void testAbsoluteAddressing(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA #$1C STA $100 INC $100");
        Program program = compiler.compileProgram();
        final int[] programByte = toIntArray(program.getProgramAsByteArray());

        Memory memory = new SimpleMemory();
        Mos6502 processor = new Mos6502(memory);
        processor.reset();
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(3);

        assertEquals(RoxByte.fromLiteral(0x1D), memory.getByte(RoxWord.fromLiteral(0x100)));
    }

    @Test
    public void testInvalidArgument(){
        try {
            final Mos6502Compiler compiler = new Mos6502Compiler("INC $12345");
            Program program = compiler.compileProgram();
            toIntArray(program.getProgramAsByteArray());
            fail("The argument for INC is too long, should throw an error");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
        }
    }

    @Test
    public void firstInvalidOpcode(){
        final Mos6502Compiler compiler = new Mos6502Compiler("ROX");

        try {
            compiler.compileProgram();
            fail("Exception expected, 'ROX' is an invalid OpCode");
        }catch(UnknownTokenException e){
            assertTrue(e.getMessage().contains("ROX"));
            assertNotNull(e);
        }
    }

    @Test
    public void opCode1JaCoCoCoverage(){
        final Mos6502Compiler compiler = new Mos6502Compiler("\0ADC $10");

        try {
            compiler.compileProgram();
            fail("Exception expected.  This should not pass a String switch statement");
        }catch(UnknownTokenException e){
            assertTrue(e.getMessage().contains("\0ADC"));
            assertNotNull(e);
        }
    }

    @Test
    public void opCode2JaCoCoCoverage(){
        final Mos6502Compiler compiler = new Mos6502Compiler("\0BRK");

        try {
            compiler.compileProgram();
            fail("Exception expected.  This should not pass a String switch statement");
        }catch(UnknownTokenException e){
            assertTrue(e.getMessage().contains("\0BRK"));
            assertNotNull(e);
        }
    }

    @Test
    public void testInvalidValuePrefix(){
        try {
            final Mos6502Compiler compiler = new Mos6502Compiler("ADC @$10");
            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());
            fail("Invalid value prefix should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }

    @Test
    public void testInvalidIndirectIndexingMode(){
        try {
            final Mos6502Compiler compiler = new Mos6502Compiler("ADC " + INDIRECT_PREFIX + "10(");
            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());
            fail("Invalid prefix for an indirect value should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }

    @Test
    public void testOversizedValue(){
        try {
            final Mos6502Compiler compiler = new Mos6502Compiler("ADC " + VALUE_PREFIX + "12345");
            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());
            fail("Argument size over " + 0xFFFF + " should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }

    @Test
    public void testInlineCommentRemoval(){
        final Mos6502Compiler compiler = new Mos6502Compiler("SEC ;this should not be parsed\nCLC");
        Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        assertEquals(2, bytes.length);
    }

    @Test
    public void testFullLineCommentRemoval(){
        final Mos6502Compiler compiler = new Mos6502Compiler("SEC \n;this should not be parsed\nCLC");
        Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        assertEquals(2, bytes.length);
    }

    private int[] toIntArray(RoxByte[] actualResult) {
        int i=0;
        final int[] comparableReault = new int[actualResult.length];
        for (RoxByte roxByte : actualResult) {
            comparableReault[i++] = roxByte.getRawValue();
        }
        return comparableReault;
    }
}
