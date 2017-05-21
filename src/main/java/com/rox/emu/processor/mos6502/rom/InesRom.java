package com.rox.emu.processor.mos6502.rom;

import com.rox.emu.UnknownRomException;

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

    public static InesRom from(final int[] bytes) {
        final InesRomHeader newHeader = processHeader(bytes);

        return new InesRom(newHeader);
    }

    private static InesRomHeader processHeader(final int[] bytes){
        if (bytes.length < InesRomHeader.HEADER_SIZE)
            throw new UnknownRomException("Invalid iNES header: Too short.");

        final String prefix = "" + (char)bytes[0] + (char)bytes[1] + (char)bytes[2];

        if (!prefix.equals("NES") || (bytes[3] != 0x1A)){

            throw new UnknownRomException("Invalid iNES header: iNES prefix missing.");
        }

        int prgBlocks = bytes[4];
        int chrBlocks = bytes[5];

        return new InesRomHeader("NES ROM", prgBlocks, chrBlocks);
    }

    public String getDescription() {
        return header.getDescription();
    }

    public InesRomHeader getHeader(){
        return header;
    }
}
