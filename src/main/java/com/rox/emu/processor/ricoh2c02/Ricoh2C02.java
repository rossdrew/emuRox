package com.rox.emu.processor.ricoh2c02;

import com.rox.emu.mem.Memory;

/**
 * Emulation of a NES (Nintendo Entertainment System) PPU (Picture Processing Unit) processor, a Ricoh 2C02.<br/>
 */
public class Ricoh2C02 {
    private final Memory vRam;
    private final Memory sprRam;
    private final Ricoh2C02Registers registers;

    //XXX Should probably be a nicer interface, the existence of the 2C0s shouldn't depend on a cpu memory
    public Ricoh2C02(final Memory vRam, final Memory sprRam, final Memory cpuRam) {
        this.vRam = vRam;
        this.sprRam = sprRam;
        this.registers = new Ricoh2C02Registers(cpuRam);
    }

    public int getRegister(Ricoh2C02Registers.Register register){
        return registers.getRegister(register);
    }

    public void setRegister(Ricoh2C02Registers.Register register, int value) {
        this.registers.setRegister(register, value);
    }
}
