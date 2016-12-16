package com.rox.emu.P6502;

/**
 * A representation of the 6502 CPU registers
 *
 * @author rossdrew
 */
public class Registers {
    public static final int REG_ACCUMULATOR = 0;
    public static final int REG_PC_HIGH = 1;
    public static final int REG_PC_LOW = 2;
    public static final int REG_SP = 3;
    public static final int REG_STATUS = 4;

    private final String[] registerNames = new String[] {"Accumulator", "", "", "Program Counter Hi", "Program Counter Low", "", "Stack Pointer", "Status Flags"};

    public static final int STATUS_FLAG_CARRY = 0x1;
    public static final int CARRY_INDICATOR_BIT = 0x100;
    public static final int STATUS_FLAG_ZERO = 0x2;
    public static final int STATUS_FLAG_IRQ_DISABLE = 0x4;
    public static final int STATUS_FLAG_DEC = 0x8;
    public static final int STATUS_FLAG_BREAK = 0x10;
    public static final int STATUS_FLAG_UNUSED = 0x20;
    public static final int STATUS_FLAG_OVERFLOW = 0x40;
    public static final int STATUS_FLAG_NEGATIVE = 0x80;

    private int register[] = new int[8];

    public void setRegister(int registerID, int val){
        int byteSizeValue = val & 0xFFFF00;
        System.out.println("Setting (R)" + register[registerID] + " to " + byteSizeValue);
        register[registerID] = byteSizeValue;
    }

    public void setPC(int wordPC){
        setRegister(REG_PC_HIGH, wordPC >> 8);
        setRegister(REG_PC_LOW,  wordPC & 0xFF);

        System.out.println("Program Counter being set to " + wordPC + " [ " + getRegister(REG_PC_HIGH) + " | " + getRegister(REG_PC_LOW) + " ]");
    }

    public int getPC(){
        return (getRegister(REG_PC_HIGH) << 8) | getRegister(REG_PC_LOW);
    }

    public String getRegisterName(int registerID){
        return registerNames[registerID];
    }

    public int getRegister(int registerID){
        return register[registerID];
    }

    public void setFlag(int flagID) {
        System.out.println("Setting (F)'" + flagID + "'");
        register[REG_STATUS] = register[REG_STATUS] | flagID;
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
     * @param flagValue int with bits to clear, turned on
     */
    public void clearFlag(int flagValue){
        System.out.println("Clearing (F)'" + flagValue + "'");
        register[REG_STATUS] = (~flagValue) & register[REG_STATUS];
    }

    public boolean[] getStatusFlags(){
        boolean[] flags = new boolean[8];
        flags[0] = (register[REG_STATUS] & STATUS_FLAG_CARRY) == STATUS_FLAG_CARRY;
        flags[1] = (register[REG_STATUS] & STATUS_FLAG_ZERO) == STATUS_FLAG_ZERO;
        flags[2] = (register[REG_STATUS] & STATUS_FLAG_IRQ_DISABLE) == STATUS_FLAG_IRQ_DISABLE;
        flags[3] = (register[REG_STATUS] & STATUS_FLAG_DEC) == STATUS_FLAG_DEC;
        flags[4] = (register[REG_STATUS] & STATUS_FLAG_BREAK) == STATUS_FLAG_BREAK;
        flags[5] = (register[REG_STATUS] & STATUS_FLAG_UNUSED) == STATUS_FLAG_UNUSED;
        flags[6] = (register[REG_STATUS] & STATUS_FLAG_OVERFLOW) == STATUS_FLAG_OVERFLOW;
        flags[7] = (register[REG_STATUS] & STATUS_FLAG_NEGATIVE) == STATUS_FLAG_NEGATIVE;
        return flags;
    }

    public int getNextProgramCounter(){
        final int originalPC = getPC();
        final int incrementedPC = originalPC + 1;

        setRegister(REG_PC_HIGH, incrementedPC & 0xFF00);
        setRegister(REG_PC_LOW, incrementedPC & 0x00FF);

        System.out.println("Program Counter now " + incrementedPC);

        return incrementedPC;
    }
}