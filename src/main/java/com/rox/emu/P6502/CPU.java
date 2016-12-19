package com.rox.emu.P6502;

/**
 * @author rossdrew
 */
public class CPU {
    public static final int OP_ADC_I = 0x69;   //ADC Immediate
    public static final int OP_LDA_Z = 0xA5;   //LDA (Zero Page)
    public static final int OP_LDA_I = 0xA9;   //... Immediate
    public static final int OP_LDA_A = 0xAD;   //... Absolute
    public static final int OP_LDA_IND = 0xF6; //... Indirect using X with Zero Page
    public static final int OP_AND_I = 0x29;   //AND Immediate
    public static final int OP_OR_I = 0x09;    //OR Immediate
    public static final int OP_EOR_I = 0x49;   //EOR Immediate
    public static final int OP_SBC_I = 0xE9;   //SBX Immediate
    public static final int OP_SEC = 0x38;     //SEC (Implied)
    public static final int OP_LDX_I = 0xA2;   //LDX Immediate

    private int[] memory;

    private Registers registers = new Registers();

    public CPU(int[] memory) {
        this.memory = memory;
    }

    /**
     * IRL this takes 6 CPU cycles but we'll cross that bridge IF we come to it
     */
    public void reset(){
        System.out.println("Resetting...");
        registers.setRegister(Registers.REG_STATUS, 0x34);
        registers.setRegister(Registers.REG_PC_HIGH, memory[0xFFFC]);
        registers.setRegister(Registers.REG_PC_LOW, memory[0xFFFD]);
        registers.setRegister(Registers.REG_SP, 0xFF);
        System.out.println("...READY!");
    }

    /**
     * Get the value of the 16 bit Program Counter (PC) and increment
     *
     * @param incrementFirst true = increment before returning, false = return then increment
     * @return the PC value before or after increment as per <i>incrementFirst</i>
     */
    private int getAndStepPC(boolean incrementFirst){
        final int originalPC = registers.getPC();
        final int incrementedPC = originalPC + 1;
        registers.setPC(incrementedPC);

        return incrementFirst ? incrementedPC : originalPC;
    }

    //XXX I should really be loading this value into the accumulator, right?
    private int getByteOfMemoryAt(int location){
        final int memoryByte = memory[location];
        System.out.println("Got " + memoryByte + " from mem[" + location + "]");
        return memoryByte;
    }

    public Registers getRegisters(){
        return registers;
    }

    public void step() {
        System.out.println("*** Step ***");
        int memoryLocation = getAndStepPC(false);
        int opCode = getByteOfMemoryAt(memoryLocation);

        int accumulatorBeforeOperation = registers.getRegister(Registers.REG_ACCUMULATOR);

        boolean carryManuallyChanged = false;

        //Execute the opcode
        switch (opCode){
            case OP_SEC:
                System.out.println("Instruction: Implied SEC...");
                registers.setFlag(Registers.STATUS_FLAG_CARRY);
                carryManuallyChanged = true;
                break;

            case OP_LDX_I:
                System.out.println("Instruction: Immediate LDX...");
                memoryLocation = getAndStepPC(false);
                registers.setRegister(Registers.REG_X_INDEX, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_LDA_IND:
                System.out.println("Instruction: Indirect LDA on Zero Page using X...");
                //TODO After LDX implemented
                break;

            case OP_LDA_I:
                System.out.println("Instruction: Immediate LDA...");
                memoryLocation = getAndStepPC(false);
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_LDA_A: // [op] [low order byte] [high order byte]
                memoryLocation = getAndStepPC(false);
                int lowByte = getByteOfMemoryAt(memoryLocation);
                memoryLocation = getAndStepPC(false);
                int pointerWord = lowByte | (getByteOfMemoryAt(memoryLocation) << 8);
                System.out.println("Instruction: Absolute LDA from " + pointerWord + "]...");
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(pointerWord));
                break;

            case OP_LDA_Z:
                memoryLocation = getAndStepPC(false);
                memoryLocation = getByteOfMemoryAt(memoryLocation);
                System.out.println("Instruction: Zero Page LDA from " + memoryLocation + "...");
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_ADC_I:
                System.out.println("Instruction: Immediate ADC...");
                memoryLocation = getAndStepPC(false);
                int newTerm = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, newTerm + accumulatorBeforeOperation);
                updateOverflowFlag(accumulatorBeforeOperation, newTerm);
                break;

            case OP_AND_I:
                System.out.println("Instruction: Immediate AND...");
                memoryLocation = getAndStepPC(false);
                int andedValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, andedValue & accumulatorBeforeOperation);
                break;

            case OP_OR_I:
                System.out.println("Instruction: Immediate OR...");
                memoryLocation = getAndStepPC(false);
                int orredValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, orredValue | accumulatorBeforeOperation);
                break;

            case OP_EOR_I:
                System.out.println("Instruction: Immediate EOR...");
                memoryLocation = getAndStepPC(false);
                int xorredValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, xorredValue ^ accumulatorBeforeOperation);
                break;

            case OP_SBC_I:
                System.out.println("Instruction: Immediate SBC...");
                memoryLocation = getAndStepPC(false);
                registers.setFlag(Registers.STATUS_FLAG_NEGATIVE);
                int subtrahend = getByteOfMemoryAt(memoryLocation);
                //XXX Should be done with addition to be more athentic but neither seem to work
                int difference = accumulatorBeforeOperation-subtrahend;
                updateOverflowFlag(accumulatorBeforeOperation, difference);
                registers.setRegister(Registers.REG_ACCUMULATOR, difference & 0xFF);
                break;

            default:
                System.out.println("ERROR: Unknown OPCODE: " + opCode);
        }

        updateZeroFlag();
        updateNegativeFlag();
        if (!carryManuallyChanged)
            updateCarryFlag();
    }

    /**
     * XXX Seems like a lot of operations, any way to optimise this?
     */
    private void updateOverflowFlag(int accumulatorBeforeAddition, int newValue) {
        if (isNegative(accumulatorBeforeAddition) && isNegative(newValue) && !isNegative(registers.getRegister(Registers.REG_ACCUMULATOR)) ||
            !isNegative(accumulatorBeforeAddition) && !isNegative(newValue) && isNegative(registers.getRegister(Registers.REG_ACCUMULATOR))){
            registers.setFlag(Registers.STATUS_FLAG_OVERFLOW);
        }else{
            registers.clearFlag(Registers.STATUS_FLAG_OVERFLOW);
        }
    }

    private boolean isNegative(int fakeByte){
        return (fakeByte & Registers.STATUS_FLAG_NEGATIVE) == Registers.STATUS_FLAG_NEGATIVE;
    }

    private void updateZeroFlag() {
        if (registers.getRegister(Registers.REG_ACCUMULATOR) == 0)
            registers.setFlag(Registers.STATUS_FLAG_ZERO);
        else
            registers.clearFlag(Registers.STATUS_FLAG_ZERO);
    }

    private void updateNegativeFlag() {
        if ( isNegative(registers.getRegister(Registers.REG_ACCUMULATOR)))
            registers.setFlag(Registers.STATUS_FLAG_NEGATIVE);
        else
            registers.clearFlag(Registers.STATUS_FLAG_NEGATIVE);
    }

    private void updateCarryFlag() {
        if ((registers.getRegister(Registers.REG_ACCUMULATOR) & Registers.CARRY_INDICATOR_BIT) == Registers.CARRY_INDICATOR_BIT) {
            registers.setFlag(Registers.STATUS_FLAG_CARRY);
            //registers[REG_ACCUMULATOR] = (~0x100) & registers[REG_ACCUMULATOR]; //TODO
            registers.setRegister(Registers.REG_ACCUMULATOR, (~0x100) & registers.getRegister(Registers.REG_ACCUMULATOR));

        }else
            registers.clearFlag(Registers.STATUS_FLAG_CARRY);
    }

}
