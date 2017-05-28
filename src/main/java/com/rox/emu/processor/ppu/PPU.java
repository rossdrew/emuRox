package com.rox.emu.processor.ppu;

import com.rox.emu.mem.Memory;

/**
 * Emulation of a NES (Nintendo Entertainment System) PPU (Picture Processing Unit) processor.<br/>
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
 *         <td>7-Disable NMI to CPU at the end of each frame (V-Bank), 5-8x8/8x16, 2-Memory Inc (0 = 1 for horizontal, 1 = 32 for vertical)</td>
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
