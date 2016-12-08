package com.rox.emu;

/**
 * @author rossdrew
 */
public class P6502 {
    private final String[] registerNames = new String[] {"Accumulator", "", "", "Program Counter Hi", "Program Counter Low", "", "Stack Pointer", "Status Flags"};
    //private final String[] flagNames = new String[] {"Carry", "Zero Result", "IRQ Disable", "Decimal Mode", "Break Command", "<Unused>", "Overflow", "Negative Result"};

    public static final int ACC_REG = 0x0;
    public static final int PC_HI_REG = 0x3;
    public static final int PC_LO_REG = 0x4;
    public static final int SP_REG = 0x6;

    public static final int STATUS_FLAGS_REG = 0x7;
        public static final int STATUS_FLAG_CARRY = 0x1;
        public static final int STATUS_FLAG_ZERO = 0x2;
        public static final int STATUS_FLAG_IRQ_DISABLE = 0x4;
        public static final int STATUS_FLAG_DEC = 0x8;
        public static final int STATUS_FLAG_BREAK = 0x10;
        public static final int STATUS_FLAG_UNUSED = 0x20;
        public static final int STATUS_FLAG_OVERFLOW = 0x40;
            public static final int OVERFLOW_INDICATOR_BIT = 0x100;
        public static final int STATUS_FLAG_NEGATIVE = 0x80;

    public static final int OPCODE_ADC_I = 0x69;  //ADC Immediate
    public static final int OPCODE_LDA_I = 0xA9;  //LDA Immediate

    private int[] registers = new int[8];
    private int[] memory;

    public P6502(int[] memory) {
        this.memory = memory;
    }

    /**
     * IRL this takes 6 CPU cycles but we'll cross that bridge IF we come to it
     */
    public void reset(){
        System.out.println("Resetting...");
        setRegister(STATUS_FLAGS_REG, 0x34);    //= 00110100  XXX EnumSet might be better here
        setRegister(PC_HI_REG, memory[0xFFFC]);
        setRegister(PC_LO_REG, memory[0xFFFD]);
        setRegister(SP_REG, 0xFF);
        System.out.println("...READY!");
    }

    public int[] getRegisters() {
        return registers;
    }

    public int getRegister(int registerID){
        return registers[registerID];
    }

    public boolean[] getStatusFlags(){
        boolean[] flags = new boolean[8];
        flags[0] = (registers[STATUS_FLAGS_REG] & STATUS_FLAG_CARRY) == STATUS_FLAG_CARRY;
        flags[1] = (registers[STATUS_FLAGS_REG] & STATUS_FLAG_ZERO) == STATUS_FLAG_ZERO;
        flags[2] = (registers[STATUS_FLAGS_REG] & STATUS_FLAG_IRQ_DISABLE) == STATUS_FLAG_IRQ_DISABLE;
        flags[3] = (registers[STATUS_FLAGS_REG] & STATUS_FLAG_DEC) == STATUS_FLAG_DEC;
        flags[4] = (registers[STATUS_FLAGS_REG] & STATUS_FLAG_BREAK) == STATUS_FLAG_BREAK;
        flags[5] = (registers[STATUS_FLAGS_REG] & STATUS_FLAG_UNUSED) == STATUS_FLAG_UNUSED;
        flags[6] = (registers[STATUS_FLAGS_REG] & STATUS_FLAG_OVERFLOW) == STATUS_FLAG_OVERFLOW;
        flags[7] = (registers[STATUS_FLAGS_REG] & STATUS_FLAG_NEGATIVE) == STATUS_FLAG_NEGATIVE;
        return flags;
    }

    /**
     * Get the value of the 16 bit Program Counter (PC) and increment
     *
     * @param incrementFirst true = increment before returning, false = return then increment
     * @return the PC value before or after increment as per <i>incrementFirst</i>
     */
    private int getAndStepPC(boolean incrementFirst){
        final int originalPC = getPC();
        final int incrementedPC = originalPC + 1;
        setRegister(PC_HI_REG, incrementedPC & 0xFF00);
        setRegister(PC_LO_REG, incrementedPC & 0x00FF);
        System.out.println("Program Counter now " + incrementedPC);

        return incrementFirst ? incrementedPC : originalPC;
    }

    public int getPC(){
        return (getRegister(PC_HI_REG) << 8) | getRegister(PC_LO_REG);
    }

    private void setRegister(int registerID, int val){
        System.out.println("Setting (R)" + registerNames[registerID] + " to " + val);
        registers[registerID] = val;
    }

    private void setFlag(int flagID) {
        System.out.println("Setting (F)'" + flagID + "'");
        registers[STATUS_FLAGS_REG] = registers[STATUS_FLAGS_REG] | flagID;
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
    private void clearFlag(int flagValue){
        System.out.println("Clearing (F)'" + flagValue + "'");
        registers[STATUS_FLAGS_REG] = (~flagValue) & registers[STATUS_FLAGS_REG];
    }

    private int getByteOfMemoryAt(int location){
        final int memoryByte = memory[location];
        System.out.println("Got " + memoryByte + " from mem[" + location + "]");
        return memoryByte;
    }

    public void step() {
        System.out.println("*** Step ***");
        int memoryLocation = getAndStepPC(false);
        int opCode = getByteOfMemoryAt(memoryLocation);

        //Execute the opcode
        switch (opCode){
            case OPCODE_ADC_I:
                System.out.println("Instruction: Immediate ADC...");
                memoryLocation = getAndStepPC(false);
                setRegister(ACC_REG, getByteOfMemoryAt(memoryLocation) + getRegister(ACC_REG));
                break;

            case OPCODE_LDA_I:
                System.out.println("Instruction: Immediate LDA...");
                memoryLocation = getAndStepPC(false);
                setRegister(ACC_REG, getByteOfMemoryAt(memoryLocation));
                break;

            default:
                System.out.println("Unknown OPCODE: " + opCode);
        }

        updateZeroFlag();
        updateNegativeFlag();
        updateCarryFlag();
    }

    private void updateZeroFlag() {
        if (getRegister(ACC_REG) == 0)
            setFlag(STATUS_FLAG_ZERO);
        else
            clearFlag(STATUS_FLAG_ZERO);
    }

    private void updateNegativeFlag() {
        if ((getRegister(ACC_REG) & STATUS_FLAG_NEGATIVE) == STATUS_FLAG_NEGATIVE)
            setFlag(STATUS_FLAG_NEGATIVE);
        else
            clearFlag(STATUS_FLAG_NEGATIVE);
    }

    private void updateCarryFlag() {
        if ((getRegister(ACC_REG) & OVERFLOW_INDICATOR_BIT) == OVERFLOW_INDICATOR_BIT) {
            setFlag(STATUS_FLAG_CARRY);
            registers[ACC_REG] = (~0x100) & registers[ACC_REG];
        }else
            clearFlag(STATUS_FLAG_CARRY);
    }
}
