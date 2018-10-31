package com.rox.emu.processor.mos6502;

import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.rox.emu.processor.mos6502.Registers.*;
import static com.rox.emu.processor.mos6502.Registers.Flag.*;
import static com.rox.emu.processor.mos6502.Registers.Register.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
        assertEquals(RoxByte.ZERO, registers.getRegister(ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(X_INDEX));
        assertEquals(RoxByte.ZERO, registers.getRegister(Y_INDEX));
        assertEquals(RoxByte.ZERO, registers.getRegister(PROGRAM_COUNTER_HI));
        assertEquals(RoxByte.ZERO, registers.getRegister(PROGRAM_COUNTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(STACK_POINTER_HI));
        assertEquals(RoxByte.fromLiteral(0b11111111), registers.getRegister(STACK_POINTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(STATUS_FLAGS));
    }

    @Test
    public void testDescription(){
        for (Flag f : Flag.values()){
            assertEquals(f.name(), f.getDescription().toUpperCase().replaceAll(" ", "_"));
        }
    }

    @Test
    public void testPlaceValues(){
        assertEquals(0b00000001, CARRY.getPlaceValue());
        assertEquals(0b00000010, ZERO.getPlaceValue());
        assertEquals(0b00000100, IRQ_DISABLE.getPlaceValue());
        assertEquals(0b00001000, DECIMAL_MODE.getPlaceValue());
        assertEquals(0b00010000, BREAK.getPlaceValue());
        assertEquals(0b00100000, UNUSED.getPlaceValue());
        assertEquals(0b01000000, OVERFLOW.getPlaceValue());
        assertEquals(0b10000000, NEGATIVE.getPlaceValue());
    }

    @Test
    public void testSetAndGetRegister(){
        registers.setRegister(ACCUMULATOR, RoxByte.fromLiteral(10));
        registers.setRegister(PROGRAM_COUNTER_HI, RoxByte.fromLiteral(1));
        registers.setRegister(PROGRAM_COUNTER_LOW, RoxByte.fromLiteral(1));
        registers.setRegister(STACK_POINTER_LOW, RoxByte.fromLiteral(3));
        registers.setRegister(STATUS_FLAGS, RoxByte.fromLiteral(0b01000011));


        assertEquals(RoxByte.fromLiteral(10), registers.getRegister(ACCUMULATOR));
        assertEquals(RoxByte.fromLiteral(1), registers.getRegister(PROGRAM_COUNTER_HI));
        assertEquals(RoxByte.fromLiteral(1), registers.getRegister(PROGRAM_COUNTER_LOW));
        assertEquals(RoxByte.fromLiteral(3), registers.getRegister(STACK_POINTER_LOW));
        assertEquals(RoxByte.fromLiteral(0b01000011), registers.getRegister(STATUS_FLAGS));
    }

    @Test
    public void testSettingNUll(){
        registers.setRegister(ACCUMULATOR, null);
        registers.setRegister(PROGRAM_COUNTER_HI, null);
        registers.setRegister(PROGRAM_COUNTER_LOW, null);
        registers.setRegister(STACK_POINTER_LOW, null);
        registers.setRegister(STATUS_FLAGS, null);


        assertEquals(RoxByte.ZERO, registers.getRegister(ACCUMULATOR));
        assertEquals(RoxByte.ZERO, registers.getRegister(PROGRAM_COUNTER_HI));
        assertEquals(RoxByte.ZERO, registers.getRegister(PROGRAM_COUNTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(STACK_POINTER_LOW));
        assertEquals(RoxByte.ZERO, registers.getRegister(STATUS_FLAGS));
    }

    @Test
    public void testGetImplicitlyCreatedPC(){
        registers.setRegister(PROGRAM_COUNTER_HI, RoxByte.fromLiteral(1));
        registers.setRegister(PROGRAM_COUNTER_LOW, RoxByte.fromLiteral(1));

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
        registers.setRegister(STATUS_FLAGS, RoxByte.ZERO);
        registers.setFlag(Flag.NEGATIVE);

        assertTrue("Expected N flag to be set", registers.getRegister(STATUS_FLAGS).isBitSet(Flag.NEGATIVE.getIndex()));
    }

    @Test
    public void testClearFlag(){
        registers.setRegister(STATUS_FLAGS, RoxByte.ZERO);
        registers.setFlag(Flag.NEGATIVE);
        registers.clearFlag(Flag.NEGATIVE);


        assertEquals("Expected flags [" + Integer.toBinaryString(0) +
                     "] were [" + Integer.toBinaryString(registers.getRegister(STATUS_FLAGS).getRawValue()) + "]" ,
                     RoxByte.ZERO, registers.getRegister(STATUS_FLAGS));
    }

    @Test
    public void testRegisterDetails(){
        for (Register register : Register.values()) {
            assertFalse(register.getDescription().isEmpty());
            assertTrue(register.getIndex() >= 0 && register.getIndex() <= 8);
        }
    }

    @Test
    public void testGetValidFlagName(){
        for (Flag flag : Flag.values()) {
            assertTrue(flag.getDescription() != null && !flag.getDescription().isEmpty());
        }
    }

    @Test
    public void testCopy(){
        registers.setRegister(ACCUMULATOR, RoxByte.fromLiteral(23));
        registers.setRegister(PROGRAM_COUNTER_HI, RoxByte.fromLiteral(24));
        registers.setRegister(PROGRAM_COUNTER_LOW, RoxByte.fromLiteral(25));
        registers.setRegister(STACK_POINTER_LOW, RoxByte.fromLiteral(26));

        registers.setFlag(CARRY);
        registers.setFlag(ZERO);
        registers.setFlag(IRQ_DISABLE);
        registers.setFlag(DECIMAL_MODE);
        registers.setFlag(BREAK);
        registers.setFlag(UNUSED);
        registers.setFlag(OVERFLOW);
        registers.setFlag(NEGATIVE);

        final Registers copiedRegisters = registers.copy();

        assertNotEquals(copiedRegisters, registers);
        assertEquals(registers.getRegister(ACCUMULATOR), RoxByte.fromLiteral(23));
        assertEquals(registers.getRegister(PROGRAM_COUNTER_HI), RoxByte.fromLiteral(24));
        assertEquals(registers.getRegister(PROGRAM_COUNTER_LOW), RoxByte.fromLiteral(25));
        assertEquals(registers.getRegister(STACK_POINTER_LOW), RoxByte.fromLiteral(26));

        assertTrue(registers.getFlag(CARRY));
        assertTrue(registers.getFlag(ZERO));
        assertTrue(registers.getFlag(IRQ_DISABLE));
        assertTrue(registers.getFlag(DECIMAL_MODE));
        assertTrue(registers.getFlag(BREAK));
        assertTrue(registers.getFlag(UNUSED));
        assertTrue(registers.getFlag(OVERFLOW));
        assertTrue(registers.getFlag(NEGATIVE));
    }

    @Test
    public void testDescriptions(){
        assertEquals("Carry", Flag.CARRY.getDescription());
        assertEquals("Zero", Flag.ZERO.getDescription());
        assertEquals("Irq Disable", Flag.IRQ_DISABLE.getDescription());
        assertEquals("Decimal Mode", Flag.DECIMAL_MODE.getDescription());
        assertEquals("Break", Flag.BREAK.getDescription());
        assertEquals("Unused", Flag.UNUSED.getDescription());
        assertEquals("Overflow", Flag.OVERFLOW.getDescription());
        assertEquals("Negative", Flag.NEGATIVE.getDescription());
    }

    @Test
    public void testPlaceValue(){
        assertEquals(1, Flag.CARRY.getPlaceValue());
        assertEquals(2, Flag.ZERO.getPlaceValue());
        assertEquals(4, Flag.IRQ_DISABLE.getPlaceValue());
        assertEquals(8, Flag.DECIMAL_MODE.getPlaceValue());
        assertEquals(16, Flag.BREAK.getPlaceValue());
        assertEquals(32, Flag.UNUSED.getPlaceValue());
        assertEquals(64, Flag.OVERFLOW.getPlaceValue());
        assertEquals(128, Flag.NEGATIVE.getPlaceValue());
    }

    /*
            registers.setRegister(Registers.Register.ACCUMULATOR, null);
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_HI, null);
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_LOW, null);
        registers.setRegister(Registers.Register.STACK_POINTER_LOW, null);
        registers.setRegister(Registers.Register.STATUS_FLAGS, null);

        assertEquals(0b00000001, CARRY.getPlaceValue());
        assertEquals(0b00000010, ZERO.getPlaceValue());
        assertEquals(0b00000100, IRQ_DISABLE.getPlaceValue());
        assertEquals(0b00001000, DECIMAL_MODE.getPlaceValue());
        assertEquals(0b00010000, BREAK.getPlaceValue());
        assertEquals(0b00100000, UNUSED.getPlaceValue());
        assertEquals(0b01000000, OVERFLOW.getPlaceValue());
        assertEquals(0b10000000, NEGATIVE.getPlaceValue());
     */
}
