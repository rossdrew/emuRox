package com.rox.emu.processor.ricoh2c02;

import com.rox.emu.env.RoxByte;
import com.rox.emu.mem.Memory;

/**
 * Emulation of a NES (Nintendo Entertainment System) PPU (Picture Processing Unit) processor, a Ricoh 2C02.<br/>
 *
 * Address Space: contains 10 kilobytes of memory:
 *   - 8 kilobytes of ROM or RAM on the Game Pak (possibly more with one of the common mappers) to store the shapes of background and sprite tiles
 *   - 2 kilobytes of RAM in the console to store a map or two.
 *
 *   Two separate, smaller address spaces (These are internal to the PPU itself) hold:
 *   - a palette [static mem], which controls which colors are associated to various indices
 *   - OAM (Object Attribute Memory) [dynamic mem], which stores the position, orientation, shape, and color of the sprites, or independent moving objects.
 */
public class Ricoh2C02 {
    private final Memory vRam;
    private final Memory oam;
    private final Ricoh2C02Registers registers;

    public Ricoh2C02(final Memory vRam,
                     final Memory oam,
                     final Ricoh2C02Registers registers) {
        this.vRam = vRam;
        this.oam = oam;
        this.registers = registers;
    }

    public RoxByte getRegister(Ricoh2C02Registers.Register register){
        return registers.getRegister(register);
    }

    public void setRegister(Ricoh2C02Registers.Register register, RoxByte value) {
        this.registers.setRegister(register, value);
    }
}
