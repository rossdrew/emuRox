package com.rox.emu.processor.mos6502.op;

import com.rox.emu.env.RoxByte;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;

/**
 * A wrapper for an {@link AddressedValueInstruction} for addressing the {@link RoxByte} in it's argument to make it "Addressable"<br/>
 *<br/>
 * XXX Perhaps a better name is required?
 */
@FunctionalInterface
interface Addressable {
    /**
     * Address a value in the provided environment and use it in a {@link AddressedValueInstruction}
     *
     * @param r The Registers associated with the desired environment
     * @param m The Memory associated with the desired environment
     * @param alu The Arithmetic Logic Unit (ALU) associated with the desired environment
     * @param i The {@link AddressedValueInstruction} called using the addressed value
     */
    void address(final Registers r,
                 final Memory m,
                 final Mos6502Alu alu,
                 final AddressedValueInstruction i);
}
