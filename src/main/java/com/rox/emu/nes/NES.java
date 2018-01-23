package com.rox.emu.nes;

import com.rox.emu.mem.Memory;
import com.rox.emu.mem.MultiSourceMemory;
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

//    private Memory setupCpuMemoryMappings(){
//        //$0800-$0FFF	$0800	Mirrors of $0000-$07FF
//        final int RAM_TOP = 0x07FF;
//        final int MIRRORED_RAM_TOP = 0x0800;
//
//        int[] logicalRamMirror = new int[RAM_TOP - MIRRORED_RAM_TOP];
//        int[] physicalRam = new int[RAM_TOP];
//        for (int ramIndex = 0; ramIndex < RAM_TOP; ramIndex++) {
//            logicalRamMirror[ramIndex] = RAM_TOP + ramIndex;
//            physicalRam[ramIndex] = ramIndex;
//        }
//
//        //$2008-$3FFF	$1FF8	Mirrors of $2000-2007 (repeats every 8 bytes)
//        final int PPU_REG_BOT = 0x2000;
//        final int PPU_REG_TOP = 0x2007;
//        final int PPU_MIRROR_TOP = 0x3FFF;
//
//        int ppuRegisterCount = (PPU_REG_TOP - PPU_REG_BOT);
//        int mirrors = (PPU_MIRROR_TOP - PPU_REG_TOP) / ppuRegisterCount;
//
//        int[] logicalPpuRegistersMirror = new int[PPU_MIRROR_TOP - PPU_REG_TOP];
//        int[] physicalPpuRegisters = new int[PPU_REG_TOP - PPU_REG_BOT];
//
//        for (int mirrorIndex = 1; mirrorIndex <= mirrors; mirrorIndex++) {
//            for (int registerIndex = 0; registerIndex < ppuRegisterCount; registerIndex++) {
//                logicalPpuRegistersMirror[registerIndex] = PPU_REG_TOP + registerIndex;
//                physicalPpuRegisters[registerIndex * mirrorIndex] = PPU_REG_BOT + registerIndex;
//            }
//        }
//
//        MultiSourceMemory cpuMemory = new MultiSourceMemory().maintaining(mainMemory)
//                                                             .withMappingTo(logicalRamMirror, physicalRam, mainMemory)
//                                                             .withMappingTo(logicalPpuRegistersMirror, physicalPpuRegisters, mainMemory);
//
//        return cpuMemory;
//    }

    public void reset(){
        //Program ROM space set to
        //0xFFFC->0x80
        //0xFFFD->0x00
        // 0x4000 of ROM should be memory mapped to both 0x8000 and 0xC000
        mainMemory.setBlock(0xFFFC, new int[] {0x80, 0x00});
    }
}
