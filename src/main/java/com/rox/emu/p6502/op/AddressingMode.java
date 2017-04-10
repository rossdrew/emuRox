package com.rox.emu.p6502.op;

public enum AddressingMode {
    IMPLIED("Implied", 1),
    IMMEDIATE("Immediate", 2),
    ZERO_PAGE("Zero Page", 2),
    ZERO_PAGE_X("Zero Page [X]", 2),
    ZERO_PAGE_Y("Zero Page [Y]", 2),
    ABSOLUTE("Absolute", 3),
    ABSOLUTE_X("Absolute [X]", 3),
    ABSOLUTE_Y("Absolute [Y]", 3),
    INDIRECT("Indirect", 2),
    INDIRECT_X("Indirect, X", 2),
    INDIRECT_Y("Indirect, Y", 2),
    ACCUMULATOR("Accumulator", 1);

    private final String description;
    private final int instructionBytes;

    AddressingMode(String description, int instructionBytes) {
        this.description = description;
        this.instructionBytes = instructionBytes;
    }

    public String getDescription(){
        return description;
    }
}
