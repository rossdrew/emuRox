package com.rox.emu.processor.mos6502.rom;

import com.rox.emu.UnknownRomException;

/**
 * A representation of an iNES ROM file.
 *
 * @author Ross Drew
 */
public final class InesRom {
    private static int HEADER_SIZE = 6; //Working value

    private final InesRomHeader header;

    private InesRom(final InesRomHeader header){
        this.header = header;
    }

    public static InesRom from(final int[] bytes) {
        final InesRomHeader newHeader = processHeader(bytes);

        return new InesRom(newHeader);
    }

    private static InesRomHeader processHeader(final int[] bytes){
        int byteIndex = 0;

        if (bytes.length < HEADER_SIZE)
            throw new UnknownRomException("Invalid iNES header: Too short.");

        if ((bytes[byteIndex++] != 0x4E) ||
            (bytes[byteIndex++] != 0x45) ||
            (bytes[byteIndex++] != 0x53) ||
            (bytes[byteIndex++] != 0x1A)){

            throw new UnknownRomException("Invalid iNES header: iNES prefix missing.");
        }

        int prgBlocks = bytes[byteIndex++];
        int chrBlocks = bytes[byteIndex++];

        return new InesRomHeader("NES ROM", prgBlocks, chrBlocks);
    }

    public String getDescription() {
        return header.getDescription();
    }

    public InesRomHeader getHeader(){
        return header;
    }
}
