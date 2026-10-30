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
import com.rox.emu.processor.mos6502.op.Mos6502OpCode;
import com.rox.emu.processor.mos6502.util.Mos6502Compiler;
import com.rox.emu.processor.mos6502.util.Program;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.rox.emu.processor.mos6502.util.Mos6502Compiler.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class Mos6502CompilerTest {
    static Stream<Arguments> invalidPrograms() {
        return Stream.of(
                Arguments.of("invalid argument", "INC $12345", UnknownOpCodeException.class, "Invalid or unimplemented argument: '$12345'"),
                Arguments.of("invalid opcode", "ROX", UnknownTokenException.class, "Unknown token (\"ROX\") while parsing program"),
                Arguments.of("invalid character (to satisfy JaCoCo 1/2)", "\0ADC $10", UnknownTokenException.class, "Unknown token (\"\0ADC\") while parsing program"),
                Arguments.of("invalid character (to satisfy JaCoCo 2/2)", "\0BRK", UnknownTokenException.class, "Unknown token (\"\0BRK\") while parsing program"),
                Arguments.of("invalid argument prefix", "ADC @$10", UnknownOpCodeException.class, "Invalid or unimplemented argument: '@$10'"),
                Arguments.of("invalid indirect index addressed argument", "ADC " + INDIRECT_PREFIX + "10(", UnknownOpCodeException.class, "Invalid or unimplemented argument: '($10'"),
                Arguments.of("oversized argument", "ADC " + VALUE_PREFIX + "12345", UnknownOpCodeException.class, "Invalid or unimplemented argument: '$12345'")
                //Arguments.of("", "", Class.class, "")
        );
    }

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

        final int[] expectedResult = new int[] {Mos6502OpCode.SEC.getByteValue(),
                                                Mos6502OpCode.BCS.getByteValue(),
                                                0b11111101};

        final Program program = compiler.compileProgram();
        final int[] actualResult = toIntArray(program.getProgramAsByteArray());

        assertArrayEquals("Expected " + Arrays.toString(expectedResult) + ", got " + Arrays.toString(actualResult), actualResult, expectedResult);
    }

    @Test
    public void testRelativeNavigationForwards(){
        final Mos6502Compiler compiler = new Mos6502Compiler("SEC BCS MyLabel NOP MyLabel: SED");

        final int[] expectedResult = new int[] {Mos6502OpCode.SEC.getByteValue(),
                                                Mos6502OpCode.BCS.getByteValue(),
                                                0b00000001,
                                                Mos6502OpCode.NOP.getByteValue(),
                                                Mos6502OpCode.SED.getByteValue()};

        final Program program = compiler.compileProgram();
        final int[] actualResult = toIntArray(program.getProgramAsByteArray());

        assertArrayEquals("Expected " + Arrays.toString(expectedResult) + ", got " + Arrays.toString(actualResult), actualResult, expectedResult);
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
        Mos6502OpCode.streamOf(Mos6502AddressingMode.IMPLIED).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName());

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertEquals("Wrong byte value for " + opcode.getOpCodeName() + "(" + opcode.getByteValue() + ")", opcode.getByteValue(), bytes[0]);
        });
    }

    @Test
    public void testSingleDigitArgument(){
        Mos6502OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "A");

            final Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), 0xA}, bytes);
        });
    }

    @Test
    public void testDoubleDigitArgument(){
        Mos6502OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "AB");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), 0xAB}, bytes);
        });
    }

    @Test
    public void testTripleDigitArgument(){
        Mos6502OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "ABC");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), 0xA, 0xBC}, bytes);
        });
    }

    @Test
    public void testQuadrupleDigitArgument(){
        Mos6502OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + "ABCD");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), 0xAB, 0xCD}, bytes);
        });
    }

    @Property
    public void testImmediateInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        Mos6502OpCode.streamOf(Mos6502AddressingMode.IMMEDIATE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + IMMEDIATE_VALUE_PREFIX + hexByte);

            Program program = compiler.compileProgram(); 
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testAccumulatorInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        Mos6502OpCode.streamOf(Mos6502AddressingMode.ACCUMULATOR).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + ACCUMULATOR_PREFIX + hexByte);

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        Mos6502OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte);

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageXInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        Mos6502OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE_X).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte+ ",X");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageYInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        Mos6502OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE_Y).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexByte + ",Y");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testAbsoluteInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        final int highByte = (wordValue >> 8) & 0xFF;
        final int lowByte = wordValue & 0xFF;

        Mos6502OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord);

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + "' was wrong.", new int[] {opcode.getByteValue(), highByte, lowByte}, bytes);
        });
    }

    @Property
    public void testAbsoluteXInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        final int highByte = (wordValue >> 8) & 0xFF;
        final int lowByte = wordValue & 0xFF;

        Mos6502OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE_X).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord + ",X");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + " 0x" + hexWord + "' was wrong.", new int[] {opcode.getByteValue(), highByte, lowByte}, bytes);
        });
    }

    @Property
    public void testAbsoluteYInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        final int highByte = (wordValue >> 8) & 0xFF;
        final int lowByte = wordValue & 0xFF;

        Mos6502OpCode.streamOf(Mos6502AddressingMode.ABSOLUTE_Y).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " " + VALUE_PREFIX + hexWord + ",Y");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals("Output for '" + opcode + " 0x" + hexWord + "' was wrong.", new int[] {opcode.getByteValue(), highByte, lowByte}, bytes);
        });
    }

    @Property
    public void testIndirectXInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        Mos6502OpCode.streamOf(Mos6502AddressingMode.INDIRECT_X).forEach((opcode)->{
            final Mos6502Compiler compiler = new Mos6502Compiler(opcode.getOpCodeName() + " (" + VALUE_PREFIX + hexByte + ",X)");

            Program program = compiler.compileProgram();
            int[] bytes = toIntArray(program.getProgramAsByteArray());

            assertArrayEquals(new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testIndirectYInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        Mos6502OpCode.streamOf(Mos6502AddressingMode.INDIRECT_Y).forEach((opcode)->{
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

        int[] expected = new int[] {Mos6502OpCode.SEC.getByteValue(), Mos6502OpCode.LDA_I.getByteValue(), 0x47};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstruction(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {Mos6502OpCode.LDA_I.getByteValue(), 0x47, Mos6502OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstructions(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 CLC LDA " + IMMEDIATE_VALUE_PREFIX + "10 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {Mos6502OpCode.LDA_I.getByteValue(), 0x47, Mos6502OpCode.CLC.getByteValue(), Mos6502OpCode.LDA_I.getByteValue(), 0x10, Mos6502OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstructionsWithLabel(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA " + IMMEDIATE_VALUE_PREFIX + "47 LABELA: CLC LDA " + IMMEDIATE_VALUE_PREFIX + "10 SEC");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {Mos6502OpCode.LDA_I.getByteValue(), 0x47, Mos6502OpCode.CLC.getByteValue(), Mos6502OpCode.LDA_I.getByteValue(), 0x10, Mos6502OpCode.SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
        assertEquals(1, program.getLabels().size());
        assertEquals(2, program.getLocationOf("LABELA"));
    }

    @Test
    public void testProgram(){
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA #$14 ADC #$5 STA $20");
        final Program program = compiler.compileProgram();
        int[] bytes = toIntArray(program.getProgramAsByteArray());

        int[] expected = new int[] {Mos6502OpCode.LDA_I.getByteValue(), 0x14,
                                    Mos6502OpCode.ADC_I.getByteValue(), 0x5,
                                    Mos6502OpCode.STA_Z.getByteValue(), 0x20};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testIntegration(){
        final Registers registers = new Registers();
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA #$14 ADC #$5 STA $20");
        Program program = compiler.compileProgram();
        final int[] programByte = toIntArray(program.getProgramAsByteArray());

        Memory memory = new SimpleMemory();
        Mos6502 processor = new Mos6502(memory,registers);
        processor.reset();
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(3);

        assertEquals(RoxByte.fromLiteral(0x19), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.fromLiteral(0x19), memory.getByte(RoxWord.fromLiteral(0x20)));
        assertEquals(RoxWord.fromLiteral(programByte.length), registers.getPC());
    }

    @Test
    public void testAbsoluteAddressing(){
        final Registers registers = new Registers();
        final Mos6502Compiler compiler = new Mos6502Compiler("LDA #$1C STA $100 INC $100");
        Program program = compiler.compileProgram();
        final int[] programByte = toIntArray(program.getProgramAsByteArray());

        Memory memory = new SimpleMemory();
        Mos6502 processor = new Mos6502(memory, registers);
        processor.reset();
        memory.setBlock(RoxWord.ZERO, program.getProgramAsByteArray());

        processor.step(3);

        assertEquals(RoxByte.fromLiteral(0x1D), memory.getByte(RoxWord.fromLiteral(0x100)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPrograms")
    void invalidRomIsRejected(String description, String program, Class<RuntimeException> exception, String expectedMessage) {
        final Mos6502Compiler compiler = new Mos6502Compiler(program);
        final RuntimeException e = assertThrows(exception, compiler::compileProgram);
        assertEquals(expectedMessage, e.getMessage());
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
