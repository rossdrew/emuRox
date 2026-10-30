package com.rox.emu.rom;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

public class InesRomTest {
    private static final int ROM_SIZE = InesRomHeader.HEADER_SIZE + InesRom.PRG_ROM_BLOCK_SIZE;

    static Stream<Arguments> invalidHeaders() {
        return Stream.of(
                Arguments.of("all-zero prefix",     new byte[]{0x0, 0x0, 0x0, 0x1A, 0x0, 0x0}),
                Arguments.of("first byte not 'N'",  new byte[]{'Z', 'E', 'S', 0x1A, 0x0, 0x0}),
                Arguments.of("second byte not 'E'", new byte[]{'N', 'Z', 'S', 0x1A, 0x0, 0x0}),
                Arguments.of("third byte not 'S'",  new byte[]{'N', 'E', 'Z', 0x1A, 0x0, 0x0}),
                Arguments.of("fourth byte not 0x1A", new byte[]{'N', 'E', 'S', 0x1B, 0x0, 0x0})
        );
    }

    @Test
    public void testFromWithBytes(){
        final InesRom rom = InesRom.from(asZeroPadded(new byte[] {0x4E, 0x45, 0x53, 0x1A, 0x0, 0x0}, ROM_SIZE));

        assertNotNull(rom);
        assertFalse(rom.getDescription().isEmpty());
    }

    @Test
    public void testGetHeader(){
        final int prgRomBlocks = 0x1;
        final int chrRomBlocks = 0x0; // ChrRom/VRom
        final byte controlOptionsByte = (byte)0b10101010;

        final InesRom rom = InesRom.from(asZeroPadded(new byte[] {0x4E, 0x45, 0x53, 0x1A, prgRomBlocks, chrRomBlocks, controlOptionsByte}, ROM_SIZE));
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidHeaders")
    void invalidHeaderIsRejected(String description, byte[] prefix) {
        final byte[] rom = asZeroPadded(prefix, ROM_SIZE);
        assertThrows(UnknownRomException.class, () -> InesRom.from(rom));
    }

    @Test
    public void testHeaderTooShort2(){
        final byte[] headerThatIsTooShort = new byte[]{'N', 'E', 'S'};
        final UnknownRomException e = assertThrows(UnknownRomException.class, () -> InesRom.from(headerThatIsTooShort));
        assertNotNull(e.getMessage());
    }

    @Test
    public void testEmptyHeader(){
        final byte[] emptyHeader = new byte[]{};
        final UnknownRomException e = assertThrows(UnknownRomException.class, () -> InesRom.from(emptyHeader));
        assertNotNull(e.getMessage());
    }

    @Test
    public void testHeaderToString(){
        InesRomHeader header = new InesRomHeader("myHeader",42,43, new RomControlOptions(0b00000000, 0b00000000));
        assertEquals("InesRomHeader{description='myHeader', prgBlocks=42, chrBlocks=43, romControlOptions=RomControlOptions{mirroring=HORIZONTAL, ramPresent=false, trainerPresent=false, mapperNumber=0, vsUnisystem=false, playChoice10=false, version=1}}", header.toString());
    }

    @Test
    public void testControlOptionsToString(){
        RomControlOptions ctrlOptions = new RomControlOptions(0b00000000, 0b00000000);
        assertEquals("RomControlOptions{mirroring=HORIZONTAL, ramPresent=false, trainerPresent=false, mapperNumber=0, vsUnisystem=false, playChoice10=false, version=1}", ctrlOptions.toString());
    }

    private byte[] asZeroPadded(final byte[] values, final int size){
        byte[] header = new byte[size];
        System.arraycopy(values, 0, header, 0, values.length);
        return header;
    }
}
