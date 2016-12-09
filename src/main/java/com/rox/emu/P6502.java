package com.rox.emu;

/**
 * @author rossdrew
 */
public class P6502 {
    private final String[] registerNames = new String[] {"Accumulator", "", "", "Program Counter Hi", "Program Counter Low", "", "Stack Pointer", "Status Flags"};
    //private final String[] flagNames = new String[] {"Carry", "Zero Result", "IRQ Disable", "Decimal Mode", "Break Command", "<Unused>", "Overflow", "Negative Result"};

    public static final int REG_ACCUMULATOR = 0;
    public static final int REG_PC_HIGH = 1;
    public static final int REG_PC_LOW = 2;
    public static final int REG_SP = 3;

    public static final int REG_STATUS = 4;
        public static final int STATUS_FLAG_CARRY = 0x1;
            public static final int CARRY_INDICATOR_BIT = 0x100;
        public static final int STATUS_FLAG_ZERO = 0x2;
        public static final int STATUS_FLAG_IRQ_DISABLE = 0x4;
        public static final int STATUS_FLAG_DEC = 0x8;
        public static final int STATUS_FLAG_BREAK = 0x10;
        public static final int STATUS_FLAG_UNUSED = 0x20;
        public static final int STATUS_FLAG_OVERFLOW = 0x40;
        public static final int STATUS_FLAG_NEGATIVE = 0x80;

    public static final int OP_ADC_I = 0x69;  //ADC Immediate
    public static final int OP_LDA_I = 0xA9;  //LDA Immediate
    public static final int OP_AND_I = 0x29;  //AND Immediate
    public static final int OP_OR_I = 0x09;  //OR Immediate

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
        setRegister(REG_STATUS, 0x34);    //= 00110100  XXX EnumSet might be better here
        setRegister(REG_PC_HIGH, memory[0xFFFC]);
        setRegister(REG_PC_LOW, memory[0xFFFD]);
        setRegister(REG_SP, 0xFF);
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
        flags[0] = (registers[REG_STATUS] & STATUS_FLAG_CARRY) == STATUS_FLAG_CARRY;
        flags[1] = (registers[REG_STATUS] & STATUS_FLAG_ZERO) == STATUS_FLAG_ZERO;
        flags[2] = (registers[REG_STATUS] & STATUS_FLAG_IRQ_DISABLE) == STATUS_FLAG_IRQ_DISABLE;
        flags[3] = (registers[REG_STATUS] & STATUS_FLAG_DEC) == STATUS_FLAG_DEC;
        flags[4] = (registers[REG_STATUS] & STATUS_FLAG_BREAK) == STATUS_FLAG_BREAK;
        flags[5] = (registers[REG_STATUS] & STATUS_FLAG_UNUSED) == STATUS_FLAG_UNUSED;
        flags[6] = (registers[REG_STATUS] & STATUS_FLAG_OVERFLOW) == STATUS_FLAG_OVERFLOW;
        flags[7] = (registers[REG_STATUS] & STATUS_FLAG_NEGATIVE) == STATUS_FLAG_NEGATIVE;
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
        setRegister(REG_PC_HIGH, incrementedPC & 0xFF00);
        setRegister(REG_PC_LOW, incrementedPC & 0x00FF);
        System.out.println("Program Counter now " + incrementedPC);

        return incrementFirst ? incrementedPC : originalPC;
    }

    public int getPC(){
        return (getRegister(REG_PC_HIGH) << 8) | getRegister(REG_PC_LOW);
    }

    private void setRegister(int registerID, int val){
        System.out.println("Setting (R)" + registerNames[registerID] + " to " + val);
        registers[registerID] = val;
    }

    private void setFlag(int flagID) {
        System.out.println("Setting (F)'" + flagID + "'");
        registers[REG_STATUS] = registers[REG_STATUS] | flagID;
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
        registers[REG_STATUS] = (~flagValue) & registers[REG_STATUS];
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

        int accumulatorBeforeOperation = getRegister(REG_ACCUMULATOR);

        //Execute the opcode
        switch (opCode){
            case OP_LDA_I:
                System.out.println("Instruction: Immediate LDA...");
                memoryLocation = getAndStepPC(false);
                setRegister(REG_ACCUMULATOR, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_ADC_I:
                System.out.println("Instruction: Immediate ADC...");
                memoryLocation = getAndStepPC(false);
                int newTerm = getByteOfMemoryAt(memoryLocation);
                setRegister(REG_ACCUMULATOR, newTerm + accumulatorBeforeOperation);
                updateOverflowFlag(accumulatorBeforeOperation, newTerm);
                break;

            case OP_AND_I:
                System.out.println("Instruction: Immediate AND...");
                memoryLocation = getAndStepPC(false);
                int andedValue = getByteOfMemoryAt(memoryLocation);
                setRegister(REG_ACCUMULATOR, andedValue & accumulatorBeforeOperation);
                break;

            case OP_OR_I:
                System.out.println("Instruction: Immediate OR...");
                memoryLocation = getAndStepPC(false);
                int orredValue = getByteOfMemoryAt(memoryLocation);
                setRegister(REG_ACCUMULATOR, orredValue | accumulatorBeforeOperation);
                break;


            default:
                System.out.println("ERROR: Unknown OPCODE: " + opCode);
        }

        updateZeroFlag();
        updateNegativeFlag();
        updateCarryFlag();
    }

    /**
     * XXX Seems like a lot of operations, any way to optimise this?
     */
    private void updateOverflowFlag(int accumulatorBeforeAddition, int newValue) {
        if (isNegative(accumulatorBeforeAddition) && isNegative(newValue) && !isNegative(getRegister(REG_ACCUMULATOR)) ||
            !isNegative(accumulatorBeforeAddition) && !isNegative(newValue) && isNegative(getRegister(REG_ACCUMULATOR))){
            setFlag(STATUS_FLAG_OVERFLOW);
        }else{
            clearFlag(STATUS_FLAG_OVERFLOW);
        }
    }

    private boolean isNegative(int fakeByte){
        return (fakeByte & STATUS_FLAG_NEGATIVE) == STATUS_FLAG_NEGATIVE;
    }

    private void updateZeroFlag() {
        if (getRegister(REG_ACCUMULATOR) == 0)
            setFlag(STATUS_FLAG_ZERO);
        else
            clearFlag(STATUS_FLAG_ZERO);
    }

    private void updateNegativeFlag() {
        if ( isNegative(getRegister(REG_ACCUMULATOR)))
            setFlag(STATUS_FLAG_NEGATIVE);
        else
            clearFlag(STATUS_FLAG_NEGATIVE);
    }

    private void updateCarryFlag() {
        if ((getRegister(REG_ACCUMULATOR) & CARRY_INDICATOR_BIT) == CARRY_INDICATOR_BIT) {
            setFlag(STATUS_FLAG_CARRY);
            registers[REG_ACCUMULATOR] = (~0x100) & registers[REG_ACCUMULATOR];
        }else
            clearFlag(STATUS_FLAG_CARRY);
    }

}
