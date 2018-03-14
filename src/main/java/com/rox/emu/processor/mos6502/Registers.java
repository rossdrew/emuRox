package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            int spaceIndex = name.indexOf(' ');
            if (spaceIndex > 0)
                name = name.substring(0, spaceIndex) + name.substring(spaceIndex, spaceIndex+2).toUpperCase() + name.substring(spaceIndex+2);
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
        registerValue[STACK_POINTER_LOW.getIndex()] = RoxByte.fromLiteral(0b11111111);
        registerValue[STATUS_FLAGS.getIndex()] = RoxByte.fromLiteral(0b00000000);
    }

    /**
     * @param register the registerValue to set
     * @param value to set the registerValue to
     */
    public void setRegister(Register register, int value){
        log.debug("'R:{}' := {}", register.getDescription(), value);
        registerValue[register.getIndex()] = RoxByte.fromLiteral(value);
    }

    /**
     * @param register from which to get the value
     * @return the value of the desired registerValue
     */
    public int getRegister(Register register){
        return registerValue[register.getIndex()].getRawValue();
    }

    /**
     * Set the given registerValue to the given value and set the flags registerValue based on that value
     *
     * @param register the registerValue to set
     * @param value to set the registerValue to
     */
    public void setRegisterAndFlags(Register register, int value){
        int valueByte = value & 0xFF;
        setRegister(register, valueByte);
        setFlagsBasedOn(valueByte);
    }

    /**
     * @param pcWordValue to set the Program Counter to
     */
    public void setPC(int pcWordValue){
        setRegister(PROGRAM_COUNTER_HI, pcWordValue >> 8);
        setRegister(PROGRAM_COUNTER_LOW, pcWordValue & 0xFF);
        log.debug("'R+:Program Counter' := {} [ {} | {} ]", new String[] {Integer.toString(pcWordValue),
                                                                          Integer.toString(getRegister(PROGRAM_COUNTER_HI)),
                                                                          Integer.toString(getRegister(PROGRAM_COUNTER_LOW))});
    }

    /**
     * @return the two byte value of the Program Counter
     */
    public int getPC(){
        return (getRegister(PROGRAM_COUNTER_HI) << 8) | getRegister(PROGRAM_COUNTER_LOW);
    }

    /**
     * Increment the Program Counter then return it's value
     *
     * @return the new value of the Program Counter
     */
    public int getNextProgramCounter(){
        setPC(getPC()+1);
        return getPC();
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
    public void setFlagsBasedOn(int value){
        int valueByte = value & 0xFF;
        setZeroFlagFor(valueByte);
        setNegativeFlagFor(valueByte);
    }

    /**
     * Set zero flag if given argument is 0
     */
    public void setZeroFlagFor(int value){
        setFlagTo(Flag.ZERO, value == 0);
    }

    /**
     * Set negative flag if given argument is 0
     */
    public void setNegativeFlagFor(int value){
        setFlagTo(Flag.NEGATIVE, isNegative(value));
    }

    private boolean isNegative(int fakeByte){
        return RoxByte.fromLiteral(fakeByte).isNegative();
//        return (fakeByte & STATUS_FLAG_NEGATIVE) == STATUS_FLAG_NEGATIVE;  ///What was this about?
    }
}
