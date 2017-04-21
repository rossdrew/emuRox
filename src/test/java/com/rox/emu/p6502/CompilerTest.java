package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import org.junit.Test;

import static com.rox.emu.p6502.op.OpCode.OP_SEC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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
    public void firstTest(){
        Compiler compiler = new Compiler("SEC");

        int[] bytes = compiler.getBytes();

        assertEquals(bytes[0], OP_SEC.getByteValue());
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
