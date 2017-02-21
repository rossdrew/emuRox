package com.rox.emu.p6502.op;

import org.junit.Test;

public class OpCodeNameConverterTest {
    @Test
    public void testValidOpcode(){
        String description = OpCodeNameConverter.toDescription("OP_SEC");
        System.out.println(description);
    }
}
