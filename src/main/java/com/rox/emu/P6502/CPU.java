package com.rox.emu.P6502;

import static com.rox.emu.P6502.InstructionSet.*;

import com.rox.emu.Memory;
import com.rox.emu.UnknownOpCodeException;

/**
 * @author rossdrew
 */
public class CPU {
    private Memory memory;

    private Registers registers = new Registers();

    public CPU(Memory memory) {
        this.memory = memory;
    }

    /**
     * IRL this takes 6 CPU cycles but we'll cross that bridge IF we come to it
     */
    public void reset(){
        System.out.println("*** RESETTING >>>");
        registers.setRegister(Registers.REG_STATUS, 0x34);
        registers.setRegister(Registers.REG_PC_HIGH, memory.getByte(0xFFFC));
        registers.setRegister(Registers.REG_PC_LOW, memory.getByte(0xFFFD));
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

    private int getByteOfMemoryAt(int location, int index){
        final int memoryByte = memory.getByte(location + index);
        System.out.println("FETCH mem[" + location + (index != 0 ? "[" + index + "]" : "") +"] --> " + memoryByte);
        return memoryByte;
    }

    private int getByteOfMemoryAt(int location){
        return getByteOfMemoryAt(location, 0);
    }

    public Registers getRegisters(){
        return registers;
    }

    /**
     * Return the next byte from program memory, as defined
     * by the Program Counter.
     * <em>Increments the Program Counter by 1</em>
     *
     * @return byte from PC[0]
     */
    private int nextProgramByte(){
        int memoryLocation = getAndStepPC(false);
        int newByte = getByteOfMemoryAt(memoryLocation);
        return newByte;
    }

    /**
     * Combine the next two bytes in program memory, as defined by
     * the Program Counter into a word so that
     *
     * PC[0] = low order byte
     * PC[1] = high order byte
     *
     * <em>Increments the Program Counter by 1</em>
     *
     * @return word made up of both bytes
     */
    private int nextProgramWord(){
        int lowOrderByte = nextProgramByte();
        int newWord = lowOrderByte | (nextProgramByte() << 8);
        return newWord;
    }

    public void step(int steps){
        for (int i=0; i<steps; i++)
            step();
    }

    public void step() {
        System.out.println("*** STEP >>>");

        int accumulatorBeforeOperation = registers.getRegister(Registers.REG_ACCUMULATOR);
        int opCode = nextProgramByte();

        //Execute the opcode
        System.out.println("Instruction: " + getName(opCode) + "...");
        switch (opCode){
            case OP_SEC:
                registers.setFlag(Registers.STATUS_FLAG_CARRY);
                break;

            case OP_CLC:
                System.out.println("Instruction: Implied CLC...");
                registers.clearFlag(Registers.STATUS_FLAG_CARRY);
                break;

            case OP_LDX_I:
                registers.setRegister(Registers.REG_X_INDEX, nextProgramByte());
                break;

            case OP_LDY_I:
                registers.setRegister(Registers.REG_Y_INDEX, nextProgramByte());
                break;

            case OP_LDA_Z_IX: {
                int index = registers.getRegister(Registers.REG_X_INDEX);
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramByte(), index));
            }
                break;

            case OP_LDA_IY:
            case OP_LDA_IX: {
                int index = registers.getRegister(opCode == OP_LDA_IX ? Registers.REG_X_INDEX : Registers.REG_Y_INDEX);
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramWord(), index));
            }
            break;

            case OP_LDA_I:
                registers.setRegister(Registers.REG_ACCUMULATOR, nextProgramByte());
                break;

            case OP_LDA_A: {
                int pointerWord = nextProgramWord();
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(pointerWord));
            }
                break;

            case OP_LDA_Z:
                registers.setRegister(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramByte()));
                break;

            case OP_ADC_I:
                executeADC(nextProgramByte());
                break;

            case OP_AND_I:
                registers.setRegister(Registers.REG_ACCUMULATOR, nextProgramByte() & accumulatorBeforeOperation);
                break;

            case OP_OR_I:
                registers.setRegister(Registers.REG_ACCUMULATOR, nextProgramByte() | accumulatorBeforeOperation);
                break;

            case OP_EOR_I:
                registers.setRegister(Registers.REG_ACCUMULATOR, nextProgramByte() ^ accumulatorBeforeOperation);
                break;

            case OP_SBC_I:
                registers.setFlag(Registers.STATUS_FLAG_NEGATIVE);
                executeADC(twosComplimentOf(nextProgramByte()));
                break;

            case OP_STA_Z:
                memory.setByte(nextProgramByte(), registers.getRegister(Registers.REG_ACCUMULATOR));
                break;

            default:
                throw new UnknownOpCodeException("Unknown 6502 OpCode:" + opCode + " encountered.", opCode);
        }

        updateZeroFlag();
        updateNegativeFlag();
    }

    private final int twosComplimentOf(int byteValue){
        return ((~byteValue) + 1) & 0xFF;
    }

    /**
     * Perform a binary addition, setting Carry and Overflow flags as required.
     *
     * Note, for subtraction (SBC), the Negative status flag must be set first
     *
     * @param term term to add to the accumulator
     */
    private void executeADC(int term){
        int result = registers.getRegister(Registers.REG_ACCUMULATOR) + term;

        //Set Overflow if the sign of both inputs is different from the sign of the result
        if (((registers.getRegister(Registers.REG_ACCUMULATOR) ^ result) & (term ^ result) & 0x80) != 0)
            registers.setFlag(Registers.STATUS_FLAG_OVERFLOW);
        else
            registers.clearFlag(Registers.STATUS_FLAG_OVERFLOW);

        //Set Carry, if bit 8 is set, ignoring in 2s compliment addition (subtraction)
        if (!registers.getFlag(Registers.STATUS_FLAG_NEGATIVE) && ((result & 0x100) == 0x100))
            registers.setFlag(Registers.STATUS_FLAG_CARRY);
        else
            registers.clearFlag(Registers.STATUS_FLAG_CARRY);

        registers.setRegister(Registers.REG_ACCUMULATOR, result & 0xFF);
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
}
