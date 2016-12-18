package com.rox.emu.P6502;

/**
 * @author rossdrew
 */
public class CPU {
    public static final int OP_ADC_I = 0x69;  //ADC Immediate
    public static final int OP_LDA_I = 0xA9;  //LDA Immediate
    public static final int OP_AND_I = 0x29;  //AND Immediate
    public static final int OP_OR_I = 0x09;   //OR Immediate
    public static final int OP_EOR_I = 0x49;  //EOR Immediate
    public static final int OP_SBC_I = 0xE9;  //SBX Immediate
    public static final int OP_SEC = 0x38;    //SEC (Implied)

    private int[] memory;

    Registers registers = new Registers();

    public CPU(int[] memory) {
        this.memory = memory;
    }

    /**
     * IRL this takes 6 CPU cycles but we'll cross that bridge IF we come to it
     */
    public void reset(){
        System.out.println("Resetting...");
        registers.setRegister(registers.REG_STATUS, 0x34);
        registers.setRegister(registers.REG_PC_HIGH, memory[0xFFFC]);
        registers.setRegister(registers.REG_PC_LOW, memory[0xFFFD]);
        registers.setRegister(registers.REG_SP, 0xFF);
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

        int accumulatorBeforeOperation = registers.getRegister(registers.REG_ACCUMULATOR);

        boolean carryManuallyChanged = false;

        //Execute the opcode
        switch (opCode){
            case OP_SEC:
                System.out.println("Instruction: Implied SEC...");
                registers.setFlag(registers.STATUS_FLAG_CARRY);
                carryManuallyChanged = true;
                break;

            case OP_LDA_I:
                System.out.println("Instruction: Immediate LDA...");
                memoryLocation = getAndStepPC(false);
                registers.setRegister(registers.REG_ACCUMULATOR, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_ADC_I:
                System.out.println("Instruction: Immediate ADC...");
                memoryLocation = getAndStepPC(false);
                int newTerm = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(registers.REG_ACCUMULATOR, newTerm + accumulatorBeforeOperation);
                updateOverflowFlag(accumulatorBeforeOperation, newTerm);
                break;

            case OP_AND_I:
                System.out.println("Instruction: Immediate AND...");
                memoryLocation = getAndStepPC(false);
                int andedValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(registers.REG_ACCUMULATOR, andedValue & accumulatorBeforeOperation);
                break;

            case OP_OR_I:
                System.out.println("Instruction: Immediate OR...");
                memoryLocation = getAndStepPC(false);
                int orredValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(registers.REG_ACCUMULATOR, orredValue | accumulatorBeforeOperation);
                break;

            case OP_EOR_I:
                System.out.println("Instruction: Immediate EOR...");
                memoryLocation = getAndStepPC(false);
                int xorredValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(registers.REG_ACCUMULATOR, xorredValue ^ accumulatorBeforeOperation);
                break;

            case OP_SBC_I:
                System.out.println("Instruction: Immediate SBC...");
                memoryLocation = getAndStepPC(false);
                registers.setFlag(registers.STATUS_FLAG_NEGATIVE);
                int subtrahend = getByteOfMemoryAt(memoryLocation);
                //XXX Should be done with addition to be more athentic but neither seem to work
                int difference = accumulatorBeforeOperation-subtrahend;
                updateOverflowFlag(accumulatorBeforeOperation, difference);
                registers.setRegister(registers.REG_ACCUMULATOR, difference & 0xFF);
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
        if (isNegative(accumulatorBeforeAddition) && isNegative(newValue) && !isNegative(registers.getRegister(registers.REG_ACCUMULATOR)) ||
            !isNegative(accumulatorBeforeAddition) && !isNegative(newValue) && isNegative(registers.getRegister(registers.REG_ACCUMULATOR))){
            registers.setFlag(registers.STATUS_FLAG_OVERFLOW);
        }else{
            registers.clearFlag(registers.STATUS_FLAG_OVERFLOW);
        }
    }

    private boolean isNegative(int fakeByte){
        return (fakeByte & registers.STATUS_FLAG_NEGATIVE) == registers.STATUS_FLAG_NEGATIVE;
    }

    private void updateZeroFlag() {
        if (registers.getRegister(registers.REG_ACCUMULATOR) == 0)
            registers.setFlag(registers.STATUS_FLAG_ZERO);
        else
            registers.clearFlag(registers.STATUS_FLAG_ZERO);
    }

    private void updateNegativeFlag() {
        if ( isNegative(registers.getRegister(registers.REG_ACCUMULATOR)))
            registers.setFlag(registers.STATUS_FLAG_NEGATIVE);
        else
            registers.clearFlag(registers.STATUS_FLAG_NEGATIVE);
    }

    private void updateCarryFlag() {
        if ((registers.getRegister(registers.REG_ACCUMULATOR) & registers.CARRY_INDICATOR_BIT) == registers.CARRY_INDICATOR_BIT) {
            registers.setFlag(registers.STATUS_FLAG_CARRY);
            //registers[REG_ACCUMULATOR] = (~0x100) & registers[REG_ACCUMULATOR]; //TODO
            registers.setRegister(registers.REG_ACCUMULATOR, (~0x100) & registers.getRegister(registers.REG_ACCUMULATOR));

        }else
            registers.clearFlag(registers.STATUS_FLAG_CARRY);
    }

}
