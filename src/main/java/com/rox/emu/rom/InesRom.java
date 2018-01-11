package com.rox.emu.rom;

/**
 * A representation of an iNES ROM file.
 *
 * @author Ross Drew
 */
public final class InesRom {
    private final InesRomHeader header;

    private InesRom(final InesRomHeader header){
        this.header = header;
    }

    public static InesRom from(final byte[] bytes) {
        final InesRomHeader newHeader = processHeader(bytes);

        return new InesRom(newHeader);
    }

    private static InesRomHeader processHeader(final byte[] bytes){
        if (bytes.length < InesRomHeader.HEADER_SIZE)
            throw new UnknownRomException("Invalid iNES header: Too short.");

        if (bytes[0] != 'N'
         || bytes[1] != 'E'
         || bytes[2] != 'S'
         || bytes[3] != 0x1A ){
            throw new UnknownRomException("Invalid iNES header: iNES prefix missing.");
        }

        int prgRomBlocks = bytes[4];
        int chrRomBlocks = bytes[5];
        RomControlOptions romControlOptions = new RomControlOptions(bytes[6], bytes[7]);

        return new InesRomHeader("NES ROM", prgRomBlocks, chrRomBlocks, romControlOptions);
    }

    public String getDescription() {
        return header.getDescription();
    }

    public InesRomHeader getHeader(){
        return header;
    }
}
