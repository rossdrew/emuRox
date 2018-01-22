package com.rox.emu.nes;

import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.ricoh2c02.Ricoh2C02;

/**
 * A representation of the generic functions of the Nintendo Entertainment System.
 *
 * Perhaps to be later broken down into the Famicom, NES-EU and NES-US if there are signification functional differences.
 */
public class NES {
    private final Mos6502 cpu;
    private final Ricoh2C02 ppu;
    private final Memory mainMemory;

    public NES(final Mos6502 cpu, final Ricoh2C02 ppu, final Memory mainMemory){
        this.cpu = cpu;
        this.ppu = ppu;
        this.mainMemory = mainMemory;
    }

    public void reset(){
        //Program ROM space set to
        //0xFFFC->0x80
        //0xFFFD->0x00
        // 0x4000 of ROM should be memory mapped to both 0x8000 and 0xC000
        mainMemory.setBlock(0xFFFC, new int[] {0x80, 0x00});
    }
}
