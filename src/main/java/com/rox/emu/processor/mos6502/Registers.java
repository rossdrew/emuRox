package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A representation of the MOS 6502 CPU registers
 *
 * @author Ross Drew
 */
public class Registers {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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
        private final int placeValue;

        Register(int index){
            this.index = index;
            this.placeValue = 1 << index;
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

        public int getPlaceValue(){
            return this.placeValue;
        }
    }

//    /** Register ID of the Accumulator */
//    public static final int REG_ACCUMULATOR = 0;
//    /** Register ID of the Y Index register */
//    public static final int REG_Y_INDEX = 1;
//    /** Register ID of the X Index register */
//    public static final int REG_X_INDEX = 2;
//    /** Register ID of the high byte of the Program Counter */
//    public static final int REG_PC_HIGH = 3;
//    /** Register ID of the low byte of the Program Counter */
//    public static final int REG_PC_LOW = 4;
//    /** Register ID of the <em>fixed value</em> high byte of the Stack Pointer */
//    private static final int REG_SP_X = 5;
//    /** Register ID of the low byte of the Stack Pointer */
//    public static final int REG_SP = 6;
//    /** Register ID of the Status flag byte */
//    public static final int REG_STATUS = 7;
//
//    private static final String[] registerNames = new String[] {"Accumulator", "Y Index", "X Index", "Program Counter (Hi)", "Program Counter (Low)", "<SP>", "Stack Pointer", "Status Flags"};
//
    /** Place value of Carry status flag in bit {@value #C} */
    public static final int STATUS_FLAG_CARRY = 0x1;
    /** Place value of Zero status flag in bit {@value #Z} */
    public static final int STATUS_FLAG_ZERO = 0x2;
    /** Place value of Interrupt status flag in bit {@value #I} */
    public static final int STATUS_FLAG_IRQ_DISABLE = 0x4;
    /** Place value of Binary Coded Decimal status flag in bit {@value #D} */
    public static final int STATUS_FLAG_DEC = 0x8;
    /** Place value of Break status flag in bit {@value #B} */
    public static final int STATUS_FLAG_BREAK = 0x10;
    private static final int STATUS_FLAG_UNUSED = 0x20; //Placeholder only
    /** Place value of Overflow status flag in bit {@value #V} */
    public static final int STATUS_FLAG_OVERFLOW = 0x40;
    /** Place value of Negative status flag in bit {@value #N} */
    public static final int STATUS_FLAG_NEGATIVE = 0x80;

    /** Bit place of Negative status flag */
    public static final int N = 7;
    /** Bit place of Overflow status flag */
    public static final int V = 6;
    /** - <em>UNUSED</em> (Placeholder flag only) **/
    private static final int U = 5; //Placeholder only
    /** Bit place of Break status flag */
    public static final int B = 4;
    /** Bit place of Binary Coded Decimal status flag */
    public static final int D = 3;
    /** Bit place of Interrupt status flag */
    public static final int I = 2;
    /** Bit place of Zero status flag */
    public static final int Z = 1;
    /** Bit place ofCarry status flag */
    public static final int C = 0;

    private static final String[] flagNames = new String[] {"Carry", "Zero", "IRQ Disable", "Decimal Mode", "BRK Command", "<UNUSED>", "Overflow", "Negative"};

    private final RoxByte[] register;

    public Registers(){
        register = new RoxByte[8];
        for (int i=0; i<8; i++)
            register[i] = RoxByte.ZERO;
        register[Register.STACK_POINTER_LOW.index]   = RoxByte.fromLiteral(0b11111111);
        register[Register.STATUS_FLAGS.index] = RoxByte.fromLiteral(0b00000000);
    }

    /**
     * @param reg of the register to set
     * @param val to set the register to
     */
    public void setRegister(Register reg, int val){
        LOG.debug("'R:" + reg.getDescription() + "' := " + val);
        register[reg.index] = RoxByte.fromLiteral(val);
    }

    /**
     * @param reg for which to get the value
     * @return the value of the desired register
     */
    public int getRegister(Register reg){
        return register[reg.index].getRawValue();
    }

    /**
     * Set the given register to the given value and set the flags register based on that value
     *
     * @param reg of the register to set
     * @param value to set the register to
     */
    public void setRegisterAndFlags(Register reg, int value){
        int valueByte = value & 0xFF;
        setRegister(reg, valueByte);
        setFlagsBasedOn(valueByte);
    }

    /**
     * @param newPCWord to set the Program Counter to
     */
    public void setPC(int newPCWord){
        setRegister(Register.PROGRAM_COUNTER_HI, newPCWord >> 8);
        setRegister(Register.PROGRAM_COUNTER_LOW, newPCWord & 0xFF);
        LOG.debug("'R+:Program Counter' := " + newPCWord + " [ " + getRegister(Register.PROGRAM_COUNTER_HI) + " | " + getRegister(Register.PROGRAM_COUNTER_LOW) + " ]");
    }

    /**
     * @return the two byte value of the Program Counter
     */
    public int getPC(){
        return (getRegister(Register.PROGRAM_COUNTER_HI) << 8) | getRegister(Register.PROGRAM_COUNTER_LOW);
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
     * @param flagNumber for which to get the name
     * @return the {@link String} name of the given flag
     */
    public static String getFlagName(int flagNumber){
        if (flagNumber < 0 || flagNumber > 7)
            throw new IllegalArgumentException("Unknown 6502 Flag ID:" + flagNumber);
        return flagNames[flagNumber];
    }

    /**
     * @param flagBitNumber flag to test
     * @return <code>true</code> if the specified flag is set, <code>false</code> otherwise
     */
    public boolean getFlag(int flagBitNumber) {
        return register[Register.STATUS_FLAGS.index].isBitSet(flagBitNumber);
    }

    /**
     * @param flagBitNumber for which to set the state
     * @param state to set the flag to
     */
    public void setFlagTo(int flagBitNumber, boolean state) {
        if (state)
            setFlag(flagBitNumber);
        else
            clearFlag(flagBitNumber);
    }

    /**
     * @param flagBitNumber for which to set to true
     */
    public void setFlag(int flagBitNumber) {
        LOG.debug("'F:" + getFlagName(flagBitNumber) +"' -> SET");
        register[Register.STATUS_FLAGS.index] = register[Register.STATUS_FLAGS.index].withBit(flagBitNumber);
    }

    /**
     * Bitwise clear flag by OR-ing the int carrying flags to be cleared
     * then AND-ing with status flag register.
     *
     * Clear bit 1 (place value 2)
     *          0000 0010
     * NOT    > 1111 1101
     * AND(R) > .... ..0.
     *
     * @param flagBitNumber int with bits to clear, turned on
     */
    public void clearFlag(int flagBitNumber){
        LOG.debug("'F:" + getFlagName(flagBitNumber) + "' -> CLEARED");
        register[Register.STATUS_FLAGS.index] = register[Register.STATUS_FLAGS.index].withoutBit(flagBitNumber);
    }

    /**
     * @param value to set the register flags based on
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
        if (value == 0)
            setFlag(Z);
        else
            clearFlag(Z);
    }

    /**
     * Set negative flag if given argument is 0
     */
    public void setNegativeFlagFor(int value){
        if (isNegative(value))
            setFlag(N);
        else
            clearFlag(N);
    }

    private boolean isNegative(int fakeByte){
        return RoxByte.fromLiteral(fakeByte).isNegative();
//        return (fakeByte & STATUS_FLAG_NEGATIVE) == STATUS_FLAG_NEGATIVE;  ///What was this about?
    }
}
