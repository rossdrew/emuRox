package com.rox.emu.P6502;

import static com.rox.emu.P6502.InstructionSet.*;
import static com.rox.emu.P6502.Registers.*;

import com.rox.emu.Memory;
import com.rox.emu.UnknownOpCodeException;

/**
 * @author rossdrew
 */
public class CPU {
    private final Memory memory;

    private final Registers registers = new Registers();

    public CPU(Memory memory) {
        this.memory = memory;
    }

    /**
     * IRL this takes 6 CPU cycles but we'll cross that bridge IF we come to it
     */
    public void reset(){
        System.out.println("*** RESETTING >>>");
        registers.setRegister(REG_STATUS, 0x34);
        registers.setRegister(REG_PC_HIGH, memory.getByte(0xFFFC));
        registers.setRegister(REG_PC_LOW, memory.getByte(0xFFFD));
        registers.setRegister(REG_SP, 0xFF);
        System.out.println("...READY!");
    }

    /**
     * Get the value of the 16 bit Program Counter (PC) and increment
     */
    private int getAndStepPC(){
        final int originalPC = registers.getPC();
        registers.setPC(originalPC + 1);

        return originalPC;
    }

    private int getByteOfMemoryAt(int location, int index){
        final int memoryByte = memory.getByte(location + index);
        System.out.println("FETCH mem[" + location + (index != 0 ? "[" + index + "]" : "") +"] --> " + memoryByte);
        return memoryByte;
    }

    private int getByteOfMemoryXIndexedAt(int location){
        return getByteOfMemoryAt(location, registers.getRegister(REG_X_INDEX));
    }

    private int getByteOfMemoryYIndexedAt(int location){
        return getByteOfMemoryAt(location, registers.getRegister(REG_Y_INDEX));
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
        int memoryLocation = getAndStepPC();
        return getByteOfMemoryAt(memoryLocation);
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
        return lowOrderByte | (nextProgramByte() << 8);
    }

    public void step(int steps){
        for (int i=0; i<steps; i++)
            step();
    }

    public void step() {
        System.out.println("*** STEP >>>");

        int accumulatorBeforeOperation = registers.getRegister(REG_ACCUMULATOR);
        int opCode = nextProgramByte();

        //Execute the opcode
        System.out.println("Instruction: " + getName(opCode) + "...");
        switch (opCode){
            case OP_SEC:
                registers.setFlag(STATUS_FLAG_CARRY);
                break;

            case OP_CLC:
                registers.clearFlag(STATUS_FLAG_CARRY);
                break;

            case OP_CLV:
                registers.clearFlag(STATUS_FLAG_OVERFLOW);
                break;

            case OP_INX:
                registers.setXAndFlags(registers.getRegister(REG_X_INDEX) + 1);
                break;

            case OP_DEX:
                registers.setXAndFlags(registers.getRegister(REG_X_INDEX) - 1);
                break;

            case OP_INY:
                registers.setYAndFlags(registers.getRegister(REG_Y_INDEX) + 1);
                break;

            case OP_DEY:
                registers.setYAndFlags(registers.getRegister(REG_Y_INDEX) - 1);
                break;

            case OP_LDX_I:
                registers.setXAndFlags(nextProgramByte());
                break;

            case OP_LDY_I:
                registers.setYAndFlags(nextProgramByte());
                break;

            case OP_LDA_Z_IX:
                registers.setAccumulatorAndFlags(getByteOfMemoryXIndexedAt(nextProgramByte()));
                break;

            case OP_LDA_IY:
                registers.setAccumulatorAndFlags(getByteOfMemoryYIndexedAt(nextProgramWord()));
                break;

            case OP_LDA_IX:
                registers.setAccumulatorAndFlags(getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case OP_LDA_I:
                registers.setAccumulatorAndFlags(nextProgramByte());
                break;

            case OP_LDA_A:
                registers.setAccumulatorAndFlags(getByteOfMemoryAt(nextProgramWord()));
                break;

            case OP_LDA_Z:
                registers.setAccumulatorAndFlags(getByteOfMemoryAt(nextProgramByte()));
                break;

            case OP_AND_I:
                registers.setAccumulatorAndFlags(nextProgramByte() & accumulatorBeforeOperation);
                break;

            case OP_OR_I:
                registers.setAccumulatorAndFlags(nextProgramByte() | accumulatorBeforeOperation);
                break;

            case OP_EOR_I:
                registers.setAccumulatorAndFlags(nextProgramByte() ^ accumulatorBeforeOperation);
                break;

            case OP_ADC_I: {
                int carry = (registers.getFlag(STATUS_FLAG_CARRY) ? 1 : 0);
                executeADC(nextProgramByte() + carry);
                break;
            }

            case OP_ADC_Z: {
                int carry = (registers.getFlag(STATUS_FLAG_CARRY) ? 1 : 0);
                executeADC(getByteOfMemoryAt(nextProgramByte()) + carry);
                break;
            }

            //(1) compliment of carry flag added (so subtracted) as well
            //(2) set carry if no borrow required (A >= M[v])
            case OP_SBC_I:
                registers.setFlag(STATUS_FLAG_NEGATIVE);
                int borrow = (registers.getFlag(STATUS_FLAG_CARRY) ? 0 : 1);
                executeADC(twosComplimentOf(nextProgramByte() + borrow));
                break;

            case OP_STA_Z:
                memory.setByte(nextProgramByte(), registers.getRegister(REG_ACCUMULATOR));
                break;

            default:
                throw new UnknownOpCodeException("Unknown 6502 OpCode:" + opCode + " encountered.", opCode);
        }
    }

    private int twosComplimentOf(int byteValue){
        return ((~byteValue) + 1) & 0xFF;
    }

    /**
     * Perform a binary addition, setting Carry and Overflow flags as required.
     *
     * Note, for subtraction (SBC), the Negative status flag must be set first
     *
     *
     * @param term term to add to the accumulator
     */
    private void executeADC(int term){
        int result = registers.getRegister(REG_ACCUMULATOR) + term;

        //Set Carry, if bit 8 is set on new accumulator value, ignoring in 2s compliment addition (subtraction)
        if (!registers.getFlag(STATUS_FLAG_NEGATIVE)){
            if ((result & CARRY_INDICATOR_BIT) == CARRY_INDICATOR_BIT)
                registers.setFlag(STATUS_FLAG_CARRY);
        }else {
            registers.clearFlag(STATUS_FLAG_CARRY);
        }

        //Set Overflow if the sign of both inputs is different from the sign of the result
        if (((registers.getRegister(REG_ACCUMULATOR) ^ result) & (term ^ result) & 0x80) != 0)
            registers.setFlag(STATUS_FLAG_OVERFLOW);
        else
            registers.clearFlag(STATUS_FLAG_OVERFLOW);

        registers.setAccumulatorAndFlags(result & 0xFF);
    }
}
