package com.rox.emu.processor.mos6502;

import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    public void testInitialState(){
        assertEquals(0, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(0, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(0, registers.getRegister(Registers.Register.Y_INDEX));
        assertEquals(0, registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));
        assertEquals(0, registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));
        assertEquals(0b11111111, registers.getRegister(Registers.Register.STACK_POINTER_LOW));
        assertEquals(0, registers.getRegister(Registers.Register.STACK_POINTER_HI));
        assertEquals(0, registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testSetAndGetRegister(){
        registers.setRegister(Registers.Register.ACCUMULATOR, 10);
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_HI, 1);
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_LOW, 1);
        registers.setRegister(Registers.Register.STACK_POINTER_HI, 3);
        registers.setRegister(Registers.Register.STATUS_FLAGS, 0b01000011);


        assertEquals(10, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(1, registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));
        assertEquals(1, registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));
        assertEquals(3, registers.getRegister(Registers.Register.STACK_POINTER_HI));
        assertEquals(0b01000011, registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testGetImplicitlyCreatedPC(){
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_HI, 1);
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_LOW, 1);

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
        registers.setRegister(Registers.Register.STATUS_FLAGS, 0b00000000);
        registers.setFlag(Registers.Flag.NEGATIVE);

        assertEquals("Expected flags " + Integer.toBinaryString(Registers.Flag.NEGATIVE.getPlaceValue()) +
                     " was " + Integer.toBinaryString(registers.getRegister(Registers.Register.STATUS_FLAGS)),
                     Registers.Flag.NEGATIVE.getPlaceValue(), registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testClearFlag(){
        registers.setRegister(Registers.Register.STATUS_FLAGS, 0b00000000);
        registers.setFlag(Registers.Flag.NEGATIVE);
        registers.clearFlag(Registers.Flag.NEGATIVE);


        assertEquals("Expected flags [" + Integer.toBinaryString(0) +
                     "] were [" + Integer.toBinaryString(registers.getRegister(Registers.Register.STATUS_FLAGS)) + "]" ,
                     0, registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testRegisterDetails(){
        for (Registers.Register register : Registers.Register.values()) {
            assertFalse(register.getDescription().isEmpty());
            assertTrue(register.getIndex() >= 0 && register.getIndex() <= 8);
        }
    }

    @Test
    public void testGetValidFlagName(){
        for (Registers.Flag flag : Registers.Flag.values()) {
            assertTrue(flag.getDescription() != null && !flag.getDescription().isEmpty());
        }
    }
}
