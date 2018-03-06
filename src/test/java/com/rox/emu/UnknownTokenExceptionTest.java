package com.rox.emu;

import com.rox.emu.processor.mos6502.op.OpCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class UnknownTokenExceptionTest {
    @Test
    public void testCreationWithOpCode(){
        UnknownOpCodeException e = new UnknownOpCodeException("This is my reason", "UNKNOWN_TOKEN");

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
        assertNotNull(e.getOpCode());
        assertEquals(OpCode.ADC_ABS, e.getOpCode());
    }
}
