package com.rox.emu.processor.mos6502.op;

import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;

/**
 * A {@link com.rox.emu.processor.mos6502.Mos6502} instruction which can be {@code perform}ed
 * in a given {@link com.rox.emu.processor.mos6502.Mos6502} environment.
 */
@FunctionalInterface
interface Mos6502Instruction {
    /**
     * Perform this operation in the specified environment
     *
     * @param alu
     * @param registers
     * @param memory
     */
    void perform(final Mos6502Alu alu,
                 final Registers registers,
                 final Memory memory);
}
