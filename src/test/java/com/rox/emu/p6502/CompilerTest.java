package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import org.junit.Test;

import static com.rox.emu.p6502.op.OpCode.OP_SEC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


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
