package com.rox.emu.p6502;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OpCodeTest {
    @Test
    public void testOpcodeValues(){
        for (OpCode o : OpCode.values()){
            assertTrue("OpCode byte value (" + o.getByteValue() + ") is not within byte range (0x0-0xFF)", o.getByteValue() > 0x0 && o.getByteValue() < 0x100);
        }
    }

    @Test
    public void testOpcodeDescriptions(){
        //XXX Make it match "XXX _ ( .* [x|y] )"
        for (OpCode o : OpCode.values()){
            assertTrue(!o.toString().isEmpty());
        }
    }
}
