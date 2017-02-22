package com.rox.emu.p6502.op;

import com.rox.emu.UnknownOpCodeException;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class OpCodeNameConverterTest {
    @Test
    public void testValidOpcode(){
        String description = OpCodeNameConverter.toDescription("OP_SEC");
        assertNotNull(description);
        assertFalse(description.isEmpty());
    }

    @Test
    public void testInvalidOpcode(){
        try {
            String description = OpCodeNameConverter.toDescription("PO_SEC");
            fail("Invalid OpCode should throw an exception, got " + description);
        }catch(UnknownOpCodeException e){
            assertNotNull(e.getMessage());
            assertNotNull(e.getOpCode());
            assertEquals("PO_SEC", e.getOpCode());
        }
    }

    @Test
    public void testValidImpliedOpCode(){
        String description = OpCodeNameConverter.toDescription("OP_SEC");
        assertEquals("SEC (" + OpCodeNameConverter.ADDR_IMP + ")", description);
    }

    @Test
    @Ignore //Need to think of a nice way of validating existing opcodes
    public void testInvalidImpliedOpCode(){
        try {
            String description = OpCodeNameConverter.toDescription("OP_SEX");
            fail("Invalid OpCode should throw an exception, got " + description);
        }catch(UnknownOpCodeException e){
            assertNotNull(e.getMessage());
            assertNotNull(e.getOpCode());
            assertEquals("PO_SEC", e.getOpCode());
        }
    }

    @Test
    public void testValidImmediateOpCode(){
        String description = OpCodeNameConverter.toDescription("OP_ORA_I");
        assertEquals("ORA (" + OpCodeNameConverter.ADDR_I + ")", description);
    }

    //XXX Add invalidImmediate

    @Test
    public void testValidAccumulatorOpCode(){
        String description = OpCodeNameConverter.toDescription("OP_LSR_A");
        assertEquals("LSR (" + OpCodeNameConverter.ADDR_A + ")", description);
    }

    //XXX Add invalidAccumulator

    @Test
    public void testValidZeroPageOpCode(){
        String description = OpCodeNameConverter.toDescription("OP_LSR_Z");
        assertEquals("LSR (" + OpCodeNameConverter.ADDR_Z + ")", description);
    }

    //XXX Add invalidZeroPage

    @Test
    public void testValidAbsoluteOpCode(){
        String description = OpCodeNameConverter.toDescription("OP_LSR_ABS");
        assertEquals("LSR (" + OpCodeNameConverter.ADDR_ABS + ")", description);
    }

    //XXX Add invalidAbsolute

    @Test
    public void testValidXIndexedOpCode(){
        String description = OpCodeNameConverter.toDescription("OP_LSR_Z_IX");
        assertEquals("LSR (" + OpCodeNameConverter.ADDR_Z + OpCodeNameConverter.INDEX_X + ")", description);
    }

    @Test
    public void testValidYIndexedOpCode(){
        String description = OpCodeNameConverter.toDescription("OP_LSR_Z_IY");
        assertEquals("LSR (" + OpCodeNameConverter.ADDR_Z + OpCodeNameConverter.INDEX_Y + ")", description);
    }

    //XXX Add invalid indexed

}
