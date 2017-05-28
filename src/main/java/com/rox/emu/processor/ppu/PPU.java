package com.rox.emu.processor.ppu;

import com.rox.emu.mem.Memory;

/**
 * Emulation of a NES (Nintendo Entertainment System) PPU (Picture Processing Unit) processor
 */
public class PPU {
    public enum Register {
        REG_CTRL_1(0x2000),
        REG_CTRL_2(0x2001);

        private final int memoryMappedLocation;

        public int getMemoryMappedLocation(){
            return memoryMappedLocation;
        }

        Register(int memoryMappedLocation) {
            this.memoryMappedLocation = memoryMappedLocation;
        }
    }

    private final Memory memory;

    public PPU(Memory memory) {
        this.memory = memory;
    }

    public int getRegister(Register registerID){
        return memory.getByte(registerID.memoryMappedLocation);
    }
}
