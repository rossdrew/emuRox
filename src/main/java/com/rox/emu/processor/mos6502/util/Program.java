package com.rox.emu.processor.mos6502.util;


import com.rox.emu.processor.mos6502.op.OpCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An immutable 6502 Program which is essentially a byte array which can
 * be added to from different types neatly.
 *
 * @author Ross Drew
 */
public class Program {
    private final int[] programBytes;
    private final Map<String, Integer> programLabels;

    public Program(){
        this(new int[]{});
    }

    private Program(int[] programBytes){
        this.programBytes = programBytes;
        this.programLabels = Collections.emptyMap();
    }

    private Program(int[] programBytes, Map<String, Integer> programLabels){
        this.programBytes = programBytes;
        this.programLabels = programLabels;
    }

    /**
     * Create a new {@link Program} with a byte value appended
     *
     * @param byteValue ({@link int}) to append
     * @return a new {@link Program} that is a copy of this one with the new value appended
     */
    public Program with(int byteValue){
        int[] newProgramBytes = new int[programBytes.length + 1];
        System.arraycopy(programBytes,0, newProgramBytes, 0, programBytes.length);
        newProgramBytes[newProgramBytes.length-1] = byteValue;
        return new Program(newProgramBytes, this.programLabels);
    }

    /**
     * Create a new {@link Program} with a {@link OpCode} appended
     *
     * @param opCode ({@link OpCode}) to append
     * @return a new {@link Program} that is a copy of this one with the new {@link OpCode} appended
     */
    public Program with(OpCode opCode) {
        return this.with(opCode.getByteValue());
    }

    /**
     * Create a new {@link Program} with a label appended
     *
     * @param label {@link String} id of the desired label
     * @return a new {@link Program} that is a copy of this one with the new label appended
     */
    public Program with(String label){
        final Map<String, Integer> newProgramLabels = new HashMap<>(programLabels);
        newProgramLabels.put(label, programBytes.length);
        return new Program(this.programBytes, newProgramLabels);
    }

    /**
     * Create a new {@link Program} with new entry appended
     *
     * @param value [{@link int} | {@link OpCode} | {@link String}] to append
     * @return a new {@link Program} that is a copy of this one with the new entry appended
     */
    public Program with(Object value){
        //XXX I'd need to abstract away the concept of program-byte in order to make this nicer
        if (value instanceof OpCode)
            return this.with((OpCode) value);

        if (value instanceof String)
            return this.with((String) value);

        return this.with((int)value);
    }

    /**
     * Create a new {@link Program} with new entries appended
     *
     * @param values [{@link int} | {@link OpCode} | {@link String}] to append
     * @return a new {@link Program} that is a copy of this one with the new entries appended
     */
    public Program with(Object ... values){
        Program tmpProgram = this;

        for (Object value : values) {
            tmpProgram = tmpProgram.with(value);
        }

        return tmpProgram;
    }

    /**
     * @return This {@link Program} compiled to a "byte" array.
     */
    public int[] getProgramAsByteArray() {
        return programBytes.clone();
    }

    /**
     * @return The byte length of this {@link Program}
     */
    public int getLength() {
        return programBytes.length;
    }

    /**
     * @return The list of labels held for this {@link Program}
     */
    public Set<String> getLabels() {
        return programLabels.keySet();
    }

    /**
     * @param labelName A {@link String} label representing a location in the program
     * @return the {@link int} location of this label in the {@link Program}
     */
    public int getLocationOf(String labelName) {
        return programLabels.get(labelName);
    }
}
