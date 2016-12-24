package com.rox.emu.P6502;

/**
 * A representation of the 6502 CPU registers
 *
 * @author rossdrew
 */
public class Registers {
    public static final int REG_ACCUMULATOR = 0;
    public static final int REG_Y_INDEX = 1;
    public static final int REG_X_INDEX = 2;
    public static final int REG_PC_HIGH = 3;
    public static final int REG_PC_LOW = 4;
    private static final int REG_SP_X = 5;
    public static final int REG_SP = 6;
    public static final int REG_STATUS = 7;

    private static final String[] registerNames = new String[] {"Accumulator", "Y Index", "X Index", "Program Counter (Hi)", "Program Counter (Low)", "<SP>", "Stack Pointer", "Status Flags"};

    public static final int STATUS_FLAG_CARRY = 0x1;
    public static final int STATUS_FLAG_ZERO = 0x2;
    public static final int STATUS_FLAG_IRQ_DISABLE = 0x4;
    public static final int STATUS_FLAG_DEC = 0x8;
    public static final int STATUS_FLAG_BREAK = 0x10;
    private static final int STATUS_FLAG_UNUSED = 0x20; //Placeholder only
    public static final int STATUS_FLAG_OVERFLOW = 0x40;
    public static final int STATUS_FLAG_NEGATIVE = 0x80;

    public static final int CARRY_INDICATOR_BIT = 0x100; //The bit set on a word when a byte has overflown

    public static final int N = 7;
    public static final int V = 6;
    private static final int U = 5; //Placeholder only
    public static final int B = 4;
    public static final int D = 3;
    public static final int I = 2;
    public static final int Z = 1;
    public static final int C = 0;

    private static final String[] flagNames = new String[] {"Carry", "Zero", "IRQ Disable", "Decimal Mode", "BRK Command", "<UNUSED>", "Overflow", "Negative"};

    private final int[] register;

    public Registers(){
        register = new int[8];
        register[REG_SP_X]   = 0b11111111;
        register[REG_STATUS] = 0b00000000;
    }

    public static String getRegisterName(int registerID){
        return registerNames[registerID];
    }

    public void setRegister(int registerID, int val){
        System.out.println("'R:" + getRegisterName(registerID) + "' := " + val);
        register[registerID] = val;
    }

    public int getRegister(int registerID){
        return register[registerID];
    }

    public void setAccumulatorAndFlags(int value){
        setRegister(REG_ACCUMULATOR, value);
        updateZeroFlag(REG_ACCUMULATOR);
        updateNegativeFlag(REG_ACCUMULATOR);
    }

    public void setXAndFlags(int value){
        setRegister(REG_X_INDEX, value & 0xFF);
        updateZeroFlag(REG_X_INDEX);
        updateNegativeFlag(REG_X_INDEX);
    }

    public void setYAndFlags(int value){
        setRegister(REG_Y_INDEX, value & 0xFF);
        updateZeroFlag(REG_Y_INDEX);
        updateNegativeFlag(REG_Y_INDEX);
    }

    public void setPC(int wordPC){
        setRegister(REG_PC_HIGH, wordPC >> 8);
        setRegister(REG_PC_LOW, wordPC & 0xFF);
        System.out.println("'R+:Program Counter' := " + wordPC + " [ " + getRegister(REG_PC_HIGH) + " | " + getRegister(REG_PC_LOW) + " ]");
    }

    public int getPC(){
        return (getRegister(REG_PC_HIGH) << 8) | getRegister(REG_PC_LOW);
    }

    public int getNextProgramCounter(){
        final int originalPC = getPC();
        final int incrementedPC = originalPC + 1;
        setPC(incrementedPC);
        return incrementedPC;
    }

    private static int getFlagID(int flagValue) throws IllegalArgumentException {
        switch (flagValue){
            case STATUS_FLAG_CARRY: return C;
            case STATUS_FLAG_ZERO: return Z;
            case STATUS_FLAG_IRQ_DISABLE: return I;
            case STATUS_FLAG_DEC: return D;
            case STATUS_FLAG_BREAK: return B;
            case STATUS_FLAG_UNUSED: return U;
            case STATUS_FLAG_OVERFLOW: return V;
            case STATUS_FLAG_NEGATIVE: return N;
            default:
                throw new IllegalArgumentException("Unknown 6502 Flag ID:" + flagValue);
        }
    }

    public static String getFlagName(int flagValue){
        return flagNames[getFlagID(flagValue)];
    }

    public boolean getFlag(int flagPlaceValue){
        return ((register[REG_STATUS] & flagPlaceValue) == flagPlaceValue);
    }

    public void setFlag(int flagPlaceValue) {
        System.out.println("'F:" + getFlagName(flagPlaceValue) +"' -> SET");
        register[REG_STATUS] = register[REG_STATUS] | flagPlaceValue;
    }

    /**
     * Bitwise clear flag by OR-ing the int carrying flags to be cleared
     * then AND-ing with status flag register.
     *
     * Clear bit 1 (place value 2)
     *          0000 0010
     * NOT    > 1111 1101
     * AND(R) > xxxx xx0x
     *
     * @param flagPlaceValue int with bits to clear, turned on
     */
    public void clearFlag(int flagPlaceValue){
        System.out.println("'F:" + getFlagName(flagPlaceValue) + "' -> CLEARED");
        register[REG_STATUS] = (~flagPlaceValue) & register[REG_STATUS];
    }

    private void updateZeroFlag(int forRegisterID) {
        if (getRegister(forRegisterID) == 0)
            setFlag(STATUS_FLAG_ZERO);
        else
            clearFlag(STATUS_FLAG_ZERO);
    }

    private void updateNegativeFlag(int forRegisterID) {
        if ( isNegative(getRegister(forRegisterID)))
            setFlag(STATUS_FLAG_NEGATIVE);
        else
            clearFlag(STATUS_FLAG_NEGATIVE);
    }

    public boolean[] getStatusFlags(){
        boolean[] flags = new boolean[8];

        int status_flags = getRegister(REG_STATUS);
        for (int i=0, j=1; i<8; i++){
            flags[i] = (status_flags & j) == j;
            j*=2;
        }

        return flags;
    }

    private boolean isNegative(int fakeByte){
        return (fakeByte & STATUS_FLAG_NEGATIVE) == STATUS_FLAG_NEGATIVE;
    }
}
