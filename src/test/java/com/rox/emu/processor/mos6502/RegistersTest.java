package com.rox.emu.processor.mos6502;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static org.spockframework.util.Assert.fail;

/**
 * @author rossdrew
 */
@RunWith(JUnitQuickcheck.class)
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
        registers.setFlag(Registers.N);

        assertEquals("Expected flags " + Integer.toBinaryString(Registers.STATUS_FLAG_NEGATIVE) +
                     " was " + Integer.toBinaryString(registers.getRegister(Registers.REG_STATUS)),
                     Registers.STATUS_FLAG_NEGATIVE, registers.getRegister(Registers.REG_STATUS));
    }

    @Test
    public void testClearFlag(){
        registers.setRegister(Registers.REG_STATUS, 0b00000000);
        registers.setFlag(Registers.N);
        registers.clearFlag(Registers.N);


        assertEquals("Expected flags [" + Integer.toBinaryString(0) +
                     "] were [" + Integer.toBinaryString(registers.getRegister(Registers.REG_STATUS)) + "]" ,
                     0, registers.getRegister(Registers.REG_STATUS));
    }
    
    @Test
    public void testFlagPlaceValueToFlagID(){
        for (int i=0; i<8; i++){
            int placevalue = 1 << i;
            assertEquals(i, Registers.getFlagID(placevalue));
        }
    }

    @Property(trials = 10)
    public void testInvalidFlagPlaceValueToFlagID(@When(satisfies = "#_ < 0 || #_ > 128") int placeValue){
        try{
            Registers.getFlagID(placeValue);
            fail("Place value " + Integer.toHexString(placeValue) + " should be invalid.");
        }catch(IllegalArgumentException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testGetValidRegisterName(){
        for (int i=0; i<8; i++){
            try {
                final String name = Registers.getRegisterName(i);

                assertNotNull(name);
                assertNotEquals("", name);
            }catch(ArrayIndexOutOfBoundsException e){
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
            }catch(ArrayIndexOutOfBoundsException e){
                assertNotNull(e);
                assertFalse(e.getMessage().isEmpty());
            }
        }
    }

    @Test
    public void testGetValidFlagName(){
        assertTrue("Expected flag 1 to contain the word Carry, was " + Registers.getFlagName(0), Registers.getFlagName(0).contains("Carry"));
        assertTrue("Expected flag 1 to contain the word Zero, was " + Registers.getFlagName(1), Registers.getFlagName(1).contains("Zero"));
        assertTrue("Expected flag 1 to contain the word IRQ, was " + Registers.getFlagName(2), Registers.getFlagName(2).contains("IRQ"));
        assertTrue("Expected flag 1 to contain the word Decimal/BCD, was " + Registers.getFlagName(3), Registers.getFlagName(3).contains("Decimal") || Registers.getFlagName(8).contains("BCD"));
        assertTrue("Expected flag 1 to contain the word BRK, was " + Registers.getFlagName(4), Registers.getFlagName(4).contains("BRK"));
        assertTrue("Expected flag 1 to contain the word <, was " + Registers.getFlagName(5), Registers.getFlagName(5).contains("<"));
        assertTrue("Expected flag 1 to contain the word Overflow, was " + Registers.getFlagName(6), Registers.getFlagName(6).contains("Overflow"));
        assertTrue("Expected flag 1 to contain the word Negative, was " + Registers.getFlagName(7), Registers.getFlagName(7).contains("Negative"));
    }

    @Test
    public void testGetInvalidFlagName(){
        int[] invalidValues = new int[] {-3,-2,-1,8,9,10};

        for (int i = 0; i < invalidValues.length; i++) {
            try {
                Registers.getFlagName(invalidValues[i]);
                fail(i + " is an invalid flag ID");
            }catch(IllegalArgumentException e){
                assertNotNull(e);
                assertFalse(e.getMessage().isEmpty());
            }
        }
    }
}
