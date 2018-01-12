package com.rox.emu.rom;

/**
 * A representation of an iNES ROM file.
 *
 * @author Ross Drew
 */
public final class InesRom {
    public static final int PRG_ROM_BLOCK_SIZE = 16384;
    public static final int CHR_ROM_BLOCK_SIZE = 8192;

    private final InesRomHeader header;
    private final byte[] programRom;
    private final byte[] characterRom;

    private InesRom(final InesRomHeader header,
                    final byte[] prgRom,
                    final byte[] chrRom){
        this.header = header;
        this.programRom = prgRom;
        this.characterRom = chrRom;
    }

    public static InesRom from(final byte[] bytes) {
        final InesRomHeader newHeader = processHeader(bytes);

        //TODO trainer offset will be needed
        //16-...   ROM banks, in ascending order. If a trainer is present, its 512 bytes precede the ROM bank contents. (PRG ROM (Number of 16384 byte program ROM pages))
        final byte[] program = extractBinaryData(bytes, newHeader.getPrgBlocks() * PRG_ROM_BLOCK_SIZE, InesRomHeader.HEADER_SIZE);
        //TODO Need to deal with 0
        //...-EOF  VROM banks, in ascending order.  (CHR ROM (Number of 8192 byte character ROM pages (0 indicates CHR RAM)))
        final byte[] character = extractBinaryData(bytes, newHeader.getChrBlocks() * CHR_ROM_BLOCK_SIZE, InesRomHeader.HEADER_SIZE + program.length);

        return new InesRom(newHeader, program, character);
    }

    private static byte[] extractBinaryData(final byte[] bytes, final int byteCount, final int offset) {
        //XXX return Arrays.copyOfRange(bytes, offset, offset+byteCount);
        byte[] prg = new byte[byteCount];
        for (int byteIndex=0; byteIndex<byteCount; byteIndex++ ){
            int romByteIndex = offset + byteIndex;
            prg[byteIndex] = bytes[romByteIndex];
            System.out.println(prg[byteIndex]);
        }
        return prg;
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

    public byte[] getProgramRom(){
        return this.programRom;
    }

    public byte[] getCharacterRom(){
        return this.characterRom;
    }
}
