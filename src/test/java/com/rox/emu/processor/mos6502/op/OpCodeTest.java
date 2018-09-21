package com.rox.emu.processor.mos6502.op;

import com.rox.emu.UnknownOpCodeException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.spockframework.util.Assert.fail;

public class OpCodeTest {
    @Test
    public void testOpcodeValues(){
        for (Mos6502OpCode o : Mos6502OpCode.values()){
            assertTrue("OpCode byte value (" + o.getByteValue() + ") is not within byte range (0x0-0xFF)", o.getByteValue() >= 0x0 && o.getByteValue() < 0x100);
        }
    }

    @Test
    public void testOpcodeDescriptions(){
        //XXX Make it match "XXX _ ( .* [x|y] )"
        for (Mos6502OpCode o : Mos6502OpCode.values()){
            assertFalse(o.toString().isEmpty());
        }
    }

    @Test
    public void testOpcodeName(){
        for (Mos6502OpCode o : Mos6502OpCode.values()){
            assertFalse(o.getOpCodeName().isEmpty());
        }
    }

    @Test
    public void testFromOpcodeName(){
        for (Mos6502OpCode o : Mos6502OpCode.values()){
            if (o.getAddressingMode() == Mos6502AddressingMode.IMPLIED)
                assertEquals(o, Mos6502OpCode.from(o.getOpCodeName()));
            assertEquals(o, Mos6502OpCode.from(o.getOpCodeName(), o.getAddressingMode()));
        }
    }

    @Test
    public void testInvalidFromOpcodeName(){
        try{
            Mos6502OpCode.from("ROX", Mos6502AddressingMode.ABSOLUTE);
            fail("ROX is not a valid OpCode");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
            assertFalse(e.getMessage().isEmpty());
        }
    }

    @Test
    public void testInvalidFromOpcodeNameAndAddressingMode(){
        try{
            Mos6502OpCode.from("ADC", Mos6502AddressingMode.IMPLIED);
            fail("ADC cannot be IMPLIED addressed");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
            assertFalse(e.getMessage().isEmpty());
        }
    }

    @Test
    public void testFromOpcode(){
        for (Mos6502OpCode o : Mos6502OpCode.values()){
            Mos6502OpCode op = Mos6502OpCode.from(o.getByteValue());
            assertEquals("0x" + Integer.toHexString(o.getByteValue()) + " == " + o + " != " + op + " (0x" + Integer.toHexString(op.getByteValue()) + ")", o, op);
        }
    }

    @Test
    public void testFromInvalidOpcode(){
        try {
            Mos6502OpCode.from(0x92);
            fail("Invalid op-code byte cannot be converted to OpCode");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testFromInvalidOpcodeName(){
        try {
            Mos6502OpCode.from("ROX");
            fail("Invalid op-code name cannot be converted to OpCode");
        }catch(UnknownOpCodeException e){
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }


    @Test
    public void testStreamOf(){
        Mos6502OpCode.streamOf(Mos6502AddressingMode.IMPLIED).forEach( (opcode)->assertEquals(opcode, Mos6502OpCode.from(opcode.getOpCodeName())) );
        Mos6502OpCode.streamOf(Mos6502AddressingMode.ZERO_PAGE).forEach( (opcode)->assertEquals(opcode, Mos6502OpCode.from(opcode.getOpCodeName(), opcode.getAddressingMode())) );
    }
}
