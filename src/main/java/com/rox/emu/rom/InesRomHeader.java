package com.rox.emu.rom;

/**
 * A header for an iNES file
 *
 * As per https://wiki.nesdev.com/w/index.php/NES_2.0...
 *
 * 0-3  : string    "NES"<EOF>
 * 4    : byte      PRG ROM (Number of 16384 byte program ROM pages)
 * 5    : byte      CHR ROM (Number of 8192 byte character ROM pages (0 indicates CHR RAM))
 * 6    : bitfield  Flags 6
 *                   NNNN FTBM
 *                   ---------
 *                   N: Lower 4 bits of the mapper number
 *                   F: Four screen mode. 0 = no, 1 = yes. (When set, the M bit has no effect)
 *                   T: Trainer.  0 = no trainer present, 1 = 512 byte trainer at 7000-71FFh
 *                   B: SRAM at 6000-7FFFh battery backed.  0= no, 1 = yes
 *                   M: Mirroring.  0 = horizontal, 1 = vertical.
 * 7    : bitfield  Flags 7
 *                   NNNN xxPV
 *                   ---------
 *                   N: Upper 4 bits of the mapper number
 *                   P: Playchoice 10.  When set, this is a PC-10 game
 *                   V: Vs. Unisystem.  When set, this is a Vs. game
 *                   x: these bits are not used in iNES.
 * 8-15 : byte      These bytes are not used, and should be 00h.
 */
public final class InesRomHeader{
    public static final int HEADER_SIZE = 6; //Working value

    private final String description;

    private final int prgBlocks;
    private final int chrBlocks;

    public InesRomHeader(final String description,
                         final int prgBlocks,
                         final int chrBlocks){
        this.description = description;
        this.prgBlocks = prgBlocks;
        this.chrBlocks = chrBlocks;
    }

    public String getDescription() {
        return description;
    }

    public int getPrgBlocks() {
        return prgBlocks;
    }

    public int getChrBlocks() {
        return chrBlocks;
    }
}