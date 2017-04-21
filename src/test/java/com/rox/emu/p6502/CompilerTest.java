package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import org.junit.Test;

import static com.rox.emu.p6502.op.OpCode.OP_ADC_I;
import static com.rox.emu.p6502.op.OpCode.OP_SEC;
import static org.junit.Assert.*;

/**
 * # - Memory
 * $ - Value
 *
 *  #$V               - Immediate
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
    public void testImpliedInstruction(){
        Compiler compiler = new Compiler(OP_SEC.getOpCodeName());

        int[] bytes = compiler.getBytes();

        assertEquals(bytes[0], OP_SEC.getByteValue());
    }

    @Test
    public void testImmediateInstruction(){
        Compiler compiler = new Compiler(OP_ADC_I.getOpCodeName() + " " + "#$10");

        int[] bytes = compiler.getBytes();

        assertArrayEquals(new int[] {OP_ADC_I.getByteValue(), 10}, bytes);
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
}
