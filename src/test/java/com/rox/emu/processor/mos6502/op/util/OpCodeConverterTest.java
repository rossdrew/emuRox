package com.rox.emu.processor.mos6502.op.util;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.op.util.util.OpCodeConverter;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

public class OpCodeConverterTest {
    @Test
    public void testCreation(){
        //Class definitions need to be instantiated in tests in order to be counted in code coverage.
        // As this is a static utility class for now, we need to instantiate it to cover that edge case.
        assertNotNull(new OpCodeConverter());
    }

    @Test
    public void testValidOpcode(){
        String description = OpCodeConverter.toDescription("OP_SEC");
        assertNotNull(description);
        assertFalse(description.isEmpty());
    }

    @Test
    public void testNullOpcode(){
        try {
            String description = OpCodeConverter.toDescription(null);
            fail("Invalid OpCode should throw an exception, got " + description);
        }catch(UnknownOpCodeException e){
            assertNotNull(e.getMessage());
            assertNull(e.getOpCode());
        }
    }

    @Test
    public void testEmptyOpcode(){
        try {
            String description = OpCodeConverter.toDescription("");
            fail("Invalid OpCode should throw an exception, got " + description);
        }catch(UnknownOpCodeException e){
            assertNotNull(e.getMessage());
            assertEquals("", e.getOpCode());
        }
    }

    @Test
    public void testInvalidOpcode(){
        try {
            String description = OpCodeConverter.toDescription("PO_SEC");
            fail("Invalid OpCode should throw an exception, got " + description);
        }catch(UnknownOpCodeException e){
            assertNotNull(e.getMessage());
            assertNotNull(e.getOpCode());
            assertEquals("PO_SEC", e.getOpCode());
        }
    }

    @Test
    public void testValidImpliedOpCode(){
        String description = OpCodeConverter.toDescription("OP_SEC");
        assertEquals("SEC (" + OpCodeConverter.ADDR_IMP + ")", description);
    }

    @Test
    public void testValidImmediateOpCode(){
        String description = OpCodeConverter.toDescription("OP_ORA_I");
        assertEquals("ORA (" + OpCodeConverter.ADDR_I + ")", description);
    }

    @Test
    public void testValidAccumulatorOpCode(){
        String description = OpCodeConverter.toDescription("OP_LSR_A");
        assertEquals("LSR (" + OpCodeConverter.ADDR_A + ")", description);
    }

    @Test
    public void testValidZeroPageOpCode(){
        String description = OpCodeConverter.toDescription("OP_LSR_Z");
        assertEquals("LSR (" + OpCodeConverter.ADDR_Z + ")", description);
    }

    @Test
    public void testValidAbsoluteOpCode(){
        String description = OpCodeConverter.toDescription("OP_LSR_ABS");
        assertEquals("LSR (" + OpCodeConverter.ADDR_ABS + ")", description);
    }

    @Test
    public void testUnknownAddressingMode(){
        try {
            String description = OpCodeConverter.toDescription("OP_LSR_XXX");
            fail("Invalid OpCode should throw an exception, got " + description);
        }catch(UnknownOpCodeException e){
            assertNotNull(e.getMessage());
            assertNotNull(e.getOpCode());
            assertEquals("[OP, LSR, XXX]", e.getOpCode());
        }
    }

    @Test
    public void testValidXIndexedOpCode(){
        String description = OpCodeConverter.toDescription("OP_LSR_Z_IX");
        assertEquals("LSR (" + OpCodeConverter.ADDR_Z + OpCodeConverter.INDEX_X + ")", description);
    }

    @Test
    public void testValidYIndexedOpCode(){
        String description = OpCodeConverter.toDescription("OP_LSR_Z_IY");
        assertEquals("LSR (" + OpCodeConverter.ADDR_Z + OpCodeConverter.INDEX_Y + ")", description);
    }

    @Test
    public void testUnknownIndexingMode(){
        try {
            String description = OpCodeConverter.toDescription("OP_LSR_Z_IZ");
            fail("Invalid OpCode should throw an exception, got " + description);
        }catch(UnknownOpCodeException e){
            assertNotNull(e.getMessage());
            assertNotNull(e.getOpCode());
            assertEquals("[OP, LSR, Z, IZ]", e.getOpCode());
        }
    }
}
