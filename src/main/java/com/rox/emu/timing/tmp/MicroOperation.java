package com.rox.emu.timing.tmp;

import com.rox.emu.processor.mos6502.util.Program;

@FunctionalInterface
public interface MicroOperation {
    State execute(final State inputState);
}
