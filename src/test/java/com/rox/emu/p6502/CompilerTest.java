package com.rox.emu.p6502;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.Memory;
import com.rox.emu.SimpleMemory;
import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.rox.emu.p6502.Compiler.INDIRECT_PREFIX;
import static com.rox.emu.p6502.Compiler.VALUE_PREFIX;
import static com.rox.emu.p6502.InstructionSet.OP_LDA_I;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class CompilerTest {
    @Test
    public void testPrefixExtraction(){
        try {
            assertEquals("$", Compiler.extractFirstOccurrence(Compiler.PREFIX_REGEX, "$10", "LDA"));
            assertEquals("#$", Compiler.extractFirstOccurrence(Compiler.PREFIX_REGEX, "#$10", "ADC"));
            assertEquals("$", Compiler.extractFirstOccurrence(Compiler.PREFIX_REGEX, "$AA", "LDA"));
            assertEquals("#$", Compiler.extractFirstOccurrence(Compiler.PREFIX_REGEX, "#$AA", "ADC"));
            assertEquals("($", Compiler.extractFirstOccurrence(Compiler.PREFIX_REGEX, "($AA,X)", "ADC"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testValueExtraction(){
        try {
            assertEquals("10", Compiler.extractFirstOccurrence(Compiler.VALUE_REGEX, "$10", "LDA"));
            assertEquals("10", Compiler.extractFirstOccurrence(Compiler.VALUE_REGEX, "#$10", "LDA"));
            assertEquals("AA", Compiler.extractFirstOccurrence(Compiler.VALUE_REGEX, "$AA", "LDA"));
            assertEquals("AA", Compiler.extractFirstOccurrence(Compiler.VALUE_REGEX, "#$AA", "LDA"));
            assertEquals("AA", Compiler.extractFirstOccurrence(Compiler.VALUE_REGEX, "($AA,X)", "ADC"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testPostfixExtraction(){
        try {
            assertEquals(",X", Compiler.extractFirstOccurrence(Compiler.POSTFIX_REGEX, "$10,X", "LDA"));
            assertEquals(",Y", Compiler.extractFirstOccurrence(Compiler.POSTFIX_REGEX, "$AA,Y", "LDA"));
            assertEquals(",X)", Compiler.extractFirstOccurrence(Compiler.POSTFIX_REGEX, "($AA,X)", "ADC"));
        }catch (UnknownOpCodeException e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testImpliedInstructions(){
        OpCode.streamOf(AddressingMode.IMPLIED).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName());

            int[] bytes = compiler.getBytes();

            assertEquals("Wrong byte value for " + opcode.getOpCodeName() + "(" + opcode.getByteValue() + ")", opcode.getByteValue(), bytes[0]);
        });
    }

    @Test
    public void testSingleDigitArgument(){
        OpCode.streamOf(AddressingMode.ZERO_PAGE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + "A");

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xA}, bytes);
        });
    }

    @Test
    public void testDoubleDigitArgument(){
        OpCode.streamOf(AddressingMode.ZERO_PAGE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + "AB");

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xAB}, bytes);
        });
    }

    @Test
    public void testTripleDigitArgument(){
        OpCode.streamOf(AddressingMode.ABSOLUTE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + "ABC");

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xABC}, bytes);
        });
    }

    @Test
    public void testQuadrupleDigitArgument(){
        OpCode.streamOf(AddressingMode.ABSOLUTE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + "ABCD");

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), 0xABCD}, bytes);
        });
    }

    @Property
    public void testImmediateInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.IMMEDIATE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.IMMEDIATE_VALUE_PREFIX + hexByte);

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testAccumulatorInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.ACCUMULATOR).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.IMMEDIATE_PREFIX + hexByte);

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.ZERO_PAGE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + hexByte);

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageXInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.ZERO_PAGE_X).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + hexByte+ ",X");

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testZeroPageYInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.ZERO_PAGE_Y).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + hexByte + ",Y");

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testAbsoluteInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        OpCode.streamOf(AddressingMode.ABSOLUTE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + hexWord);

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + "' was wrong.", new int[] {opcode.getByteValue(), wordValue}, bytes);
        });
    }

    @Property
    public void testAbsoluteXInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        OpCode.streamOf(AddressingMode.ABSOLUTE_X).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + hexWord + ",X");

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + " 0x" + hexWord + "' was wrong.", new int[] {opcode.getByteValue(), wordValue}, bytes);
        });
    }

    @Property
    public void testAbsoluteYInstructions(@InRange(min = "256", max = "65535") int wordValue){
        final String hexWord = Integer.toHexString(wordValue);

        OpCode.streamOf(AddressingMode.ABSOLUTE_Y).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + hexWord + ",Y");

            int[] bytes = compiler.getBytes();

            assertArrayEquals("Output for '" + opcode.toString() + " 0x" + hexWord + "' was wrong.", new int[] {opcode.getByteValue(), wordValue}, bytes);
        });
    }

    @Property
    public void testIndirectXInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.INDIRECT_X).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " (" + Compiler.VALUE_PREFIX + hexByte + ",X)");

            int[] bytes = compiler.getBytes();

            assertArrayEquals(new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Property
    public void testIndirectYInstructions(@InRange(min = "0", max = "255") int byteValue){
        final String hexByte = Integer.toHexString(byteValue);

        OpCode.streamOf(AddressingMode.INDIRECT_Y).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " (" + Compiler.VALUE_PREFIX + hexByte + "),Y");

            int[] bytes = compiler.getBytes();

            assertArrayEquals(new int[] {opcode.getByteValue(), byteValue}, bytes);
        });
    }

    @Test
    public void testChainedInstruction(){
        Compiler compiler = new Compiler("SEC LDA " + Compiler.IMMEDIATE_VALUE_PREFIX + "47");
        final int[] bytes = compiler.getBytes();

        int[] expected = new int[] {OpCode.OP_SEC.getByteValue(), OpCode.OP_LDA_I.getByteValue(), 0x47};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstruction(){
        Compiler compiler = new Compiler("LDA " + Compiler.IMMEDIATE_VALUE_PREFIX + "47 SEC");
        final int[] bytes = compiler.getBytes();

        int[] expected = new int[] {OpCode.OP_LDA_I.getByteValue(), 0x47, OpCode.OP_SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testChainedTwoByteInstructions(){
        Compiler compiler = new Compiler("LDA " + Compiler.IMMEDIATE_VALUE_PREFIX + "47 CLC LDA " + Compiler.IMMEDIATE_VALUE_PREFIX + "10 SEC");
        final int[] bytes = compiler.getBytes();

        int[] expected = new int[] {OpCode.OP_LDA_I.getByteValue(), 0x47, OpCode.OP_CLC.getByteValue(), OpCode.OP_LDA_I.getByteValue(), 0x10, OpCode.OP_SEC.getByteValue()};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testProgram(){
        Compiler compiler = new Compiler("LDA #$14 ADC #$5 STA $20");
        final int[] bytes = compiler.getBytes();

        int[] expected = new int[] {OpCode.OP_LDA_I.getByteValue(), 0x14,
                                    OpCode.OP_ADC_I.getByteValue(), 0x5,
                                    OpCode.OP_STA_Z.getByteValue(), 0x20};
        assertArrayEquals("Expected: " + Arrays.toString(expected) + ", Got: " + Arrays.toString(bytes), expected, bytes);
    }

    @Test
    public void testIntegration(){
        Compiler compiler = new Compiler("LDA #$14 ADC #$5 STA $20");
        final int[] program = compiler.getBytes();

        Memory memory = new SimpleMemory();
        CPU processor = new CPU(memory);
        processor.reset();
        memory.setMemory(0, program);

        processor.step(3);

        Registers registers = processor.getRegisters();
        assertEquals(0x19, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(0x19, memory.getByte(0x20));
        assertEquals(program.length, registers.getPC());
    }

    @Test
    public void firstInvalidOpcode(){
        Compiler compiler = new Compiler("ROX");

        try {
            compiler.getBytes();
            fail("Exception expected, 'ROX' is an invalid OpCode");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
        }
    }

    @Test
    public void opCodeJaCoCoCoverage(){
        Compiler compiler = new Compiler("\0ADC $10");

        try {
            compiler.getBytes();
            fail("Exception expected.  This should not pass a String switch statement");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testInvalidValuePrefix(){
        try {
            Compiler compiler = new Compiler("ADC @$10");
            int[] bytes = compiler.getBytes();
            fail("Invalid value prefix should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }

    @Test
    public void testInvalidIndirectIndexingMode(){
        try {
            Compiler compiler = new Compiler("ADC " + INDIRECT_PREFIX + "10(");
            int[] bytes = compiler.getBytes();
            fail("Invalid prefix for an indirect value should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }

    @Test
    public void testOversizedValue(){
        try {
            Compiler compiler = new Compiler("ADC " + VALUE_PREFIX + "12345");
            int[] bytes = compiler.getBytes();
            fail("Argument size over " + 0xFFFF + " should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }
}
