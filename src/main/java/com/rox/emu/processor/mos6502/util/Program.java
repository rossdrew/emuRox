package com.rox.emu.processor.mos6502.util;


import com.rox.emu.UnknownTokenException;
import com.rox.emu.env.RoxByte;
import com.rox.emu.processor.mos6502.op.Mos6502OpCode;

import java.util.*;

/**
 * An immutable 6502 Program which is essentially a byte array which can
 * be added to from different types neatly.
 *
 * @author Ross Drew
 */
public class Program {
    private final int[] programBytes;
    private final Map<String, Integer> programLabels;
    private final List<Reference> references;

    /**
     * An inline label reference
     */
    public static class Reference {
        private final String targetLabel;
        private final int rootAddress;

        Reference(final String target, final int root){
            this.targetLabel = target;
            this.rootAddress = root;
        }
    }

    public Reference referenceBuilder(String targetLabel){
        return new Reference(targetLabel, programBytes.length);
    }

    public Program(){
            this(new int[]{});
    }

    private Program(int[] programBytes){
        this.programBytes = programBytes;
        this.programLabels = Collections.emptyMap();
        this.references = new ArrayList<>();
    }

    private Program(final int[] programBytes,
                    final Map<String, Integer> programLabels,
                    final List<Reference> references){
        this.programBytes = programBytes;
        this.programLabels = programLabels;
        this.references = references;
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
        return new Program(newProgramBytes, this.programLabels, references);
    }

    /**
     * Create a new {@link Program} with a {@link Mos6502OpCode} appended
     *
     * @param opCode ({@link Mos6502OpCode}) to append
     * @return a new {@link Program} that is a copy of this one with the new {@link Mos6502OpCode} appended
     */
    public Program with(Mos6502OpCode opCode) {
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
        return new Program(this.programBytes, newProgramLabels, references);
    }

    /**
     * Create a new {@link Program} with a label reference appended
     *
     * @param reference the {@link Reference} to the desired program label
     * @return a new {@link Program} that is a copy of this one with the new label {@link Reference} appended
     */
    public Program with(Reference reference){
        int[] newProgramBytes = new int[programBytes.length + 1];
        System.arraycopy(programBytes,0, newProgramBytes, 0, programBytes.length);
        newProgramBytes[newProgramBytes.length-1] = 0b1111111100000000;
        final List<Reference> newReferences = new ArrayList<>(references);
        newReferences.add(new Reference(reference.targetLabel, newProgramBytes.length-1));
        return new Program(newProgramBytes, this.programLabels, newReferences);
    }

    /**
     * Create a new {@link Program} with new entry appended
     *
     * @param value [{@link int} | {@link Mos6502OpCode} | {@link String}] to append
     * @return a new {@link Program} that is a copy of this one with the new entry appended
     */
    public Program with(Object value){
        //XXX I'd need to abstract away the concept of program-byte in order to make this nicer
        if (value instanceof Mos6502OpCode)
            return this.with((Mos6502OpCode) value);

        if (value instanceof String)
            return this.with((String) value);

        if (value instanceof Reference)
            return this.with((Reference) value);

        if (value instanceof Number)
            return this.with(((Number) value).intValue());

        throw new RuntimeException("Unknown program element of type " + value.getClass());
    }

    /**
     * Create a new {@link Program} with new entries appended
     *
     * @param values [{@link int} | {@link Mos6502OpCode} | {@link String}] to append
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
    public RoxByte[] getProgramAsByteArray() {
        int[] clonedBytes = programBytes.clone();

        for (Reference reference : references) {
            if (programLabels.containsKey(reference.targetLabel)){
                int targetAddress = programLabels.get(reference.targetLabel);

                //XXX Should be a binary subtraction?
                int relativeAddress = ((targetAddress & 0xFF) - (reference.rootAddress + 1 & 0xFF)) & 0xFF;
                clonedBytes[reference.rootAddress] = relativeAddress;
            }else{
                throw new UnknownTokenException("Unknown label reference '" + reference.targetLabel + "'", reference.targetLabel );
            }
        }

        final RoxByte[] programRoxBytes = new RoxByte[clonedBytes.length];
        for (int i=0; i<programRoxBytes.length; i++)
            programRoxBytes[i] = RoxByte.fromLiteral(clonedBytes[i]);


        return programRoxBytes;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()){
            if (o instanceof Collection)
                return arrayMatches(((Collection)o).toArray());

            if (o.getClass().isArray()){
                return arrayMatches((Object[])o);

            }else{
                return false;
            }
        }

        Program program = (Program) o;
        return Arrays.equals(programBytes, program.programBytes);
    }

    private boolean arrayMatches(final Object[] o){
        if (o.length != programBytes.length)
            return false;

        for (int i=0; i<o.length; i++){
            if (o[i] instanceof Number && (((Number)o[i]).intValue() != programBytes[i]))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(programBytes);
    }
}
