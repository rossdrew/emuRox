package com.rox.emu.processor.ricoh2c02;

import com.rox.emu.mem.Memory;
import com.rox.emu.mem.MultiSourceMemory;

/**
 * Ricoh 2C02 CPU Registers<br/>
 *<br/>
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
 */
class Ricoh2C02Registers {
    public enum Register {
        /** Control register 1 */
        CTRL_1(0x2000),
        /** Control register 2 */
        CTRL_2(0x2001),
        /** Status register */
        STATUS(0x2002),
        /** Direct memory access register */
        DMA(0x4014);

        private final int memoryMappedLocation;

        public int getMemoryMappedLocation(){
            return memoryMappedLocation;
        }

        Register(int memoryMappedLocation) {
            this.memoryMappedLocation = memoryMappedLocation;
        }

        static int[] getMappedAddresses(){
            int[] addresses = new int[values().length];

            int i=0;
            for (final Register register : values()) {
                addresses[i++] = register.getMemoryMappedLocation();
            }

            return addresses;
        }
    }

    private final Memory cpuMemory;

    Ricoh2C02Registers(final Memory cpuMemory){
        this.cpuMemory = new MultiSourceMemory().withMapping(Register.getMappedAddresses(), cpuMemory);
    }

    public int getRegister(Register registerId) {
        return cpuMemory.getByte(registerId.getMemoryMappedLocation());
    }

    public void setRegister(Register register, int value) {
        cpuMemory.setByteAt(register.getMemoryMappedLocation(), value);
    }
}