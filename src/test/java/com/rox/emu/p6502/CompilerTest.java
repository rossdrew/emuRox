package com.rox.emu.p6502;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * # - Memory
 * $ - Value
 *
 *  #$V               - Immediate
 *  #VV               - Accumulator
 *  $V / $ VV         - Zero Page
 *  $V,X / $VV,X      - Zero Page[X]
 *  $V,Y / $VV,Y      - Zero Page[Y]
 *  $VVV / $VVVV      - Absolute
 *  $VVV,X / $VVVV,X  - Absolute[X]
 *  $VVV,Y / $VVVV,Y  - Absolute[Y]
 *  ($V,X) / ($VV,X)  - Indirect, X
 *  ($V),Y / ($VV),Y  - Indirect, Y
 *
 *  | $[ V_Z | V_ABS ] ]
 */
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
    public void testInvalidValuePrefix(){
        try {
            Compiler compiler = new Compiler("ADC @$10");
            int[] bytes = compiler.getBytes();
            fail("Invalid argument structure should throw an exception but was " + Arrays.toString(bytes));
        }catch (UnknownOpCodeException e){
            assertFalse(e.getMessage().isEmpty());
            assertFalse(e.getOpCode() == null);
        }
    }


}
