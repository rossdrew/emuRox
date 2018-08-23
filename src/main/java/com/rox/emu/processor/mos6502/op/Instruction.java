package com.rox.emu.processor.mos6502.op;

import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;

@FunctionalInterface
public interface Instruction {
    void perform(Registers r, Memory m, Mos6502Alu alu);
}
