package com.rox.emu.processor.mos6502.op;

import com.rox.emu.env.RoxByte;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;

@FunctionalInterface
interface AddressedValueInstruction {
    RoxByte perform(final Mos6502Alu alu,
                    final Registers registers,
                    final Memory memory,
                    final RoxByte value);
}
