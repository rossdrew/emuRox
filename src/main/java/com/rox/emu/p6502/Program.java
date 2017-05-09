package com.rox.emu.p6502;


import com.rox.emu.p6502.op.OpCode;

/**
 * An immutable 6502 Program which is essentially a byte array which can be added to from different types neatly
 */
public class Program {
    private int[] programBytes = new int[] {};

    public Program(){
        this(new int[]{});
    }

    private Program(int[] programBytes){
        this.programBytes = programBytes;
    }

    public Program with(int byteValue){
        int[] newProgramBytes = new int[programBytes.length + 1];
        System.arraycopy(programBytes,0, newProgramBytes, 0, programBytes.length);
        newProgramBytes[newProgramBytes.length-1] = byteValue;
        return new Program(newProgramBytes);
    }

    public Program with(OpCode opCode){
        return this.with(opCode.getByteValue());
    }

    public Program with(Object value){
        //XXX I'd need to abstract away the concept of program-byte in order to make this nicer
        if (value instanceof OpCode)
            return this.with((OpCode) value);

        return this.with((int)value);
    }

    public Program with(Object ... values){
        Program tmpProgram = this;

        for (Object value : values) {
            tmpProgram = tmpProgram.with(value);
        }

        return tmpProgram;
    }

    public int[] getProgramAsByteArray() {
        return programBytes.clone();
    }

    public int getLength() {
        return programBytes.length;
    }
}
