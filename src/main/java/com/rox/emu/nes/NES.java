package com.rox.emu.nes;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.ricoh2c02.Ricoh2C02;

/**
 * A representation of the generic functions of the Nintendo Entertainment System.
 *
 * Perhaps to be later broken down into the Famicom, NES-EU and NES-US if there are signification functional differences.
 *
 * XXX How do I write this in a unit testable way
 *     - Inject a mock memory, cpu and ppu and make sure their startup and, reset states do as they should and that any memory modifications map onto memory/cpu/ppu as they should
 */
public class NES {
    private final Mos6502 cpu;
    private final Ricoh2C02 ppu;
    private final Memory mainMemory;

    /**
     original front-loading design, RP2A03G CPU chip, NES-CPU-07 main board revision, manufactured in 1988

     CPU Power up state
     --------------
     P = $34[1] (IRQ disabled)[2]
     A, X, Y = 0
     S = $FD
     $4017 = $00 (frame irq enabled)
     $4015 = $00 (all channels disabled)
     $4000-$400F = $00 (not sure about $4010-$4013)
     All 15 bits of noise channel LFSR = $0000[3]. The first time the LFSR is clocked from the all-0s state, it will
     shift in a 1.

     Internal memory ($0000-$07FF) has unreliable startup state. Some machines may have consistent RAM contents at
     power-on, but others do not.

     Emulators often implement a consistent RAM startup state (e.g. all $00 or $FF, or a particular pattern), and flash
     carts like the PowerPak may partially or fully initialize RAM before starting a program, so an NES programmer must
     be careful not to rely on the startup contents of RAM.

     CPU After Reset
     -----------
     A, X, Y were not affected
     S was decremented by 3 (but nothing was written to the stack)
     The I (IRQ disable) flag was set to true (status ORed with $04)
     The internal memory was unchanged
     APU mode in $4017 was unchanged
     APU was silenced ($4015 = 0)
     */

    public NES(final Mos6502 cpu, final Ricoh2C02 ppu, final Memory mainMemory){
        this.cpu = cpu;
        this.ppu = ppu;
        this.mainMemory = mainMemory;
    }

    public void reset(){
        mainMemory.setBlock(RoxWord.fromLiteral(0xFFFC), RoxByte.fromIntArray(new int[] {0x80, 0x00}));
    }
}
