package com.rox.emu.rom;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InesRomTest {
    @Test
    public void testFromWithBytes(){
        final InesRom rom = InesRom.from(asPaddedHeader(new byte[] {0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0}));

        assertNotNull(rom);
        assertFalse(rom.getDescription().isEmpty());
    }

    @Test
    public void testGetHeader(){
        final int prgRomBlocks = 0x9;
        final int chrRomBlocks = 0x4; // ChrRom/VRom
        final byte controlOptionsByte = (byte)0b10101010;

        final InesRom rom = InesRom.from(asPaddedHeader(new byte[] {0x4E, 0x45, 0x53, 0x1A, prgRomBlocks, chrRomBlocks, controlOptionsByte}));
        final InesRomHeader header = rom.getHeader();

        assertNotNull(header);
        assertFalse(header.getDescription().isEmpty());
        assertEquals(prgRomBlocks, header.getPrgBlocks());
        assertEquals(chrRomBlocks, header.getChrBlocks());
        assertEquals("NES ROM", header.getDescription());

        assertEquals(chrRomBlocks, rom.getHeader().getChrBlocks());
        assertEquals(prgRomBlocks, rom.getHeader().getPrgBlocks());

        assertEquals(RomControlOptions.Mirroring.FOUR_SCREEN, rom.getHeader().getRomControlOptions().getMirroring());
        assertTrue(rom.getHeader().getRomControlOptions().isRamPresent());
        assertFalse(rom.getHeader().getRomControlOptions().isTrainerPresent());
    }

    @Test
    public void testInvalidFromWithBytes(){
        try {
            InesRom.from(asPaddedHeader(new byte[]{0x0, 0x0, 0x0, 0x1A, 0x0, 0x0}));
            fail("ROM contains an invalid NES header, should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testHeaderTooShort(){
        try {

            InesRom.from(new byte[]{'N', 'E', 'S'});
            fail("ROM contains a NES header that is too short to be valid, should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testEmptyFrom(){
        try {
            InesRom.from(new byte[]{});
            fail("ROM contains a missing NES header, should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testInvalidFirstHeaderPrefixValue(){
        try {
            InesRom.from(asPaddedHeader(new byte[]{'Z', 'E', 'S', 0x1A, 0x0, 0x0}));
            fail("ROM contains an invalid NES header (first byte should be 'N'), should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testInvalidSecondHeaderPrefixValue(){
        try {
            InesRom.from(asPaddedHeader(new byte[]{'N', 'Z', 'S', 0x1A, 0x0, 0x0}));
            fail("ROM contains an invalid NES header (second byte should be 0x45), should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testInvalidThirdHeaderPrefixValue(){
        try {
            InesRom.from(asPaddedHeader(new byte[]{'N', 'E', 'Z', 0x1A, 0x0, 0x0}));
            fail("ROM contains an invalid NES header (third byte should be 0x53), should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testInvalidFourthHeaderPrefixValue(){
        try {
            InesRom.from(asPaddedHeader(new byte[]{'N', 'E', 'S', 0x1B, 0x0, 0x0}));
            fail("ROM contains an invalid NES header (fourth byte should be 0x1A), should throw an exception");
        }catch(UnknownRomException e){
            assertNotNull(e.getMessage());
        }
    }

    private byte[] asPaddedHeader(byte[] values){
        byte[] header = new byte[InesRomHeader.HEADER_SIZE];
        System.arraycopy(values, 0, header, 0, values.length);
        return header;
    }
}
