package com.rox.emu.rom;

import com.rox.emu.mem.ReadOnlyMemory;

import java.util.Arrays;

/**
 * A representation of an iNES ROM file.
 *
 * @author Ross Drew
 */
public final class InesRom {
    /** The predefined PRG ROM block size */
    public static final int PRG_ROM_BLOCK_SIZE = 16384;
    /** The predefined CHR ROM block size */
    public static final int CHR_ROM_BLOCK_SIZE = 8192;
    /** The size of of the trainer in bytes if present */
    public static final int TRAINER_SIZE = 512;

    private final InesRomHeader header;
    private final byte[] trainerRom; //XXX Move to ReadOnlyMemory

    private final ReadOnlyMemory prgrom;
    private final ReadOnlyMemory chrrom;

    private InesRom(final InesRomHeader header,
                    byte[] trainerRom,
                    final byte[] prgRom,
                    final byte[] chrRom){
        this.header = header;
        this.trainerRom = trainerRom;

        this.prgrom = new ReadOnlyMemory(prgRom);
        this.chrrom = new ReadOnlyMemory(chrRom);
    }

    /**
     * Generate an {@link InesRom} from the provided bytes
     */
    public static InesRom from(final byte[] bytes) {
        final InesRomHeader newHeader = processHeader(bytes);
        int offset = InesRomHeader.HEADER_SIZE;

        byte[] trainer = {};
        if (newHeader.getRomControlOptions().isTrainerPresent()){
            trainer = extractBinaryData(bytes, TRAINER_SIZE, offset);
            offset += TRAINER_SIZE;
        }

        final byte[] program = extractBinaryData(bytes, newHeader.getPrgBlocks() * PRG_ROM_BLOCK_SIZE, offset);
        offset += program.length;

        byte[] character = {};
        if (newHeader.getChrBlocks() > 0) {
            character = extractBinaryData(bytes, newHeader.getChrBlocks() * CHR_ROM_BLOCK_SIZE, offset);
        } //TODO else CHR RAM
        offset += (newHeader.getChrBlocks() * CHR_ROM_BLOCK_SIZE);

        return new InesRom(newHeader, trainer, program, character);
    }

    private static byte[] extractBinaryData(final byte[] bytes, final int byteCount, final int offset) {
        return Arrays.copyOfRange(bytes, offset,offset+byteCount);
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

    public ReadOnlyMemory getProgramRom(){
        return this.prgrom;
    }

    public ReadOnlyMemory getCharacterRom(){
        return this.chrrom;
    }

    public byte[] getTrainerRom() {
        return this.trainerRom;
    }
}
