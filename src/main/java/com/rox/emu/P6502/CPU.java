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
     * the Program Counter into a word so that:-
     *
     * PC[0] = high order byte
     * PC[1] = low order byte
     *
     * <em>Increments the Program Counter by 1</em>
     *
     * @return word made up of both bytes
     */
    private int nextProgramWord(){
        int byte1 = nextProgramByte();
        return (byte1 << 8) | nextProgramByte() ;
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

            case OP_ORA_I:
                registers.setAccumulatorAndFlags(nextProgramByte() | accumulatorBeforeOperation);
                break;

            case OP_EOR_I:
                registers.setAccumulatorAndFlags(nextProgramByte() ^ accumulatorBeforeOperation);
                break;

            case OP_ADC_Z:
                performADC(getByteOfMemoryAt(nextProgramByte()));
                break;

            case OP_ADC_I:
                performADC(nextProgramByte());
                break;

            case OP_ADC_A:
                performADC(getByteOfMemoryAt(nextProgramWord()));
                break;

            case OP_SBC_I:
                performSBC(nextProgramByte());
                break;

            case OP_STA_Z:
                memory.setByte(nextProgramByte(), registers.getRegister(REG_ACCUMULATOR));
                break;

            /* XXX
             * Do I get an item from the stack then increment so that it
             * points at nothing or increment then add so that it points
             * at the last item?
             * i.e. should SP point at the top item or the next slot
             */
            case OP_PHA:
                memory.setByte(registers.getRegister(REG_SP), registers.getRegister(REG_ACCUMULATOR));
                registers.setRegister(REG_SP, registers.getRegister(REG_SP) - 1);
                break;

            case OP_PLA:
                registers.setRegister(REG_SP, registers.getRegister(REG_SP) + 1);
                int stackItemAddress = registers.getRegister(REG_SP);
                registers.setAccumulatorAndFlags(getByteOfMemoryAt(stackItemAddress));
                break;

            default:
                throw new UnknownOpCodeException("Unknown 6502 OpCode:" + opCode + " encountered.", opCode);
        }
    }

    private void performADC(int byteTerm){
        int carry = (registers.getFlag(STATUS_FLAG_CARRY) ? 1 : 0);
        addToAccumulator(byteTerm + carry);
    }

    //(1) compliment of carry flag added (so subtracted) as well
    //(2) set carry if no borrow required (A >= M[v])
    private void performSBC(int byteTerm){
        registers.setFlag(STATUS_FLAG_NEGATIVE);
        int borrow = (registers.getFlag(STATUS_FLAG_CARRY) ? 0 : 1);
        addToAccumulator(twosComplimentOf(byteTerm + borrow));
    }

    private int twosComplimentOf(int byteValue){
        return ((~byteValue) + 1) & 0xFF;
    }

    /**
     * Perform a binary addition, setting Carry and Overflow flags as required.
     *
     * @param term term to add to the accumulator
     */
    private void addToAccumulator(int term){
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

        registers.setAccumulatorAndFlags(result & 0xFF);
    }
}
