package com.rox.emu.processor.mos6502.op;

import com.rox.emu.UnknownOpCodeException;

/**
 * An enum representing possible addressing modes for {@link OpCode}
 *
 * @author Ross Drew
 */
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

    private final String name;
    private final int instructionBytes;

    AddressingMode(String name, int instructionBytes) {
        this.name = name;
        this.instructionBytes = instructionBytes;
    }

    public String getDescription(){
        return name;
    }

    public int getInstructionBytes(){
        return this.instructionBytes;
    }

    public AddressingMode xIndexed(){
        if (this == ZERO_PAGE){
            return ZERO_PAGE_X;
        }else if (this == ABSOLUTE) {
            return ABSOLUTE_X;
        }else if (this == INDIRECT){
            return INDIRECT_X;
        }else{
            throw new UnknownOpCodeException(this + " cannot be X indexed", this);
        }
    }

    public AddressingMode yIndexed(){
        if (this == ZERO_PAGE){
            return ZERO_PAGE_Y;
        }else if (this == ABSOLUTE){
            return ABSOLUTE_Y;
        }else if (this == INDIRECT){
            return INDIRECT_Y;
        }else{
            throw new UnknownOpCodeException(this + " cannot be Y indexed", this);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
