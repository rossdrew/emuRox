package com.rox.emu.p6502;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OpCodeTest {
    @Test
    public void testOpcodes(){
        for (OpCode o : OpCode.values()){
            assertTrue(!o.toString().isEmpty());
            assertTrue("OpCode byte value (" + o.getByteValue() + ") is not within byte range (0x0-0xFF)", o.getByteValue() > 0x0 && o.getByteValue() < 0x100);
        }
    }
}
