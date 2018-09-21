package com.rox.emu.processor.mos6502.op;

import com.rox.emu.env.RoxByte;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;

/**
 * An instruction that can be {@code perform}ed on an addressed {@link RoxByte}.
 */
@FunctionalInterface
interface AddressedValueInstruction {
    /**
     * Perform an operation, in a specified environment using a {@link RoxByte} addressed externally.
     *
     * @param alu The Arithmetic Logic Unit (ALU) associated with the desired environment
     * @param registers The Registers associated with the desired environment
     * @param memory The Memory associated with the desired environment
     * @param value The addressed value that the operation is performed on
     * @return The {@link RoxByte} result of performing the operation in the specified environment
     */
    RoxByte perform(final Mos6502Alu alu,
                    final Registers registers,
                    final Memory memory,
                    final RoxByte value);
}
