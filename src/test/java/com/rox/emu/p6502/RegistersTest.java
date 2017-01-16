package com.rox.emu.p6502;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.spockframework.util.Assert.fail;

/**
 * @author rossdrew
 */
public class RegistersTest {
    private Registers registers;

    @Before
    public void setUp(){
        registers = new Registers();
    }

    @Test
    public void testSetAndGetRegister(){
        registers.setRegister(Registers.REG_ACCUMULATOR, 10);
        registers.setRegister(Registers.REG_PC_HIGH, 1);
        registers.setRegister(Registers.REG_PC_LOW, 1);
        registers.setRegister(Registers.REG_SP, 3);
        registers.setRegister(Registers.REG_STATUS, 0b01000011);


        assertEquals(10, registers.getRegister(Registers.REG_ACCUMULATOR));
        assertEquals(1, registers.getRegister(Registers.REG_PC_HIGH));
        assertEquals(1, registers.getRegister(Registers.REG_PC_LOW));
        assertEquals(3, registers.getRegister(Registers.REG_SP));
        assertEquals(0b01000011, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testGetImplicitlyCreatedPC(){
        registers.setRegister(Registers.REG_PC_HIGH, 1);
        registers.setRegister(Registers.REG_PC_LOW, 1);

        assertEquals("Program counter should be combined REG_PC_HIGH and REG_PC_LOW", 0b100000001, registers.getPC());
    }

    @Test
    public void testGetExplicitlyCreatedPC(){
        registers.setPC(0b111111110);

        assertEquals("Program counter should be combined REG_PC_HIGH and REG_PC_LOW", 0b111111110, registers.getPC());
    }

    @Test
    public void testSetAndGetNextProgramCounter(){
        registers.setPC(0);
        for (int i=1; i<300; i++) {
            int result = registers.getNextProgramCounter();
            assertEquals("Expected: " + Integer.toBinaryString(i) + ", got " + Integer.toBinaryString(result), i, result);
        }
    }

    @Test
    public void testSetFlag(){
        registers.setRegister(Registers.REG_STATUS, 0b00000000);
        registers.setFlag(Registers.STATUS_FLAG_NEGATIVE);

        assertEquals("Expected flags " + Integer.toBinaryString(Registers.STATUS_FLAG_NEGATIVE) +
                     " was " + Integer.toBinaryString(registers.getRegister(Registers.REG_STATUS)),
                     Registers.STATUS_FLAG_NEGATIVE, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testClearFlag(){
        registers.setRegister(Registers.REG_STATUS, 0b00000000);
        registers.setFlag(Registers.STATUS_FLAG_NEGATIVE);
        registers.clearFlag(Registers.STATUS_FLAG_NEGATIVE);


        assertEquals("Expected flags [" + Integer.toBinaryString(0) +
                     "] were [" + Integer.toBinaryString(registers.getRegister(Registers.REG_STATUS)) + "]" ,
                     0, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testGetStatusFlags(){
        registers.setRegister(Registers.REG_STATUS, 0b11011001);

        assertTrue(registers.getStatusFlags()[0] &
                   registers.getStatusFlags()[3] &
                   registers.getStatusFlags()[4] &
                   registers.getStatusFlags()[6] &
                   registers.getStatusFlags()[7]);

        assertFalse(registers.getStatusFlags()[1] &
                    registers.getStatusFlags()[2] &
                    registers.getStatusFlags()[5]);
    }

    @Test
    public void testFlagPlaceValueToFlagID(){
        for (int i=0; i<8; i++){
            int placevalue = 1 << i;
            assertEquals(i, Registers.getFlagID(placevalue));
        }
    }

    @Test
    public void testGetValidRegisterName(){
        for (int i=0; i<8; i++){
            try {
                assertNotEquals("", Registers.getRegisterName(i));
            }catch(Exception e){
                TestCase.fail("Register #" + i + " should have a name");
            }
        }
    }

    @Test
    public void testGetInvalidRegisterName(){
        for (int i=9; i<11; i++){
            try {
                Registers.getRegisterName(i);
                fail(i + " is an invalid register ID");
            }catch(Exception e){

            }
        }
    }

    @Test
    public void testGetValidFlagName(){
        assertTrue(Registers.getFlagName(1).contains("Carry"));
        assertTrue(Registers.getFlagName(2).contains("Zero"));
        assertTrue(Registers.getFlagName(4).contains("IRQ"));
        assertTrue(Registers.getFlagName(8).contains("Decimal") || Registers.getFlagName(8).contains("BCD"));
        assertTrue(Registers.getFlagName(16).contains("BRK"));
        assertTrue(Registers.getFlagName(32).contains("<"));
        assertTrue(Registers.getFlagName(64).contains("Overflow"));
        assertTrue(Registers.getFlagName(128).contains("Negative"));
    }

    @Test
    public void testGetInvalidFlagName(){
        for (int i=9; i<11; i++){
            try {
                Registers.getFlagName(i);
                fail(i + " is an invalid flag ID");
            }catch(Exception e){

            }
        }
    }
}
