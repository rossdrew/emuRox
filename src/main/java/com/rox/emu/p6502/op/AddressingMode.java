package com.rox.emu.p6502.op;

public enum AddressingMode {
    IMPLIED("Implied"),
    IMMEDIATE("Immediate"),
    ZERO_PAGE("Zero Page"),
    ZERO_PAGE_X("Zero Page [X]"),
    ZERO_PAGE_Y("Zero Page [Y]"),
    ABSOLUTE("Absolute"),
    ABSOLUTE_X("Absolute [X]"),
    ABSOLUTE_Y("Absolute [Y]"),
    INDIRECT("Indirect"),
    INDIRECT_X("Indirect, X"),
    INDIRECT_Y("Indirect, Y"),
    ACCUMULATOR("Accumulator");

    private final String description;

    AddressingMode(String description) {
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
