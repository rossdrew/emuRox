package com.rox.emu.processor.mos6502.util;

public class UnknownProgramElementException extends RuntimeException {
    public UnknownProgramElementException(final String s) {
        super(s);
    }
}
