package com.rox.emu;

import com.rox.emu.processor.mos6502.op.Mos6502OpCode;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnknownOpcodeExceptionTest {
    @Test
    public void testCreationWithOpID(){
        UnknownOpCodeException e = new UnknownOpCodeException("This is my reason", 1234);

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
        assertNotNull(e.getOpCode());
        assertEquals("1234", e.getOpCode());
    }

    @Test
    public void testCreationWithOpCode(){
        UnknownOpCodeException e = new UnknownOpCodeException("This is my reason", Mos6502OpCode.ADC_ABS);

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
        assertNotNull(e.getOpCode());
        assertEquals(Mos6502OpCode.ADC_ABS.toString(), e.getOpCode());
    }

    @Test
    public void testCausedException(){
        Exception cause = new Exception();
        UnknownOpCodeException e = new UnknownOpCodeException("This is my reason", Mos6502OpCode.ADC_ABS, cause);

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
        assertNotNull(e.getOpCode());
        assertEquals(Mos6502OpCode.ADC_ABS.toString(), e.getOpCode());
        assertEquals(cause, e.getCause());
    }
}
