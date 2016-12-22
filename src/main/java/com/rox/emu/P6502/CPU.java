package com.rox.emu.P6502;

import static com.rox.emu.P6502.InstructionSet.*;

/**
 * @author rossdrew
 */
public class CPU {
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
        System.out.println("Instruction: " + getName(opCode) + "...");
        switch (opCode){
            case OP_SEC:
                registers.setFlag(Registers.STATUS_FLAG_CARRY);
                carryManuallyChanged = true;
                break;

            case OP_LDX_I:
                memoryLocation = getAndStepPC(false);
                registers.setRegister(Registers.REG_X_INDEX, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_LDY_I:
                memoryLocation = getAndStepPC(false);
                registers.setRegister(Registers.REG_Y_INDEX, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_LDA_Z_IX:
                memoryLocation = getAndStepPC(false);
                memoryLocation = getByteOfMemoryAt(memoryLocation);
                int zIndex = registers.getRegister(Registers.REG_X_INDEX);
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(memoryLocation + zIndex));
                break;

            case OP_LDA_IY:
            case OP_LDA_IX:
                memoryLocation = getAndStepPC(false);
                int l = getByteOfMemoryAt(memoryLocation);
                memoryLocation = getAndStepPC(false);
                int mp = l | (getByteOfMemoryAt(memoryLocation) << 8);

                int index = registers.getRegister(opCode == OP_LDA_IX ? Registers.REG_X_INDEX : Registers.REG_Y_INDEX);
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(mp + index));
                break;

            case OP_LDA_I:
                memoryLocation = getAndStepPC(false);
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_LDA_A: // [op] [low order byte] [high order byte]
                memoryLocation = getAndStepPC(false);
                int lowByte = getByteOfMemoryAt(memoryLocation);
                memoryLocation = getAndStepPC(false);
                int pointerWord = lowByte | (getByteOfMemoryAt(memoryLocation) << 8);
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(pointerWord));
                break;

            case OP_LDA_Z:
                memoryLocation = getAndStepPC(false);
                memoryLocation = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(memoryLocation));
                break;

            case OP_ADC_I:
                memoryLocation = getAndStepPC(false);
                int newTerm = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, newTerm + accumulatorBeforeOperation);
                updateOverflowFlag(accumulatorBeforeOperation, newTerm);
                break;

            case OP_AND_I:
                memoryLocation = getAndStepPC(false);
                int andedValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, andedValue & accumulatorBeforeOperation);
                break;

            case OP_OR_I:
                memoryLocation = getAndStepPC(false);
                int orredValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, orredValue | accumulatorBeforeOperation);
                break;

            case OP_EOR_I:
                memoryLocation = getAndStepPC(false);
                int xorredValue = getByteOfMemoryAt(memoryLocation);
                registers.setRegister(Registers.REG_ACCUMULATOR, xorredValue ^ accumulatorBeforeOperation);
                break;

            case OP_SBC_I:
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
