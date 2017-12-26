package com.rox.emu.processor.ricoh2c02;

import com.rox.emu.mem.Memory;
import com.rox.emu.mem.MultiSourceMemory;

class Ricoh2C02Registers {
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

    private final Memory cpuMemory;

    Ricoh2C02Registers(final Memory cpuMemory){
        this.cpuMemory = new MultiSourceMemory().withMapping(Register.REG_CTRL_1.getMemoryMappedLocation(), cpuMemory)
                                                .withMapping(Register.REG_CTRL_2.getMemoryMappedLocation(), cpuMemory);
    }

    public int getRegister(Register registerId) {
        return cpuMemory.getByte(registerId.getMemoryMappedLocation());
    }

    public void setRegister(Register register, int value) {
        cpuMemory.setByteAt(register.getMemoryMappedLocation(), value);
    }
}