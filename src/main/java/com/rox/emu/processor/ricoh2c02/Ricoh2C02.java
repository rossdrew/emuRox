package com.rox.emu.processor.ricoh2c02;

import com.rox.emu.mem.Memory;
import com.rox.emu.mem.MultiSourceMemory;

/**
 * Emulation of a NES (Nintendo Entertainment System) PPU (Picture Processing Unit) processor, a Ricoh 2C02.<br/>
 * <br/>
 * <h4>Memory Mapped Registers</h4>
 * <table>
 *     <tr>
 *         <th>Location</th>
 *         <th>Description</th>
 *         <th>Function</th>
 *     </tr>
 *
 *     <tr>
 *         <td>$2001</td>
 *         <td>PPU Control Register 1</td>
 *         <td>7-Disable NMI to Mos6502 at the end of each frame (V-Bank), 5-8x8/8x16, 2-Memory Inc (0 = 1 for horizontal, 1 = 32 for vertical)</td>
 *      </tr>
 *      <tr>
 *         <td>$2002</td>
 *         <td>PPU Control Register 2</td>
 *         <td>4-Hide Sprites, 3-Hide background.  A read from here clears byte 7 and registers @ $2005 & $2006</td>
 *     </tr>
 *      <tr>
 *         <td>$2003</td>
 *         <td>PPU Status Register <em>Read Only</em></td>
 *         <td>7-V-Blank occurring, 6,5-(sprite), 4-if writes to RAM accepted</td>
 *     </tr>
 *
 *     <tr> <td>$2003</td><td></td><td></td> </tr>
 *     <tr> <td>$2004</td><td></td><td></td> </tr>
 *     <tr> <td>$2005</td><td></td><td></td> </tr>
 *     <tr> <td>$2006</td><td></td><td></td> </tr>
 *     <tr> <td>$2007</td><td></td><td></td> </tr>
 *
 *     <tr>
 *          <td>$4014</td>
 *          <td>Direct Memory Access</td>
 *          <td></td>
 *     </tr>
 *
 * </table>
 *
 */
public class Ricoh2C02 {
    private final Memory vRam;
    private final Ricoh2C02Registers registers;

    //XXX Should probably be a nicer interface, the existence of the 2C0s shouldn't depend on a cpu memory
    public Ricoh2C02(final Memory vRam, final Memory cpuRam) {
        this.vRam = vRam;
        this.registers = new Ricoh2C02Registers(cpuRam);
    }

    //This is wrong, registers aren't retrieved from VRAM, they are memory mapped to cpuRam
    public int getRegister(Ricoh2C02Registers.Register register){
        return registers.getRegister(register);
    }

    public void setRegister(Ricoh2C02Registers.Register register, int value) {
        this.registers.setRegister(register, value);
    }
}
