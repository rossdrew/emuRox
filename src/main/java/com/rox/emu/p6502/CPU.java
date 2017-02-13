package com.rox.emu.p6502;

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
        registers.setRegister(Registers.REG_ACCUMULATOR, 0x0);
        registers.setRegister(Registers.REG_X_INDEX, 0x0);
        registers.setRegister(Registers.REG_Y_INDEX, 0x0);
        registers.setRegister(Registers.REG_STATUS, 0x34);
        registers.setRegister(Registers.REG_PC_HIGH, memory.getByte(0xFFFC));
        registers.setRegister(Registers.REG_PC_LOW, memory.getByte(0xFFFD));
        registers.setRegister(Registers.REG_SP, 0xFF);
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

    private int setByteOfMemoryAt(int location, int index, int newByte){
        memory.setByteAt(location + index, newByte);
        System.out.println("STORE " + newByte + " --> mem[" + location + (index != 0 ? "[" + index + "]" : "") +"]");
        return (location + index);
    }

    private int getByteOfMemoryXIndexedAt(int location){
        return getByteOfMemoryAt(location, registers.getRegister(Registers.REG_X_INDEX));
    }

    private int setByteOfMemoryXIndexedAt(int location, int newByte){
        return setByteOfMemoryAt(location, registers.getRegister(Registers.REG_X_INDEX), newByte);
    }

    private int getByteOfMemoryYIndexedAt(int location){
        return getByteOfMemoryAt(location, registers.getRegister(Registers.REG_Y_INDEX));
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

        int accumulatorBeforeOperation = registers.getRegister(Registers.REG_ACCUMULATOR);
        int opCode = nextProgramByte();

        //Execute the opcode
        System.out.println("Instruction: " + InstructionSet.getOpCodeName(opCode) + "...");
        switch (opCode){
            case InstructionSet.OP_ASL_A: {
                int newFakeByte = registers.getRegister(Registers.REG_ACCUMULATOR) << 1;
                setCarryFlagBasedOn(newFakeByte);
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, newFakeByte);
            }
            break;

            case InstructionSet.OP_ASL_Z: {
                int location = nextProgramByte();
                int newFakeByte = memory.getByte(location) << 1;
                setCarryFlagBasedOn(newFakeByte);
                registers.setFlagsBasedOn(newFakeByte);
                memory.setByteAt(location, newFakeByte);
            }
            break;

            case InstructionSet.OP_ASL_Z_IX: {
                int location = nextProgramByte();
                int newFakeByte = getByteOfMemoryXIndexedAt(location) << 1;
                setCarryFlagBasedOn(newFakeByte);
                registers.setFlagsBasedOn(newFakeByte);
                setByteOfMemoryXIndexedAt(location, newFakeByte);
            }
            break;

            case InstructionSet.OP_ASL_ABS_IX: {
                int location = nextProgramWord();
                int newFakeByte = getByteOfMemoryXIndexedAt(location) << 1;
                setCarryFlagBasedOn(newFakeByte);
                registers.setFlagsBasedOn(newFakeByte);
                setByteOfMemoryXIndexedAt(location, newFakeByte);
            }
            break;

            case InstructionSet.OP_ASL_ABS: {
                int location = nextProgramWord();
                int newFakeByte = memory.getByte(location) << 1;
                setCarryFlagBasedOn(newFakeByte);
                registers.setFlagsBasedOn(newFakeByte);
                memory.setByteAt(location, newFakeByte);
            }
            break;

            case InstructionSet.OP_LSR_A: {
                int newFakeByte = registers.getRegister(Registers.REG_ACCUMULATOR);
                setBorrowFlagFor(newFakeByte);
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, newFakeByte >> 1);
            }
            break;

            case InstructionSet.OP_LSR_Z: {
                int location = nextProgramByte();
                int newFakeByte = memory.getByte(location);

                setBorrowFlagFor(newFakeByte);
                newFakeByte = newFakeByte >> 1;
                registers.setFlagsBasedOn(newFakeByte);
                memory.setByteAt(location, newFakeByte);
            }
            break;

            case InstructionSet.OP_LSR_Z_IX: {
                int location = nextProgramByte();
                int newFakeByte = getByteOfMemoryXIndexedAt(location);
                setBorrowFlagFor(newFakeByte);
                newFakeByte = newFakeByte >> 1;
                registers.setFlagsBasedOn(newFakeByte);
                setByteOfMemoryXIndexedAt(location, newFakeByte);
            }
            break;

            case InstructionSet.OP_LSR_ABS: {
                int location = nextProgramWord();
                int newFakeByte = memory.getByte(location);

                setBorrowFlagFor(newFakeByte);
                newFakeByte = newFakeByte >> 1;
                registers.setFlagsBasedOn(newFakeByte);
                memory.setByteAt(location, newFakeByte);
            }
            break;

            case InstructionSet.OP_LSR_ABS_IX: {
                int location = nextProgramWord();
                int newFakeByte = getByteOfMemoryXIndexedAt(location);

                setBorrowFlagFor(newFakeByte);
                newFakeByte = newFakeByte >> 1;
                registers.setFlagsBasedOn(newFakeByte);
                setByteOfMemoryXIndexedAt(location, newFakeByte);
            }
            break;

            case InstructionSet.OP_ROL_A:
                registers.setRegister(Registers.REG_ACCUMULATOR, performROL(registers.getRegister(Registers.REG_ACCUMULATOR)));
            break;

            case InstructionSet.OP_ROL_Z: {
                int location = nextProgramByte();
                memory.setByteAt(location, performROL(memory.getByte(location)));
            }break;

            case InstructionSet.OP_ROL_Z_IX: {
                int location = nextProgramByte();
                int result = performROL(getByteOfMemoryXIndexedAt(location));
                setByteOfMemoryXIndexedAt(location, result);
            }break;

            case InstructionSet.OP_ROL_ABS: {
                int location = nextProgramWord();
                memory.setByteAt(location, performROL(memory.getByte(location)));
            }break;

            case InstructionSet.OP_ROL_ABS_IX: {
                int location = nextProgramWord();
                int result = performROL(getByteOfMemoryXIndexedAt(location));
                setByteOfMemoryXIndexedAt(location, result);
            }break;

            /* Not implemented and/or not published on older 6502s */
            case InstructionSet.OP_ROR_A:
                registers.setRegister(Registers.REG_ACCUMULATOR, performROR(registers.getRegister(Registers.REG_ACCUMULATOR)));
                break;

            case InstructionSet.OP_SEC:
                registers.setFlag(Registers.STATUS_FLAG_CARRY);
                break;

            case InstructionSet.OP_CLC:
                registers.clearFlag(Registers.STATUS_FLAG_CARRY);
                break;

            case InstructionSet.OP_CLV:
                registers.clearFlag(Registers.STATUS_FLAG_OVERFLOW);
                break;

            case InstructionSet.OP_INC_Z: {
                int incrementLocation = nextProgramByte();
                int incrementedValue = (memory.getByte(incrementLocation) + 1) & 0xFF;
                registers.setFlagsBasedOn(incrementedValue);
                memory.setByteAt(incrementLocation, incrementedValue);
            }break;

            case InstructionSet.OP_INC_Z_IX: {
                int incrementLocation = nextProgramByte();
                int incrementedValue = (getByteOfMemoryXIndexedAt(incrementLocation) + 1) & 0xFF;
                registers.setFlagsBasedOn(incrementedValue);
                memory.setByteAt(incrementLocation, incrementedValue);
            }break;

            case InstructionSet.OP_INC_ABS: {
                int incrementLocation = nextProgramWord();
                int incrementedValue = (memory.getByte(incrementLocation) + 1) & 0xFF;
                registers.setFlagsBasedOn(incrementedValue);
                memory.setByteAt(incrementLocation, incrementedValue);
            }break;

            case InstructionSet.OP_INC_ABS_IX: {
                int incrementLocation = nextProgramWord();
                int incrementedValue = (getByteOfMemoryXIndexedAt(incrementLocation) + 1) & 0xFF;
                registers.setFlagsBasedOn(incrementedValue);
                setByteOfMemoryXIndexedAt(incrementLocation, incrementedValue);
            }break;

            case InstructionSet.OP_DEC_Z: {
                int decrementLocation = nextProgramByte();
                int decrementedValue = (memory.getByte(decrementLocation) - 1) & 0xFF;
                registers.setFlagsBasedOn(decrementedValue);
                memory.setByteAt(decrementLocation, decrementedValue);
            }break;

            case InstructionSet.OP_DEC_Z_IX: {
                int decrementLocation = nextProgramByte();
                int value = getByteOfMemoryXIndexedAt(decrementLocation);
                int decrementedValue = (value - 1) & 0xFF;
                System.out.println("#### [" + decrementLocation +"] " + value + " := " + decrementedValue);
                registers.setFlagsBasedOn(decrementedValue);

                setByteOfMemoryXIndexedAt(decrementLocation, decrementedValue);
            }break;

            case InstructionSet.OP_DEC_ABS: {
                int decrementLocation = nextProgramWord();
                int decrementedValue = (memory.getByte(decrementLocation) - 1) & 0xFF;
                registers.setFlagsBasedOn(decrementedValue);
                memory.setByteAt(decrementLocation, decrementedValue);
            }break;

            case InstructionSet.OP_INX:
                registers.incrementRegisterWithFlags(Registers.REG_X_INDEX);
                break;

            case InstructionSet.OP_DEX:
                registers.decrementRegisterWithFlags(Registers.REG_X_INDEX);
                break;

            case InstructionSet.OP_INY:
                registers.incrementRegisterWithFlags(Registers.REG_Y_INDEX);
                break;

            case InstructionSet.OP_DEY:
                registers.decrementRegisterWithFlags(Registers.REG_Y_INDEX);
                break;

            case InstructionSet.OP_LDX_I:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, nextProgramByte());
                break;

            case InstructionSet.OP_LDX_Z:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, getByteOfMemoryAt(nextProgramByte()));
                break;

            case InstructionSet.OP_LDX_Z_IY:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, getByteOfMemoryYIndexedAt(nextProgramByte()));
                break;

            case InstructionSet.OP_LDX_ABS:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, getByteOfMemoryAt(nextProgramWord()));
                break;

            case InstructionSet.OP_LDY_I:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, nextProgramByte());
                break;

            case InstructionSet.OP_LDY_Z:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, getByteOfMemoryAt(nextProgramByte()));
                break;

            case InstructionSet.OP_LDY_Z_IX:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, getByteOfMemoryXIndexedAt(nextProgramByte()));
                break;

            case InstructionSet.OP_LDY_ABS:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, getByteOfMemoryAt(nextProgramWord()));
                break;

            case InstructionSet.OP_LDY_ABS_IX:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case InstructionSet.OP_LDA_Z_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramByte()));
                break;

            case InstructionSet.OP_LDA_ABS_IY:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryYIndexedAt(nextProgramWord()));
                break;

            case InstructionSet.OP_LDA_ABS_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case InstructionSet.OP_LDA_I:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, nextProgramByte());
                break;

            case InstructionSet.OP_LDA_ABS:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramWord()));
                break;

            case InstructionSet.OP_LDA_Z:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramByte()));
                break;

            case InstructionSet.OP_AND_Z:
                performAND(getByteOfMemoryAt(nextProgramByte()));
                break;

            case InstructionSet.OP_AND_ABS:
                performAND(getByteOfMemoryAt(nextProgramWord()));
                break;

            case InstructionSet.OP_AND_I:
                performAND(nextProgramByte());
                break;

            case InstructionSet.OP_AND_Z_IX:
                performAND(getByteOfMemoryXIndexedAt(nextProgramByte()));
                break;

            case InstructionSet.OP_AND_ABS_IX:
                performAND(getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case InstructionSet.OP_BIT_Z: {
                performBIT(memory.getByte(nextProgramByte()));
            }break;

            case InstructionSet.OP_BIT_ABS: {
                performBIT(memory.getByte(nextProgramWord()));
            }break;

            case InstructionSet.OP_ORA_I:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, nextProgramByte() | accumulatorBeforeOperation);
                break;

            case InstructionSet.OP_ORA_Z:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramByte()) | accumulatorBeforeOperation);
                break;

            case InstructionSet.OP_ORA_Z_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramByte()) | accumulatorBeforeOperation);
                break;

            case InstructionSet.OP_ORA_ABS:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramWord()) | accumulatorBeforeOperation);
                break;

            case InstructionSet.OP_ORA_ABS_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramWord()) | accumulatorBeforeOperation);
                break;

            case InstructionSet.OP_EOR_I:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, nextProgramByte() ^ accumulatorBeforeOperation);
                break;

            case InstructionSet.OP_EOR_Z:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramByte()) ^ accumulatorBeforeOperation);
                break;

            case InstructionSet.OP_EOR_ABS:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramWord()) ^ accumulatorBeforeOperation);
                break;

            case InstructionSet.OP_ADC_Z:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performADC(getByteOfMemoryAt(nextProgramByte())));
                break;

            case InstructionSet.OP_ADC_I:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performADC(nextProgramByte()));
                break;

            case InstructionSet.OP_ADC_ABS:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performADC(getByteOfMemoryAt(nextProgramWord())));
                break;

            case InstructionSet.OP_ADC_ABS_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performADC(getByteOfMemoryXIndexedAt(nextProgramWord())));
                break;

            case InstructionSet.OP_ADC_ABS_IY:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performADC(getByteOfMemoryYIndexedAt(nextProgramWord())));
                break;

            case InstructionSet.OP_ADC_Z_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performADC(getByteOfMemoryXIndexedAt(nextProgramByte())));
                break;

            case InstructionSet.OP_CMP_I:
                performCMP(nextProgramByte(), Registers.REG_ACCUMULATOR);
                break;

            case InstructionSet.OP_CMP_Z:
                performCMP(getByteOfMemoryAt(nextProgramByte()), Registers.REG_ACCUMULATOR);
                break;

            case InstructionSet.OP_CPX_I:
                performCMP(nextProgramByte(), Registers.REG_X_INDEX);
            break;

            case InstructionSet.OP_CPY_I:
                performCMP(nextProgramByte(), Registers.REG_Y_INDEX);
            break;

            case InstructionSet.OP_SBC_I:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performSBC(nextProgramByte()));
                break;

            case InstructionSet.OP_SBC_Z:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performSBC(getByteOfMemoryAt(nextProgramByte())));
                break;

            case InstructionSet.OP_SBC_Z_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performSBC(getByteOfMemoryXIndexedAt(nextProgramByte())));
                break;

            case InstructionSet.OP_SBC_ABS:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, performSBC(getByteOfMemoryAt(nextProgramWord())));
                break;

            case InstructionSet.OP_STY_Z:
                memory.setByteAt(nextProgramByte(), registers.getRegister(Registers.REG_Y_INDEX));
                break;

            case InstructionSet.OP_STY_ABS:
                memory.setByteAt(nextProgramWord(), registers.getRegister(Registers.REG_Y_INDEX));
                break;

            case InstructionSet.OP_STY_Z_IX:
                setByteOfMemoryXIndexedAt(nextProgramByte(), registers.getRegister(Registers.REG_Y_INDEX));
                break;

            case InstructionSet.OP_STA_Z:
                memory.setByteAt(nextProgramByte(), registers.getRegister(Registers.REG_ACCUMULATOR));
                break;

            case InstructionSet.OP_STA_ABS:
                memory.setByteAt(nextProgramWord(), registers.getRegister(Registers.REG_ACCUMULATOR));
                break;

            case InstructionSet.OP_STA_Z_IX:
                setByteOfMemoryXIndexedAt(nextProgramByte(), registers.getRegister(Registers.REG_ACCUMULATOR));
                break;

            case InstructionSet.OP_STA_ABS_IX:
                setByteOfMemoryXIndexedAt(nextProgramWord(), registers.getRegister(Registers.REG_ACCUMULATOR));
                break;

            case InstructionSet.OP_STX_Z:
                memory.setByteAt(nextProgramByte(), registers.getRegister(Registers.REG_X_INDEX));
                break;

            case InstructionSet.OP_STX_ABS:
                memory.setByteAt(nextProgramWord(), registers.getRegister(Registers.REG_X_INDEX));
                break;

            case InstructionSet.OP_PHA:
                push(registers.getRegister(Registers.REG_ACCUMULATOR));
                break;

            case InstructionSet.OP_PLA:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, pop());
                break;

            case InstructionSet.OP_PHP:
                push(registers.getRegister(Registers.REG_STATUS));
                break;

            case InstructionSet.OP_PLP://XXX
                registers.setRegister(Registers.REG_STATUS, pop());
                break;

            case InstructionSet.OP_JMP_ABS:
                int h = nextProgramByte();
                int l = nextProgramByte();
                registers.setRegister(Registers.REG_PC_HIGH, h);
                registers.setRegister(Registers.REG_PC_LOW, l);
                break;

            case InstructionSet.OP_BCS:
                branchIf(registers.getFlag(Registers.STATUS_FLAG_CARRY));
                break;

            case InstructionSet.OP_BCC:
                branchIf(!registers.getFlag(Registers.STATUS_FLAG_CARRY));
                break;

            case InstructionSet.OP_BEQ:
                branchIf(registers.getFlag(Registers.STATUS_FLAG_ZERO));
                break;

            case InstructionSet.OP_BNE:
                branchIf(!registers.getFlag(Registers.STATUS_FLAG_ZERO));
                break;

            case InstructionSet.OP_BMI:
                branchIf(registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
                break;

            case InstructionSet.OP_JSR:
                int hi = nextProgramByte();
                int lo = nextProgramByte();
                push(registers.getRegister(Registers.REG_PC_HIGH));
                push(registers.getRegister(Registers.REG_PC_LOW));
                registers.setRegister(Registers.REG_PC_HIGH, hi);
                registers.setRegister(Registers.REG_PC_LOW, lo);
                break;

            case InstructionSet.OP_BPL:
                branchIf(!registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
                break;

            case InstructionSet.OP_BVS:
                branchIf(registers.getFlag(Registers.STATUS_FLAG_OVERFLOW));
                break;

            case InstructionSet.OP_BVC:
                branchIf(!registers.getFlag(Registers.STATUS_FLAG_OVERFLOW));
                break;

            case InstructionSet.OP_TAX:
                registers.setRegister(Registers.REG_X_INDEX, registers.getRegister(Registers.REG_ACCUMULATOR));
                break;

            case InstructionSet.OP_TAY:
                registers.setRegister(Registers.REG_Y_INDEX, registers.getRegister(Registers.REG_ACCUMULATOR));
                break;

            case InstructionSet.OP_TYA:
                registers.setRegister(Registers.REG_ACCUMULATOR, registers.getRegister(Registers.REG_Y_INDEX));
                break;

            case InstructionSet.OP_TXA:
                registers.setRegister(Registers.REG_ACCUMULATOR, registers.getRegister(Registers.REG_X_INDEX));
                break;

            case InstructionSet.OP_TXS:
                registers.setRegister(Registers.REG_SP, registers.getRegister(Registers.REG_X_INDEX));
                break;

            case InstructionSet.OP_TSX:
                registers.setRegister(Registers.REG_X_INDEX, registers.getRegister(Registers.REG_SP));
                registers.setFlagsBasedOn(registers.getRegister(Registers.REG_X_INDEX));
                break;

            case InstructionSet.OP_NOP:
                //Do nothing
                break;

            case InstructionSet.OP_SEI:
                registers.setFlag(Registers.STATUS_FLAG_IRQ_DISABLE);
                break;

            case InstructionSet.OP_CLI:
                registers.clearFlag(Registers.STATUS_FLAG_IRQ_DISABLE);
                break;

            case InstructionSet.OP_SED:
                registers.setFlag(Registers.STATUS_FLAG_DEC);
                break;

            case InstructionSet.OP_CLD:
                registers.clearFlag(Registers.STATUS_FLAG_DEC);
                break;

            default:
                throw new UnknownOpCodeException("Unknown 6502 OpCode:" + opCode + " encountered.", opCode);
        }
    }

    private int pop(){
        registers.setRegister(Registers.REG_SP, registers.getRegister(Registers.REG_SP) + 1);
        int address = 0x0100 | registers.getRegister(Registers.REG_SP);
        int value = getByteOfMemoryAt(address);
        System.out.println("POP " + value + "(" + Integer.toBinaryString(value) + ") from mem[" + address + "]");
        return value;
    }

    private void push(int value){
        System.out.println("PUSH " + value + "(" + Integer.toBinaryString(value) + ") to mem[" + registers.getRegister(Registers.REG_SP) + "]");
        memory.setByteAt(0x0100 | registers.getRegister(Registers.REG_SP), value);
        registers.setRegister(Registers.REG_SP, registers.getRegister(Registers.REG_SP) - 1);
    }

    private int performROL(int initialValue){
        int rotatedValue = (initialValue << 1) | (registers.getFlag(Registers.STATUS_FLAG_CARRY) ? 1 : 0);
        registers.setFlagsBasedOn(rotatedValue);
        setCarryFlagBasedOn(rotatedValue);
        return rotatedValue & 0xFF;
    }

    private int performROR(int initialValue){
        int rotatedValue = (initialValue >> 1) | (registers.getFlag(Registers.STATUS_FLAG_CARRY) ? 0b10000000 : 0);
        setBorrowFlagFor(initialValue);
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue & 0xFF;
    }

    private void performBIT(int memData) {
        if ((memData & registers.getRegister(Registers.REG_ACCUMULATOR)) == memData)
            registers.setFlag(Registers.STATUS_FLAG_ZERO);
        else
            registers.clearFlag(Registers.STATUS_FLAG_ZERO);

        //Set N, V to bits 7 and 6 of memory data
        registers.setRegister(Registers.REG_STATUS, (memData & 0b11000000) | (registers.getRegister(Registers.REG_STATUS) & 0b00111111));
    }

    //XXX Need to use 2s compliment addition (subtraction)
    private void performCMP(int value, int toRegister){
        int result = registers.getRegister(toRegister) - value;
        registers.setFlagsBasedOn(result & 0xFF);

        if (registers.getRegister(Registers.REG_ACCUMULATOR) >= value)
            registers.setFlag(Registers.STATUS_FLAG_CARRY);
        else
            registers.clearFlag(Registers.STATUS_FLAG_CARRY);
    }

    private void setBorrowFlagFor(int newFakeByte) {
        if ((newFakeByte & 0x1) == 0x1)
            registers.setFlag(Registers.STATUS_FLAG_CARRY);
        else
            registers.clearFlag(Registers.STATUS_FLAG_CARRY);
    }

    private void setCarryFlagBasedOn(int newFakeByte) {
        if ((newFakeByte & CARRY_INDICATOR_BIT) == CARRY_INDICATOR_BIT)
            registers.setFlag(Registers.STATUS_FLAG_CARRY);
        else
            registers.clearFlag(Registers.STATUS_FLAG_CARRY);
    }

    private void branchIf(boolean condition){
        int location = nextProgramByte();
        if (condition) branchTo(location);
    }

    /**
     * Branch to a relative location as defined by a signed byte
     *
     * @param displacement relative (-127 -> 128) location from end of branch instruction
     */
    private void branchTo(int displacement) {
        int displacementByte = displacement & 0xFF;
        if ((displacementByte & NEGATIVE_INDICATOR_BIT) == NEGATIVE_INDICATOR_BIT)
            registers.setRegister(Registers.REG_PC_LOW, registers.getRegister(Registers.REG_PC_LOW) - fromTwosComplimented(displacementByte));
        else
            registers.setRegister(Registers.REG_PC_LOW, registers.getRegister(Registers.REG_PC_LOW) + displacementByte);
    }

    private void performAND(int byteTerm){
        registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, byteTerm & registers.getRegister(Registers.REG_ACCUMULATOR));
    }

    private int performADC(int byteTerm){
        int carry = (registers.getFlag(Registers.STATUS_FLAG_CARRY) ? 1 : 0);
        return addToAccumulator(byteTerm + carry);
    }

    //(1) compliment of carry flag added (so subtracted) as well
    //(2) set carry if no borrow required (A >= M[v])
    private int performSBC(int byteTerm){
        registers.setFlag(Registers.STATUS_FLAG_NEGATIVE);
        int borrow = (registers.getFlag(Registers.STATUS_FLAG_CARRY) ? 0 : 1);
        return addToAccumulator(twosComplimentOf(byteTerm + borrow));
    }

    private int twosComplimentOf(int byteValue){
        return ((~byteValue) + 1) & 0xFF;
    }

    private int fromTwosComplimented(int byteValue){
        return ((~byteValue)) & 0xFF;
    }

    /**
     * Perform a binary addition, setting Carry and Overflow flags as required.
     *
     * @param term term to add to the accumulator
     */
    private int addToAccumulator(int term){
        int result = registers.getRegister(Registers.REG_ACCUMULATOR) + term;

        //Set Carry, if bit 8 is set on new accumulator value, ignoring in 2s compliment addition (subtraction)
        if (!registers.getFlag(Registers.STATUS_FLAG_NEGATIVE)){
            setCarryFlagBasedOn(result);
        }else {
            registers.clearFlag(Registers.STATUS_FLAG_CARRY);
        }

        //Set Overflow if the sign of both inputs is different from the sign of the result
        if (((registers.getRegister(Registers.REG_ACCUMULATOR) ^ result) & (term ^ result) & 0x80) != 0)
            registers.setFlag(Registers.STATUS_FLAG_OVERFLOW);

        return (result & 0xFF);
    }
}
