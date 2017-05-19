package com.rox.emu.processor.mos6502.rom;

import com.rox.emu.UnknownRomException;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;

public class InesRomTest {
    @Test
    public void testFromWithBytes(){
        final InesRom rom = InesRom.from(new int[] {0x4E, 0x45, 0x53, 0x1A});

        assertNotNull(rom);
        assertNotNull(rom.getDescription());
        assertFalse(rom.getDescription().isEmpty());
    }

    @Test
    public void testInvalidFromWithBytes(){
        try {
            InesRom.from(new int[]{0x0, 0x0, 0x0, 0x1A});
            fail("ROM contains an invalid NES header, should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testEmptyFrom(){
        try {
            InesRom.from(new int[]{});
            fail("ROM contains a missing NES header, should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }
}
