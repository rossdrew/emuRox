package com.rox.emu.rom;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class InesRomTest {
    @Test
    public void testFromWithBytes(){
        final InesRom rom = InesRom.from(asPaddedHeader(new int[] {0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0}));

        assertNotNull(rom);
        assertFalse(rom.getDescription().isEmpty());
    }

    @Test
    public void testGetHeader(){
        final int prgBlocks = 0x9;
        final int charBlocks = 0x4;

        final InesRom rom = InesRom.from(asPaddedHeader(new int[] {0x4E, 0x45, 0x53, 0x1A, prgBlocks, charBlocks}));
        final InesRomHeader header = rom.getHeader();

        assertNotNull(header);
        assertFalse(header.getDescription().isEmpty());
        assertEquals(prgBlocks, header.getPrgBlocks());
        assertEquals(charBlocks, header.getChrBlocks());
        assertEquals("NES ROM", header.getDescription());
    }

    @Test
    public void testInvalidFromWithBytes(){
        try {
            InesRom.from(asPaddedHeader(new int[]{0x0, 0x0, 0x0, 0x1A, 0x0, 0x0}));
            fail("ROM contains an invalid NES header, should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testHeaderTooShort(){
        try {

            InesRom.from(new int[]{'N', 'E', 'S'});
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

    @Test
    public void testInvalidFirstHeaderPrefixValue(){
        try {
            InesRom.from(asPaddedHeader(new int[]{'Z', 'E', 'S', 0x1A, 0x0, 0x0}));
            fail("ROM contains an invalid NES header (first byte should be 'N'), should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testInvalidSecondHeaderPrefixValue(){
        try {
            InesRom.from(asPaddedHeader(new int[]{'N', 'Z', 'S', 0x1A, 0x0, 0x0}));
            fail("ROM contains an invalid NES header (second byte should be 0x45), should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testInvalidThirdHeaderPrefixValue(){
        try {
            InesRom.from(asPaddedHeader(new int[]{'N', 'E', 'Z', 0x1A, 0x0, 0x0}));
            fail("ROM contains an invalid NES header (third byte should be 0x53), should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testInvalidFourthHeaderPrefixValue(){
        try {
            InesRom.from(asPaddedHeader(new int[]{'N', 'E', 'S', 0x1B, 0x0, 0x0}));
            fail("ROM contains an invalid NES header (fourth byte should be 0x1A), should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    private int[] asPaddedHeader(int[] values){
        int[] header = new int[InesRomHeader.HEADER_SIZE];
        System.arraycopy(values, 0, header, 0, values.length);
        return header;
    }
}
