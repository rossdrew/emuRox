package com.rox.emu.P6502;

import static com.rox.emu.P6502.InstructionSet.*;
import static com.rox.emu.P6502.Registers.*;

import com.rox.emu.Memory;
import com.rox.emu.UnknownOpCodeException;

/**
 * A emulated representation of MOS 6502, 8 bit
 * microprocessor functionality.
 *
 * @author Ross Drew
 */
public class CPU {
    private final Memory memory;
    private final Registers registers = new Registers();

    public static final int CARRY_INDICATOR_BIT = 0x100;    //The bit set on a word when a byte has carried up
    public static final int NEGATIVE_INDICATOR_BIT = 0x80;  //The bit set on a byte when a it is negative

    public CPU(Memory memory) {
        this.memory = memory;
    }

    /**
     * IRL this takes 6 CPU cycles but we'll cross that bridge IF we come to it-
     */
    public void reset(){
        System.out.println("*** RESETTING >>>");
        registers.setRegister(REG_ACCUMULATOR, 0x0);
        registers.setRegister(REG_X_INDEX, 0x0);
        registers.setRegister(REG_Y_INDEX, 0x0);
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
        System.out.println("\n*** STEP >>>");

        int accumulatorBeforeOperation = registers.getRegister(REG_ACCUMULATOR);
        int opCode = nextProgramByte();

        //Execute the opcode
        System.out.println("Instruction: " + getName(opCode) + "...");
        switch (opCode){
            case OP_ASL_A: {
                int newFakeByte = registers.getRegister(REG_ACCUMULATOR) << 1;
                setCarryFlagBasedOn(newFakeByte);

                registers.setRegisterAndFlags(REG_ACCUMULATOR, newFakeByte);
            }
            break;

            case OP_ASL_Z: {
                int location = nextProgramByte();
                int newFakeByte = memory.getByte(location) << 1;
                setCarryFlagBasedOn(newFakeByte);

                memory.setByte(location, newFakeByte & 0xFF);
                registers.setFlagsBasedOn(newFakeByte);
            }
            break;

            case OP_LSR_A: {
                int newFakeByte = registers.getRegister(REG_ACCUMULATOR);
                setBorrowFlagFor(newFakeByte);
                registers.setRegisterAndFlags(REG_ACCUMULATOR, newFakeByte >> 1);
            }
            break;

            case OP_LSR_Z: {
                int location = nextProgramByte();
                int newFakeByte = memory.getByte(location);

                setBorrowFlagFor(newFakeByte);
                newFakeByte = newFakeByte >> 1;
                memory.setByte(location, newFakeByte & 0xFF);
                registers.setFlagsBasedOn(newFakeByte);
            }
            break;

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
                registers.incrementRegisterWithFlags(REG_X_INDEX);
                break;

            case OP_DEX:
                registers.decrementRegisterWithFlags(REG_X_INDEX);
                break;

            case OP_INY:
                registers.incrementRegisterWithFlags(REG_Y_INDEX);
                break;

            case OP_DEY:
                registers.decrementRegisterWithFlags(REG_Y_INDEX);
                break;

            case OP_LDX_I:
                registers.setRegisterAndFlags(REG_X_INDEX, nextProgramByte());
                break;

            case OP_LDY_I:
                registers.setRegisterAndFlags(REG_Y_INDEX, nextProgramByte());
                break;

            case OP_LDA_Z_IX:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramByte()));
                break;

            case OP_LDA_IY:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, getByteOfMemoryYIndexedAt(nextProgramWord()));
                break;

            case OP_LDA_IX:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case OP_LDA_I:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, nextProgramByte());
                break;

            case OP_LDA_A:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramWord()));
                break;

            case OP_LDA_Z:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramByte()));
                break;

            case OP_AND_I:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, nextProgramByte() & accumulatorBeforeOperation);
                break;

            case OP_ORA_I:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, nextProgramByte() | accumulatorBeforeOperation);
                break;

            case OP_EOR_I:
                registers.setRegisterAndFlags(REG_ACCUMULATOR, nextProgramByte() ^ accumulatorBeforeOperation);
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

            case OP_STY_Z:
                memory.setByte(nextProgramByte(), registers.getRegister(REG_Y_INDEX));
                break;

            case OP_STA_Z:
                memory.setByte(nextProgramByte(), registers.getRegister(REG_ACCUMULATOR));
                break;

            case OP_STX_Z:
                memory.setByte(nextProgramByte(), registers.getRegister(REG_X_INDEX));
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
                registers.setRegisterAndFlags(REG_ACCUMULATOR, getByteOfMemoryAt(stackItemAddress));
                break;

            case OP_JMP_A:
                int h = nextProgramByte();
                int l = nextProgramByte();
                registers.setRegister(REG_PC_HIGH, h);
                registers.setRegister(REG_PC_LOW, l);
                break;

            case OP_BCS:
                branchIf(registers.getFlag(STATUS_FLAG_CARRY));
                break;

            case OP_BCC:
                branchIf(!registers.getFlag(STATUS_FLAG_CARRY));
                break;

            case OP_BEQ:
                branchIf(registers.getFlag(STATUS_FLAG_ZERO));
                break;

            case OP_BNE:
                branchIf(!registers.getFlag(STATUS_FLAG_ZERO));
                break;

            case OP_BMI:
                branchIf(registers.getFlag(STATUS_FLAG_NEGATIVE));
                break;

            case OP_BPL:
                branchIf(!registers.getFlag(STATUS_FLAG_NEGATIVE));
                break;

            case OP_BVS:
                branchIf(registers.getFlag(STATUS_FLAG_OVERFLOW));
                break;

            case OP_BVC:
                branchIf(!registers.getFlag(STATUS_FLAG_OVERFLOW));
                break;

            case OP_ROL_A: {
                int rotatedValue = (registers.getRegister(REG_ACCUMULATOR) << 1) | (registers.getFlag(STATUS_FLAG_CARRY) ? 1 : 0);
                registers.setFlagsBasedOn(rotatedValue);
                setCarryFlagBasedOn(rotatedValue);
                registers.setRegister(REG_ACCUMULATOR, rotatedValue & 0xFF);
            }
                break;

            case OP_ROL_Z: {
                int location = nextProgramByte();
                int rotatedValue = (memory.getByte(location) << 1) | (registers.getFlag(STATUS_FLAG_CARRY) ? 1 : 0);
                setCarryFlagBasedOn(rotatedValue);
                registers.setFlagsBasedOn(rotatedValue);
                memory.setByte(location, rotatedValue & 0xFF);
            }
                break;

            case OP_NOP:
                //Do nothing
                break;

            default:
                throw new UnknownOpCodeException("Unknown 6502 OpCode:" + opCode + " encountered.", opCode);
        }
    }

    private void setBorrowFlagFor(int newFakeByte) {
        if ((newFakeByte & 0x1) == 0x1)
            registers.setFlag(STATUS_FLAG_CARRY);
    }

    private void setCarryFlagBasedOn(int newFakeByte) {
        if ((newFakeByte & CARRY_INDICATOR_BIT) == CARRY_INDICATOR_BIT)
            registers.setFlag(STATUS_FLAG_CARRY);
        else
            registers.clearFlag(STATUS_FLAG_CARRY);
    }

    private void branchIf(boolean condition){
        int location = nextProgramByte();
        if (condition) branchTo(location);
    }

    /**
     * Branch to a relative location as defined by a signed byte
     *
     * XXX This should be using twos-compliment numbers
     *
     * @param displacement relative (-127 -> 128) location from end of branch instruction
     */
    private void branchTo(int displacement) {
        displacement &= 0xFF;
        int absoluteDisplacement = displacement & 0b01111111;
        if ((displacement & NEGATIVE_INDICATOR_BIT) == NEGATIVE_INDICATOR_BIT)
            registers.setRegister(REG_PC_LOW, registers.getRegister(REG_PC_LOW) - absoluteDisplacement);
        else
            registers.setRegister(REG_PC_LOW, registers.getRegister(REG_PC_LOW) + absoluteDisplacement);
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
            setCarryFlagBasedOn(result);
        }else {
            registers.clearFlag(STATUS_FLAG_CARRY);
        }

        //Set Overflow if the sign of both inputs is different from the sign of the result
        if (((registers.getRegister(REG_ACCUMULATOR) ^ result) & (term ^ result) & 0x80) != 0)
            registers.setFlag(STATUS_FLAG_OVERFLOW);

        registers.setRegisterAndFlags(REG_ACCUMULATOR, result & 0xFF);
    }
}
