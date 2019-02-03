package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.rox.emu.processor.mos6502.Registers.Register.*;

/**
 * A representation of the MOS 6502 CPU registers
 *
 * @author Ross Drew
 */
public class Registers {
    private static final Logger log = LoggerFactory.getLogger(Registers.class);

    /**
     * A single registerValue for a MOS 6502 containing information on registerValue id and name
     */
    public enum Register {
        ACCUMULATOR(0),
        Y_INDEX(1),
        X_INDEX(2),
        PROGRAM_COUNTER_HI(3),
        PROGRAM_COUNTER_LOW(4),
        STACK_POINTER_LOW(5),
        STACK_POINTER_HI(6),
        STATUS_FLAGS(7);

        private final String description;
        private final int index;

        Register(int index){
            this.index = index;
            description = prettifyName(name());
        }

        private static String prettifyName(String originalName){
            String name = originalName.replaceAll("_"," ")
                                      .toLowerCase()
                                      .replace("hi","(High)")
                                      .replace("low","(Low)");

            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            int spaceIndex = name.indexOf(' ');
            if (spaceIndex > 0)
                name = name.substring(0, spaceIndex) + name.substring(spaceIndex, spaceIndex+2).toUpperCase() + name.substring(spaceIndex+2);
            return name;
        }

        public String getDescription(){
            return description;
        }

        public int getIndex(){
            return this.index;
        }
    }

    /**
     * A MOS 6502 status flag
     */
    public enum Flag {
        CARRY(0),
        ZERO(1),
        IRQ_DISABLE(2),
        DECIMAL_MODE(3),
        BREAK(4),
        UNUSED(5),
        OVERFLOW(6),
        NEGATIVE(7);

        private final int index;
        private final int placeValue;
        private final String description;

        private static String prettifyName(String originalName){
            String name = originalName.replaceAll("_"," ").toLowerCase();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            if (name.contains(" ")) {
                int spaceIndex = name.indexOf(' ');
                name = name.substring(0, spaceIndex) + name.substring(spaceIndex, spaceIndex + 2).toUpperCase() + name.substring(spaceIndex + 2);
            }
            return name;
        }

        Flag(int index) {
            this.index = index;
            this.placeValue = (1 << index);
            description = prettifyName(name());
        }

        public String getDescription() {
            return description;
        }

        public int getIndex() {
            return index;
        }

        public int getPlaceValue() {
            return placeValue;
        }
    }

    private final RoxByte[] registerValue;

    public Registers(){
        registerValue = new RoxByte[8];
        for (int i=0; i<8; i++)
            registerValue[i] = RoxByte.ZERO;
        setRegister(STACK_POINTER_LOW, RoxByte.fromLiteral(0b11111111));
        setRegister(STATUS_FLAGS, RoxByte.fromLiteral(0b00000000));
    }

    /**
     * @param register the registerValue to set
     * @param value to set the registerValue to
     */
    public void setRegister(Register register, RoxByte value){
        log.debug("'R:{}' := {}", register.getDescription(), value);
        registerValue[register.getIndex()] = (value == null ? RoxByte.ZERO : value);
    }

    /**
     * @param register from which to get the value
     * @return the value of the desired registerValue
     */
    public RoxByte getRegister(Register register){
        return registerValue[register.getIndex()];
    }

    /**
     * @param pcWordValue to set the Program Counter to
     */
    public void setPC(RoxWord pcWordValue){
        setRegister(PROGRAM_COUNTER_HI, pcWordValue.getHighByte());
        setRegister(PROGRAM_COUNTER_LOW, pcWordValue.getLowByte());
        log.debug("'R+:Program Counter' := {} [ {} | {} ]", new Object[] {pcWordValue,
                                                                          getRegister(PROGRAM_COUNTER_HI),
                                                                          getRegister(PROGRAM_COUNTER_LOW)});
    }

    /**
     * @return the two byte value of the Program Counter
     */
    public RoxWord getPC(){
        return (RoxWord.from(getRegister(PROGRAM_COUNTER_HI), getRegister(PROGRAM_COUNTER_LOW)));
    }

    /**
     * Increment the Program Counter then return it's value
     *
     * @return the new value of the Program Counter
     */
    public RoxWord getNextProgramCounter(){
        setPC(RoxWord.fromLiteral(getPC().getRawValue()+1));
        return getPC();
    }

    /**
     * Get the Program Counter value then increment
     *
     * @return the value of the Program Counter
     */
    public RoxWord getAndStepProgramCounter(){
        final RoxWord pc = getPC();
        setPC(RoxWord.fromLiteral(getPC().getRawValue()+1));
        return pc;
    }

    /**
     * @param flag flag to test
     * @return <code>true</code> if the specified flag is set, <code>false</code> otherwise
     */
    public boolean getFlag(Flag flag) {
        return registerValue[STATUS_FLAGS.getIndex()].isBitSet(flag.getIndex());
    }

    /**
     * @param flag for which to set the state
     * @param state to set the flag to
     */
    public void setFlagTo(Flag flag, boolean state) {
        if (state)
            setFlag(flag);
        else
            clearFlag(flag);
    }

    /**
     * Set* the specified flag of the status register to 1
     *
     * @param flag for which to set to true
     */
    public void setFlag(Flag flag) {
        log.debug("'F:{}' -> SET", flag.description);
        registerValue[STATUS_FLAGS.getIndex()] = registerValue[STATUS_FLAGS.getIndex()].withBit(flag.getIndex());
    }

    /**
     * Set/clear the specified flag of the status register to 0
     *
     * @param flag to be cleared
     */
    public void clearFlag(Flag flag){
        log.debug("'F:{}' -> CLEARED", flag.getDescription());
        registerValue[STATUS_FLAGS.getIndex()] = registerValue[STATUS_FLAGS.getIndex()].withoutBit(flag.getIndex());
    }

    /**
     * @param value to set the status flags based on
     */
    public void setFlagsBasedOn(RoxByte value){
        setZeroFlagFor(value.getRawValue());
        setNegativeFlagFor(value.getRawValue());
    }

    /**
     * Set zero flag if given argument is 0
     */
    private void setZeroFlagFor(int value){
        setFlagTo(Flag.ZERO, value == 0);
    }

    /**
     * Set negative flag if given argument is 0
     */
    private void setNegativeFlagFor(int value){
        setFlagTo(Flag.NEGATIVE, isNegative(value));
    }

    private boolean isNegative(int fakeByte){
        return RoxByte.fromLiteral(fakeByte).isNegative();
    }

    public Registers copy(){
        final Registers newRegisters = new Registers();
        for (int i=0; i< registerValue.length; i++)
            newRegisters.registerValue[i] = registerValue[i].copy();
        return newRegisters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registers registers = (Registers) o;
        return Arrays.equals(registerValue, registers.registerValue);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(registerValue);
    }
}
