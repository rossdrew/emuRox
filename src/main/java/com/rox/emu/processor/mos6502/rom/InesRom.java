package com.rox.emu.processor.mos6502.rom;

import com.rox.emu.UnknownRomException;

import java.util.Arrays;

/**
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
 * @author Ross Drew
 */
public final class InesRom {
    private final String description;

    private InesRom(String description){
        this.description = description;
    }

    public static InesRom from(int[] bytes) {
        String headerDescription = processHeader(bytes);

        return new InesRom(headerDescription);
    }

    private static String processHeader(int[] bytes){
        if (bytes.length < 4 ||
            (bytes[0] != 0x4E) ||
            (bytes[1] != 0x45) ||
            (bytes[2] != 0x53) ||
            (bytes[3] != 0x1A)){

            throw new UnknownRomException("Invalid iNES header");
        }

        return "NES ROM";
    }

    public String getDescription() {
        return description;
    }
}
