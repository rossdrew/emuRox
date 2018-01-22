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

    /**
     $0000-$07FF	$0800	2KB internal RAM
     $0800-$0FFF	$0800	Mirrors of $0000-$07FF
     $1000-$17FF	$0800
     $1800-$1FFF	$0800
     $2000-$2007	$0008	NES PPU registers
     $2008-$3FFF	$1FF8	Mirrors of $2000-2007 (repeats every 8 bytes)
     $4000-$4017	$0018	NES APU and I/O registers
     $4018-$401F	$0008	APU and I/O functionality that is normally disabled. See CPU Test Mode.
     $4020-$FFFF	$BFE0	Cartridge space: PRG ROM, PRG RAM, and mapper registers (See Note)

     * @return
     */
//    private Memory setupCpuMemoryMappings(){
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
//        final int PPU_REG_BOT = 0x2000;
//        final int PPU_REG_TOP = 0x2007;
//        final int PPU_MIRROR_TOP = 0x3FFF;
//
//        int[] logicalPpuRegistersMirror = new int[PPU_MIRROR_TOP - PPU_REG_TOP];
//        int[] physicalPpuRegisters = new int[PPU_REG_TOP - PPU_REG_BOT];
//        for (int registerIndex = 0; registerIndex < (PPU_REG_TOP - PPU_REG_BOT); registerIndex++){
//            logicalPpuRegistersMirror[registerIndex] = PPU_REG_TOP + registerIndex;
//            physicalPpuRegisters[registerIndex] = PPU_REG_BOT + registerIndex;
//        }
//
//        MultiSourceMemory cpuMemory = new MultiSourceMemory().maintaining(mainMemory)
//                                                             .withMappingTo(logicalRamMirror, physicalRam, mainMemory)
//                                                             .withMappingTo(logicalPpuRegistersMirror, physicalPpuRegisters, mainMemory);
//    }

    public void reset(){
        //Program ROM space set to
        //0xFFFC->0x80
        //0xFFFD->0x00
        // 0x4000 of ROM should be memory mapped to both 0x8000 and 0xC000
        mainMemory.setBlock(0xFFFC, new int[] {0x80, 0x00});
    }
}
