package com.rox.emu.P6502;

import com.rox.emu.UnknownOpCodeException;

/**
 * @author rossdrew
 */
public class CPU {
    public static final int OP_ADC_I = 0x69;   //ADC Immediate
    public static final int OP_LDA_Z = 0xA5;   //LDA (Zero Page)
    public static final int OP_LDA_I = 0xA9;   //... Immediate
    public static final int OP_LDA_A = 0xAD;   //... Absolute
    public static final int OP_LDA_Z_IX = 0xB5;//... Zero Page indexed with X
    public static final int OP_LDA_IY = 0xB9;  //... Indexed with Y
    public static final int OP_LDA_IX = 0xBD;  //... Indexed with X
    public static final int OP_AND_I = 0x29;   //AND Immediate
    public static final int OP_OR_I = 0x09;    //OR Immediate
    public static final int OP_EOR_I = 0x49;   //EOR Immediate
    public static final int OP_SBC_I = 0xE9;   //SBX Immediate
    public static final int OP_SEC = 0x38;     //SEC (Implied)
    public static final int OP_LDY_I = 0xA0;   //LDX Immediate
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
        System.out.println("*** RESETTING >>>");
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

    private int nextProgramByte(){
        int memoryLocation = getAndStepPC(false);
        int newByte = getByteOfMemoryAt(memoryLocation);
        return newByte;
    }

    public void step() {
        System.out.println("*** STEP >>>");

        int accumulatorBeforeOperation = registers.getRegister(Registers.REG_ACCUMULATOR);
        int opCode = nextProgramByte();
        boolean carryManuallyChanged = false;
        int temporaryByte;

        //Execute the opcode
        switch (opCode) {
            case OP_SEC:
                System.out.println("Instruction: Implied SEC...");
                registers.setFlag(Registers.STATUS_FLAG_CARRY);
                carryManuallyChanged = true;
                break;

            case OP_LDX_I:
                System.out.println("Instruction: Immediate LDX...");
                registers.setRegister(Registers.REG_X_INDEX, nextProgramByte());
                break;

            case OP_LDY_I:
                System.out.println("Instruction: Immediate LDY...");
                registers.setRegister(Registers.REG_Y_INDEX, nextProgramByte());
                break;

            case OP_LDA_Z_IX:
                temporaryByte = nextProgramByte();
                int zIndex = registers.getRegister(Registers.REG_X_INDEX);
                System.out.println("Instruction: Zero Page LDA from [" + temporaryByte + "[" + zIndex + "]]...");
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(temporaryByte + zIndex));
                break;

            case OP_LDA_IY:
            case OP_LDA_IX: {
                int lowOrderByte = nextProgramByte();
                int pointerWord = lowOrderByte | (nextProgramByte() << 8);

                int index = registers.getRegister(opCode == OP_LDA_IX ? Registers.REG_X_INDEX : Registers.REG_Y_INDEX);
                System.out.println("Instruction: LDA from [" + pointerWord + "[" + index + "]]...");
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(pointerWord + index));
            }
            break;

            case OP_LDA_I:
                System.out.println("Instruction: Immediate LDA...");
                registers.setRegister(Registers.REG_ACCUMULATOR, nextProgramByte());
                break;

            case OP_LDA_A: {// [op] [low order byte] [high order byte]
                int lowOrderByte = nextProgramByte();
                int pointerWord = lowOrderByte | (nextProgramByte() << 8);
                System.out.println("Instruction: Absolute LDA from [" + pointerWord + "]...");
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(pointerWord));
            }
                break;

            case OP_LDA_Z:
                temporaryByte = nextProgramByte();
                System.out.println("Instruction: Zero Page LDA from " + temporaryByte + "...");
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(temporaryByte));
                break;

            case OP_ADC_I:
                System.out.println("Instruction: Immediate ADC...");
                int newTerm = nextProgramByte();
                registers.setRegister(Registers.REG_ACCUMULATOR, newTerm + accumulatorBeforeOperation);
                updateOverflowFlag(accumulatorBeforeOperation, newTerm);
                break;

            case OP_AND_I:
                System.out.println("Instruction: Immediate AND...");
                registers.setRegister(Registers.REG_ACCUMULATOR, nextProgramByte() & accumulatorBeforeOperation);
                break;

            case OP_OR_I:
                System.out.println("Instruction: Immediate OR...");
                registers.setRegister(Registers.REG_ACCUMULATOR, nextProgramByte() | accumulatorBeforeOperation);
                break;

            case OP_EOR_I:
                System.out.println("Instruction: Immediate EOR...");
                registers.setRegister(Registers.REG_ACCUMULATOR, nextProgramByte() ^ accumulatorBeforeOperation);
                break;

            case OP_SBC_I:
                System.out.println("Instruction: Immediate SBC...");
                registers.setFlag(Registers.STATUS_FLAG_NEGATIVE);
                int subtrahend = nextProgramByte();
                //XXX Should be done with addition to be more authentic but neither seem to work
                int difference = accumulatorBeforeOperation-subtrahend;
                updateOverflowFlag(accumulatorBeforeOperation, difference);
                registers.setRegister(Registers.REG_ACCUMULATOR, difference & 0xFF);
                break;

            default:
                System.out.println("ERROR: Unknown OPCODE: " + opCode);
                throw new UnknownOpCodeException("Unknown 6502 OpCode:" + opCode + " encountered.", opCode);
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
