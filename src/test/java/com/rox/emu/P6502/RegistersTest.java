package com.rox.emu.P6502;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author rossdrew
 */
public class RegistersTest {
    private Registers registers;

    @Before
    public void setup(){
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

    //TODO set PC
    //TODO set/clear Flag/flags
    //TODO getNexTProgramCounter
}
