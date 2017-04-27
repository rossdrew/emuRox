package com.rox.emu.p6502;

import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OpCodeTest {
    @Test
    public void testOpcodeValues(){
        for (OpCode o : OpCode.values()){
            assertTrue("OpCode byte value (" + o.getByteValue() + ") is not within byte range (0x0-0xFF)", o.getByteValue() >= 0x0 && o.getByteValue() < 0x100);
        }
    }

    @Test
    public void testOpcodeDescriptions(){
        //XXX Make it match "XXX _ ( .* [x|y] )"
        for (OpCode o : OpCode.values()){
            assertFalse(o.toString().isEmpty());
        }
    }

    @Test
    public void testOpcodeName(){
        for (OpCode o : OpCode.values()){
            assertFalse(o.getOpCodeName().isEmpty());
        }
    }

    @Test
    public void testFromOpcodeName(){
        for (OpCode o : OpCode.values()){
            if (o.getAddressingMode() == AddressingMode.IMPLIED)
                assertEquals(o, OpCode.from(o.getOpCodeName()));
            assertEquals(o, OpCode.from(o.getOpCodeName(), o.getAddressingMode()));
        }
    }


    @Test
    public void testFromOpcode(){
        for (OpCode o : OpCode.values()){
            OpCode op = OpCode.from(o.getByteValue());
            assertEquals("0x" + Integer.toHexString(o.getByteValue()) + " == " + o + " != " + op + " (0x" + Integer.toHexString(op.getByteValue()) + ")", o, op);
        }
    }

    @Test
    public void testStreamOf(){
        OpCode.streamOf(AddressingMode.IMPLIED).forEach((opcode)->assertEquals(opcode, OpCode.from(opcode.getOpCodeName())));
        OpCode.streamOf(AddressingMode.ZERO_PAGE).forEach((opcode)->assertEquals(opcode, OpCode.from(opcode.getOpCodeName(), opcode.getAddressingMode())));
    }
}
