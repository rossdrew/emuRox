package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;
import org.junit.Test;

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
public class CompilerTest {
    @Test
    public void testImpliedInstructions(){
        OpCode.streamOf(AddressingMode.IMPLIED).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName());

            int[] bytes = compiler.getBytes();

            assertEquals("Wrong byte value for " + opcode.getOpCodeName() + "(" + opcode.getByteValue() + ")", opcode.getByteValue(), bytes[0]);
        });
    }

    @Test
    public void testImmediateInstructions(){
        OpCode.streamOf(AddressingMode.IMMEDIATE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.IMMEDIATE_VALUE_PREFIX + "10");

            int[] bytes = compiler.getBytes();

            assertArrayEquals(new int[] {opcode.getByteValue(), 10}, bytes);
        });
    }

    @Test
    public void testAccumulatorInstructions(){
        OpCode.streamOf(AddressingMode.ACCUMULATOR).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.IMMEDIATE_PREFIX + "10");

            int[] bytes = compiler.getBytes();

            assertArrayEquals(new int[] {opcode.getByteValue(), 10}, bytes);
        });
    }

    @Test
    public void testZeroPageInstructions(){
        //TODO Test single character argument
        OpCode.streamOf(AddressingMode.ZERO_PAGE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + "10");

            int[] bytes = compiler.getBytes();

            assertArrayEquals(new int[] {opcode.getByteValue(), 10}, bytes);
        });
    }

    @Test
    public void testZeroPageXInstructions(){
        //TODO Test single character argument
        OpCode.streamOf(AddressingMode.ZERO_PAGE_X).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + "10,X");

            int[] bytes = compiler.getBytes();

            assertArrayEquals(new int[] {opcode.getByteValue(), 10}, bytes);
        });
    }

    @Test
    public void testAbsoluteInstructions(){
        //TODO test three character argument
        OpCode.streamOf(AddressingMode.ABSOLUTE).forEach((opcode)->{
            Compiler compiler = new Compiler(opcode.getOpCodeName() + " " + Compiler.VALUE_PREFIX + "1234");

            int[] bytes = compiler.getBytes();

            assertArrayEquals(new int[] {opcode.getByteValue(), 1234}, bytes);
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
