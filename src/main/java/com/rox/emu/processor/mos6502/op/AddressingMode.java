package com.rox.emu.processor.mos6502.op;

import com.rox.emu.UnknownOpCodeException;

/**
 * An enum representing possible addressing modes for {@link OpCode}
 *
 * @author Ross Drew
 */
public enum AddressingMode {
    /** Expects no argument */
    IMPLIED("Implied", 1),

    /** Expects a one byte argument that is a literal value for use in the operation */
    IMMEDIATE("Immediate", 2),

    /** Expects a one byte argument that contains a zero page address to use in the operation. Can be indexed
     *  as {@link #ZERO_PAGE_X} or {@link #ZERO_PAGE_Y} */
    ZERO_PAGE("Zero Page", 2),

    /** Expects a one byte argument that contains a zero page address and the X Register to be filled with an
     *  offset value, to use in the operation */
    ZERO_PAGE_X("Zero Page [X]", 2),

    /** Expects a one byte argument that contains a zero page address and the Y Register to be filled with an
     *  offset value, to use in the operation */
    ZERO_PAGE_Y("Zero Page [Y]", 2),

    /** Expects a 2 byte argument that contains an absolute address for use in the operation. Can be indexed
     *  as {@link #ABSOLUTE_X} or {@link #ABSOLUTE_Y} */
    ABSOLUTE("Absolute", 3),

    /** Expects a 2 byte argument that contains an absolute address and the X Register to be filled with an
     *  offset value, to use in the operation */
    ABSOLUTE_X("Absolute [X]", 3),

    /** Expects a 2 byte argument that contains an absolute address and the Y Register to be filled with an
     *  offset value, to use in the operation */
    ABSOLUTE_Y("Absolute [Y]", 3),

    /** Expects a one byte argument that contains a zero page address that contains the two byte address,
     *  to use in the operation.  Can be indexed as {@link #INDIRECT_X} or {@link #INDIRECT_Y} */
    INDIRECT("Indirect", 2),

    /** <i>Indexed indirect</i>: Expects a one byte argument and an offset in the X Register added together they
     *  give an address in Zero Page that itself contains a two byte address to be used in the operation */
    INDIRECT_X("Indirect, X", 2),

    /** <i>Indirect indexed</i>: Expects a one byte argument and an offset in the Y Register.  A two byte address
     *  is fetched from the Zero Page location pointed to by the argument, the offset is added to this address which
     *  gives the two byte address to be used in the operation  */
    INDIRECT_Y("Indirect, Y", 2),

    /** Expects no argument, operation will be performed using the Accumulator Register*/
    ACCUMULATOR("Accumulator", 1),

    /** Expects a one byte argument that is the offset for a branch instruction */
    RELATIVE("Relative", 2);

    private final String name;
    /* Bytes required to perform this instruction including a byte for the opcode and then it's arguments */
    private final int instructionBytes;

    AddressingMode(final String name, final int instructionBytes) {
        this.name = name;
        this.instructionBytes = instructionBytes;
    }

    /**
     * @return the {@link String} description of this addressing mode
     */
    public String getDescription(){
        return name;
    }

    /**
     * @return the number of bytes needed to make up this instruction, including the instruction byte
     */
    public int getInstructionBytes(){
        return this.instructionBytes;
    }

    /**
     * Convert this addressing mode, to x indexed. Instructions that can be X Indexed are:-
     * <ul>
     *     <li>{@link #ZERO_PAGE}</li>
     *     <li>{@link #ABSOLUTE}</li>
     *     <li>{@link #INDIRECT}</li>
     * </ul>
     * @return the {@link AddressingMode} that corresponds to this {@link AddressingMode}, but indexed by X
     * @throws UnknownOpCodeException if there is an attempt to X index an addressing mode that cannot be X indexed
     */
    public AddressingMode xIndexed() throws UnknownOpCodeException{
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

    /**
     * Convert this addressing mode, to y indexed. Instructions that can be Y Indexed are:-
     * <ul>
     *     <li>{@link #ZERO_PAGE}</li>
     *     <li>{@link #ABSOLUTE}</li>
     *     <li>{@link #INDIRECT}</li>
     * </ul>
     * @return the {@link AddressingMode} that corresponds to this {@link AddressingMode}, but indexed by Y
     * @throws UnknownOpCodeException if there is an attempt to Y index an addressing mode that cannot be Y indexed
     */
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
