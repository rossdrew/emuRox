package com.rox.emu.processor.mos6502;

import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.X_INDEX));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.Y_INDEX));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.STACK_POINTER_HI));
        assertEquals(RoxByte.fromLiteral(0b11111111), registers.getRegister(Registers.Register.STACK_POINTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testSetAndGetRegister(){
        registers.setRegister(Registers.Register.ACCUMULATOR, RoxByte.fromLiteral(10));
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_HI, RoxByte.fromLiteral(1));
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_LOW, RoxByte.fromLiteral(1));
        registers.setRegister(Registers.Register.STACK_POINTER_LOW, RoxByte.fromLiteral(3));
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.fromLiteral(0b01000011));


        assertEquals(RoxByte.fromLiteral(10), registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.fromLiteral(1), registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));
        assertEquals(RoxByte.fromLiteral(1), registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));
        assertEquals(RoxByte.fromLiteral(3), registers.getRegister(Registers.Register.STACK_POINTER_LOW));
        assertEquals(RoxByte.fromLiteral(0b01000011), registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testSettingNUll(){
        registers.setRegister(Registers.Register.ACCUMULATOR, null);
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_HI, null);
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_LOW, null);
        registers.setRegister(Registers.Register.STACK_POINTER_LOW, null);
        registers.setRegister(Registers.Register.STATUS_FLAGS, null);


        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.STACK_POINTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(Registers.Register.STATUS_FLAGS));
    }

    @Test
    public void testGetImplicitlyCreatedPC(){
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_HI, RoxByte.fromLiteral(1));
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_LOW, RoxByte.fromLiteral(1));

        assertEquals("Program counter should be combined REG_PC_HIGH and REG_PC_LOW", RoxWord.fromLiteral(0b100000001), registers.getPC());
    }

    @Test
    public void testGetExplicitlyCreatedPC(){
        registers.setPC(RoxWord.fromLiteral(0b111111110));

        assertEquals("Program counter should be combined REG_PC_HIGH and REG_PC_LOW", RoxWord.fromLiteral(0b111111110), registers.getPC());
    }

    @Test
    public void testSetAndGetNextProgramCounter(){
        registers.setPC(RoxWord.ZERO);
        for (int i=1; i<300; i++) {
            RoxWord result = registers.getNextProgramCounter();
            assertEquals("Expected: " + Integer.toBinaryString(i) + ", got " + Integer.toBinaryString(result.getRawValue()), RoxWord.fromLiteral(i), result);
        }
    }

    @Test
    public void testSetFlag(){
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.ZERO);
        registers.setFlag(Registers.Flag.NEGATIVE);

        assertTrue("Expected N flag to be set", registers.getRegister(Registers.Register.STATUS_FLAGS).isBitSet(Registers.Flag.NEGATIVE.getIndex()));
    }

    @Test
    public void testClearFlag(){
        registers.setRegister(Registers.Register.STATUS_FLAGS, RoxByte.ZERO);
        registers.setFlag(Registers.Flag.NEGATIVE);
        registers.clearFlag(Registers.Flag.NEGATIVE);


        assertEquals("Expected flags [" + Integer.toBinaryString(0) +
                     "] were [" + Integer.toBinaryString(registers.getRegister(Registers.Register.STATUS_FLAGS).getRawValue()) + "]" ,
                     RoxByte.ZERO, registers.getRegister(Registers.Register.STATUS_FLAGS));
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
