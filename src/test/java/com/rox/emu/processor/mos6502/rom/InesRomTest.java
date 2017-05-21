package com.rox.emu.processor.mos6502.rom;

import com.rox.emu.UnknownRomException;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class InesRomTest {
    @Test
    public void testFromWithBytes(){
        final InesRom rom = InesRom.from(new int[] {0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0});

        assertNotNull(rom);
    }

    @Test
    public void testGetHeader(){
        final int prgBlocks = 0x9;
        final int charBlocks = 0x4;

        final InesRom rom = InesRom.from(new int[] {0x4E, 0x45, 0x53, 0x1A, prgBlocks, charBlocks});
        final InesRomHeader header = rom.getHeader();

        assertNotNull(header);
        assertFalse(header.getDescription().isEmpty());
        assertEquals(prgBlocks, header.getPrgBlocks());
        assertEquals(charBlocks, header.getChrBlocks());
    }

    @Test
    public void testInvalidFromWithBytes(){
        try {
            InesRom.from(new int[]{0x0, 0x0, 0x0, 0x1A, 0x0, 0x0});
            fail("ROM contains an invalid NES header, should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testHeaderToShortForFrom(){
        try {
            InesRom.from(new int[]{0x4E, 0x45, 0x53});
            fail("ROM contains a NES header that is too short to be valid, should throw an exception");
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
