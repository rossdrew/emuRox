package com.rox.emu.processor.mos6502.op;

import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;

/**
 * @author Ross W. Drew
 */
interface Addressable {
    void address(final Registers r,
                 final Memory m,
                 final Mos6502Alu alu,
                 final AddressedValueInstruction i);
}
