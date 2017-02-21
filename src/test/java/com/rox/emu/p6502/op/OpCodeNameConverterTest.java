package com.rox.emu.p6502.op;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class OpCodeNameConverterTest {
    @Test
    public void testValidOpcode(){
        String description = OpCodeNameConverter.toDescription("OP_SEC");
        assertFalse(description == null);
        assertFalse(description.isEmpty());
    }
}
