package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.op.OpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.rox.emu.processor.mos6502.Registers.*;

/**
 * A emulated representation of MOS 6502, 8 bit
 * microprocessor functionality.
 *
 * XXX: At this point, we are only emulating the NES custom version of the 6502
 *
 * @author Ross Drew
 */
public class Mos6502 {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Memory memory;
    private final Registers registers = new Registers();
    private final Mos6502Alu alu = new Mos6502Alu(registers);

    public Mos6502(Memory memory) {
        this.memory = memory;
    }

    /**
     * Reset the CPU; akin to firing the Reset pin on a 6502.<br/>
     * <br/>
     * This will
     * <ul>
     *     <li>Set Accumulator &rarr; <code>0</code></li>
     *     <li>Set Indexes &rarr; <code>0</code></li>
     *     <li>Status register &rarr; <code>0x34</code></li>
     *     <li>Set PC to the values at <code>0xFFFC</code> and <code>0xFFFD</code></li>
     *     <li>Reset Stack Pointer &rarr; 0xFF</li>
     * </ul>
     * <br/>
     * Note: IRL this takes 6 CPU cycles but we'll cross that bridge IF we come to it-
     */
    public void reset(){
       log.debug("RESETTING...");
       setRegisterValue(Register.ACCUMULATOR, RoxByte.ZERO);
       setRegisterValue(Register.X_INDEX, RoxByte.ZERO);
       setRegisterValue(Register.Y_INDEX, RoxByte.ZERO);
       setRegisterValue(Register.STATUS_FLAGS, RoxByte.fromLiteral(0x34));
       setRegisterValue(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFC)));
       setRegisterValue(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFD)));
       setRegisterValue(Register.STACK_POINTER_HI, RoxByte.fromLiteral(0xFF));  //XXX Shouldmaybe be a max
       log.debug("...READY!");
    }

    /**
     * Fire an <b>I</b>nterrupt <b>R</b>e<b>Q</b>uest; akin to setting the IRQ pin on a 6502.<br/>
     * <br>
     * This will stash the PC and Status registers and set the Program Counter to the values at
     * <code>0xFFFE</code> and <code>0xFFFF</code> where the <b>I</b>nterrupt <b>S</b>ervice
     * <b>R</b>outine is expected to be
     */
    public void irq() {
        log.debug("IRQ!");
        registers.setFlag(Flag.IRQ_DISABLE);

        pushRegister(Register.PROGRAM_COUNTER_HI);
        pushRegister(Register.PROGRAM_COUNTER_LOW);
        pushRegister(Register.STATUS_FLAGS);

        setRegisterValue(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFe)));
        setRegisterValue(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFF)));
    }

    /**
     * Fire a <b>N</b>on <b>M</b>askable <b>I</b>nterrupt; akin to setting the NMI pin on a 6502.<br/>
     * <br>
     * This will stash the PC and Status registers and set the Program Counter to the values at <code>0xFFFA</code>
     * and <code>0xFFFB</code> where the <b>I</b>nterrupt <b>S</b>ervice <b>R</b>outine is expected to be
     */
    public void nmi() {
        log.debug("NMI!");
        registers.setFlag(Flag.IRQ_DISABLE);

        pushRegister(Register.PROGRAM_COUNTER_HI);
        pushRegister(Register.PROGRAM_COUNTER_LOW);
        pushRegister(Register.STATUS_FLAGS);

        setRegisterValue(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFA)));
        setRegisterValue(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFB)));
    }

    /**
     * @return the {@link Registers} being used
     */
    public Registers getRegisters(){
        return registers;
    }

    /**
     * Execute the next program instruction as per {@link Registers#getNextProgramCounter()}
     *
     * @param steps number of instructions to execute
     */
    public void step(int steps){
        for (int i=0; i<steps; i++)
            step();
    }

    /**
     * Execute the next program instruction as per {@link Registers#getNextProgramCounter()}
     */
    public void step() {
        log.debug("STEP >>>");

        final OpCode opCode = OpCode.from(nextProgramByte().getRawValue());

        //Execute the opcode
        log.debug("Instruction: {}...", opCode.getOpCodeName());
        switch (opCode){
            case ASL_A:
            case ASL_Z:
            case ASL_Z_IX:
            case ASL_ABS_IX:
            case ASL_ABS:
            case LSR_A:
            case LSR_Z:
            case LSR_Z_IX:
            case LSR_ABS:
            case LSR_ABS_IX:
            case ROL_A:
            case ROL_Z:
            case ROL_Z_IX:
            case ROL_ABS:
            case ROL_ABS_IX:
            case SEC:
            case CLC:
            case CLV:
                opCode.perform(alu, registers, memory);
            break;

            case ROR_A:
                withRegister(Register.ACCUMULATOR, this::performROR);
                break;

            case INC_Z:
                withByteAt(RoxWord.from(nextProgramByte()), this::performINC);
            break;

            case INC_Z_IX:
                withByteXIndexedAt(RoxWord.from(nextProgramByte()), this::performINC);
            break;

            case INC_ABS:
                withByteAt(nextProgramWord(), this::performINC);break;

            case INC_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performINC);
            break;

            case DEC_Z:
                withByteAt(RoxWord.from(nextProgramByte()), this::performDEC);
            break;

            case DEC_Z_IX:
                withByteXIndexedAt(RoxWord.from(nextProgramByte()), this::performDEC);
            break;

            case DEC_ABS:
                withByteAt(nextProgramWord(), this::performDEC);
            break;

            case DEC_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performDEC);
            break;

            case INX:
                withRegister(Register.X_INDEX, this::performINC);
                break;

            case DEX:
                withRegister(Register.X_INDEX, this::performDEC);
                break;

            case INY:
                withRegister(Register.Y_INDEX, this::performINC);
                break;

            case DEY:
                withRegister(Register.Y_INDEX, this::performDEC);
                break;

            case LDX_I:
                registers.setRegisterAndFlags(Register.X_INDEX, nextProgramByte());
                break;

            case LDX_Z:
                registers.setRegisterAndFlags(Register.X_INDEX, getByteOfMemoryAt(RoxWord.from(nextProgramByte())));
                break;

            case LDX_Z_IY:
                registers.setRegisterAndFlags(Register.X_INDEX, getByteOfMemoryYIndexedAt(RoxWord.from(nextProgramByte())));
                break;

            case LDX_ABS:
                registers.setRegisterAndFlags(Register.X_INDEX, getByteOfMemoryAt(nextProgramWord()));
                break;

            case LDX_ABS_IY:
                registers.setRegisterAndFlags(Register.X_INDEX, getByteOfMemoryYIndexedAt(nextProgramWord()));
                break;

            case LDY_I:
                registers.setRegisterAndFlags(Register.Y_INDEX, nextProgramByte());
                break;

            case LDY_Z:
                registers.setRegisterAndFlags(Register.Y_INDEX, getByteOfMemoryAt(RoxWord.from(nextProgramByte())));
                break;

            case LDY_Z_IX:
                registers.setRegisterAndFlags(Register.Y_INDEX, getByteOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())));
                break;

            case LDY_ABS:
                registers.setRegisterAndFlags(Register.Y_INDEX, getByteOfMemoryAt(nextProgramWord()));
                break;

            case LDY_ABS_IX:
                registers.setRegisterAndFlags(Register.Y_INDEX, getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case LDA_I:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, nextProgramByte());
                break;

            case LDA_Z:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryAt(RoxWord.from(nextProgramByte())));
                break;

            case LDA_Z_IX:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())));
                break;

            case LDA_ABS:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryAt(nextProgramWord()));
                break;

            case LDA_ABS_IY:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryYIndexedAt(nextProgramWord()));
                break;

            case LDA_ABS_IX:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case LDA_IND_IX:
                //XXX this needs to be wrappable for zero page addressing
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryAt(getWordOfMemoryXIndexedAt(RoxWord.from(nextProgramByte()))));
            break;

            case LDA_IND_IY:
                final RoxWord l = getIndirectYPointer();
                final RoxByte v = getByteOfMemoryAt(l);
                registers.setRegisterAndFlags(Register.ACCUMULATOR, v);
            break;

            case AND_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performAND);
                break;

            case AND_ABS:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case AND_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performAND);
                break;

            case AND_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performAND);
                break;

            case AND_ABS_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case AND_ABS_IY:
                withRegisterAndByteYIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case AND_IND_IX:
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())), this::performAND);
            break;

            case AND_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performAND);
            break;

            case BIT_Z:
                performBIT(getByteOfMemoryAt(RoxWord.from(nextProgramByte())));
            break;

            case BIT_ABS:
                performBIT(getByteOfMemoryAt(nextProgramWord()));
            break;

            case ORA_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performORA);
                break;

            case ORA_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performORA);
                break;

            case ORA_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performORA);
                break;

            case ORA_ABS:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramWord(), this::performORA);
                break;

            case ORA_ABS_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performORA);
                break;

            case ORA_ABS_IY:
                withRegisterAndByteYIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performORA);
                break;

            case ORA_IND_IX:
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())), this::performORA);
            break;

            case ORA_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performORA);
            break;

            case EOR_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performEOR);
                break;

            case EOR_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performEOR);
                break;

            case EOR_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performEOR);
                break;

            case EOR_ABS:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramWord(), this::performEOR);
                break;

            case EOR_ABS_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performEOR);
                break;

            case EOR_ABS_IY:
                withRegisterAndByteYIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performEOR);
                break;

            case EOR_IND_IX:
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())), this::performEOR);
            break;

            case EOR_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performEOR);
            break;

            case ADC_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performADC);
                break;

            case ADC_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performADC);
                break;

            case ADC_ABS:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramWord(), this::performADC);
                break;

            case ADC_ABS_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performADC);
                break;

            case ADC_ABS_IY:
                withRegisterAndByteYIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performADC);
                break;

            case ADC_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performADC);
                break;

            case ADC_IND_IX:
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())), this::performADC);
            break;

            case ADC_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performADC);
            break;

            case CMP_I:
                performCMP(nextProgramByte(), Register.ACCUMULATOR);
                break;

            case CMP_Z:
                performCMP(getByteOfMemoryAt(RoxWord.from(nextProgramByte())), Register.ACCUMULATOR);
                break;

            case CMP_Z_IX:
                performCMP(getByteOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())), Register.ACCUMULATOR);
                break;

            case CMP_ABS:
                performCMP(getByteOfMemoryAt(nextProgramWord()), Register.ACCUMULATOR);
                break;

            case CMP_ABS_IX:
                performCMP(getByteOfMemoryXIndexedAt(nextProgramWord()), Register.ACCUMULATOR);
                break;

            case CMP_ABS_IY:
                performCMP(getByteOfMemoryYIndexedAt(nextProgramWord()), Register.ACCUMULATOR);
                break;

            case CMP_IND_IX:
                performCMP(getByteOfMemoryAt(getWordOfMemoryXIndexedAt(RoxWord.from(nextProgramByte()))), Register.ACCUMULATOR);
            break;

            case CMP_IND_IY:
                performCMP(getByteOfMemoryAt(getIndirectYPointer()), Register.ACCUMULATOR);
            break;

            case CPX_I:
                performCMP(nextProgramByte(), Register.X_INDEX);
                break;

            case CPX_Z:
                performCMP(getByteOfMemoryAt(RoxWord.from(nextProgramByte())), Register.X_INDEX);
                break;

            case CPX_ABS:
                performCMP(getByteOfMemoryAt(nextProgramWord()), Register.X_INDEX);
                break;

            case CPY_I:
                performCMP(nextProgramByte(), Register.Y_INDEX);
                break;

            case CPY_Z:
                performCMP(getByteOfMemoryAt(RoxWord.from(nextProgramByte())), Register.Y_INDEX);
                break;

            case CPY_ABS:
                performCMP(getByteOfMemoryAt(nextProgramWord()), Register.Y_INDEX);
                break;

            case SBC_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performSBC);
                break;

            case SBC_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performSBC);
                break;

            case SBC_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, RoxWord.from(nextProgramByte()), this::performSBC);
                break;

            case SBC_ABS:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramWord(), this::performSBC);
                break;

            case SBC_ABS_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performSBC);
                break;

            case SBC_ABS_IY:
                withRegisterAndByteYIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performSBC);
                break;

            case SBC_IND_IX:
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())), this::performSBC);
            break;

            case SBC_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performSBC);
            break;

            case STY_Z:
                setByteOfMemoryAt(RoxWord.from(nextProgramByte()), getRegisterValue(Register.Y_INDEX));
                break;

            case STY_ABS:
                setByteOfMemoryAt(nextProgramWord(), getRegisterValue(Register.Y_INDEX));
                break;

            case STY_Z_IX:
                setByteOfMemoryXIndexedAt(RoxWord.from(nextProgramByte()), getRegisterValue(Register.Y_INDEX));
                break;

            case STA_Z:
                setByteOfMemoryAt(RoxWord.from(nextProgramByte()), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_ABS:
                setByteOfMemoryAt(nextProgramWord(), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_Z_IX:
                setByteOfMemoryXIndexedAt(RoxWord.from(nextProgramByte()), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_ABS_IX:
                setByteOfMemoryXIndexedAt(nextProgramWord(), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_ABS_IY:
                setByteOfMemoryYIndexedAt(nextProgramWord(), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_IND_IX:
                setByteOfMemoryAt(getWordOfMemoryXIndexedAt(RoxWord.from(nextProgramByte())), getRegisterValue(Register.ACCUMULATOR));
            break;

            case STA_IND_IY:
                setByteOfMemoryAt(getIndirectYPointer(), getRegisterValue(Register.ACCUMULATOR));
            break;

            case STX_Z:
                setByteOfMemoryAt(RoxWord.from(nextProgramByte()), getRegisterValue(Register.X_INDEX));
                break;

            case STX_Z_IY:
                setByteOfMemoryYIndexedAt(RoxWord.from(nextProgramByte()), getRegisterValue(Register.X_INDEX));
                break;

            case STX_ABS:
                setByteOfMemoryAt(nextProgramWord(), getRegisterValue(Register.X_INDEX));
                break;

            case PHA:
                pushRegister(Register.ACCUMULATOR);
                break;

            case PLA:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, pop());
                break;

            case PHP:
                pushRegister(Register.STATUS_FLAGS);
                break;

            case PLP:
                registers.setRegister(Register.STATUS_FLAGS, pop());
                break;

            case JMP_ABS:
                registers.setPC(nextProgramWord());
            break;

            case JMP_IND:
                registers.setPC(getWordOfMemoryAt(nextProgramWord()));
            break;

            case BCS:
                branchIf(registers.getFlag(Flag.CARRY));
                break;

            case BCC:
                branchIf(!registers.getFlag(Flag.CARRY));
                break;

            case BEQ:
                branchIf(registers.getFlag(Flag.ZERO));
                break;

            case BNE:
                branchIf(!registers.getFlag(Flag.ZERO));
                break;

            case BMI:
                branchIf(registers.getFlag(Flag.NEGATIVE));
                break;

            case JSR:
                RoxByte hi = nextProgramByte();
                RoxByte lo = nextProgramByte();
                pushRegister(Register.PROGRAM_COUNTER_HI);
                pushRegister(Register.PROGRAM_COUNTER_LOW);
                setRegisterValue(Register.PROGRAM_COUNTER_HI, hi);
                setRegisterValue(Register.PROGRAM_COUNTER_LOW, lo);
                break;

            case BPL:
                branchIf(!registers.getFlag(Flag.NEGATIVE));
                break;

            case BVS:
                branchIf(registers.getFlag(Flag.OVERFLOW));
                break;

            case BVC:
                branchIf(!registers.getFlag(Flag.OVERFLOW));
                break;

            case TAX:
                setRegisterValue(Register.X_INDEX, getRegisterValue(Register.ACCUMULATOR));
                break;

            case TAY:
                setRegisterValue(Register.Y_INDEX, getRegisterValue(Register.ACCUMULATOR));
                break;

            case TYA:
                setRegisterValue(Register.ACCUMULATOR, getRegisterValue(Register.Y_INDEX));
                break;

            case TXA:
                setRegisterValue(Register.ACCUMULATOR, getRegisterValue(Register.X_INDEX));
                break;

            case TXS:
                setRegisterValue(Register.STACK_POINTER_HI, getRegisterValue(Register.X_INDEX));
                break;

            case TSX:
                setRegisterValue(Register.X_INDEX, getRegisterValue(Register.STACK_POINTER_HI));
                registers.setFlagsBasedOn(getRegisterValue(Register.X_INDEX));
                break;

            case NOP:
                //Do nothing
                break;

            case SEI:
                registers.setFlag(Flag.IRQ_DISABLE);
                break;

            case CLI:
                registers.clearFlag(Flag.IRQ_DISABLE);
                break;

            case SED:
                registers.setFlag(Flag.DECIMAL_MODE);
                break;

            case CLD:
                registers.clearFlag(Flag.DECIMAL_MODE);
                break;

            case RTS:
                setRegisterValue(Register.PROGRAM_COUNTER_LOW, pop());
                setRegisterValue(Register.PROGRAM_COUNTER_HI, pop());
                break;

            case RTI:
                setRegisterValue(Register.STATUS_FLAGS, pop());
                setRegisterValue(Register.PROGRAM_COUNTER_LOW, pop());
                setRegisterValue(Register.PROGRAM_COUNTER_HI, pop());
                break;

            case BRK:
                //BRK is unlike an interrupt in that PC+2 is saved to the stack, this may not be the next instruction
                //    and a correction may be necessary.  Due to the assumed use of BRK to path existing programs where
                //    BRK replaces a 2-byte instruction.
            default:
                registers.setPC(RoxWord.fromLiteral(registers.getPC().getRawValue() + 2));

                push(registers.getRegister(Register.PROGRAM_COUNTER_HI));
                push(registers.getRegister(Register.PROGRAM_COUNTER_LOW));
                push(RoxByte.fromLiteral(registers.getRegister(Register.STATUS_FLAGS).getRawValue() | Flag.BREAK.getPlaceValue()));

                registers.setRegister(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFE)));
                registers.setRegister(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFF)));
                break;
        }
    }

    private RoxByte getRegisterValue(Register registerID){
        return registers.getRegister(registerID);
    }

    private void setRegisterValue(Register registerID, RoxByte value){
        registers.setRegister(registerID, value);
    }

    /**
     * Get the value of the 16 bit Program Counter (PC) and increment. Equivalent of <br/>
     * <br/>
     * <code>PC++</code>
     */
    private RoxWord getAndStepPC(){
       final RoxWord originalPC = registers.getPC();
       final RoxWord newPC = RoxWord.fromLiteral(originalPC.getRawValue() + 1);

       registers.setPC(newPC);

       return originalPC;
    }

    /**
     * Return the next byte from program memory, as defined
     * by the Program Counter.<br/>
     * <br/>
     * <em>Increments the Program Counter by 1</em>
     *
     * @return byte {@code from mem[ PC[0] ]}
     */
    private RoxByte nextProgramByte(){
       return getByteOfMemoryAt(getAndStepPC());
    }

    /**
     * Combine the next two bytes in program memory, as defined by
     * the Program Counter into a word so that:-
     *
     * PC[0] = high order byte
     * PC[1] = low order byte
     *<br/><br/>
     * <em>Increments the Program Counter by 1</em>
     *
     * @return word made up of both bytes
     */
    private RoxWord nextProgramWord(){
       return RoxWord.from(nextProgramByte(), nextProgramByte());
    }

    /**
     * Pop value from stack
     *
     * @return popped value
     */
    private RoxByte pop(){
       setRegisterValue(Register.STACK_POINTER_HI, RoxByte.fromLiteral(getRegisterValue(Register.STACK_POINTER_HI).getRawValue() + 1));
       RoxWord address = RoxWord.from(RoxByte.fromLiteral(0x01), getRegisterValue(Register.STACK_POINTER_HI));
       RoxByte value = getByteOfMemoryAt(address);
       debug("POP {}(0b{}) from mem[0x{}]", value.toString(),
                                                    Integer.toBinaryString(value.getRawValue()),
                                                    Integer.toHexString(address.getRawValue()).toUpperCase());
       return value;
    }

    private void debug(final String message, String ... args){
        if (log.isDebugEnabled())
            log.debug(message, args);
    }

    private void pushRegister(Register registerID){
        push(getRegisterValue(registerID));
    }

    /**
     * Push to stack
     *
     * @param value value to push
     */
    private void push(RoxByte value){
       debug("PUSH {}(0b{}) to mem[0x{}]",  value.toString(),
                                                    Integer.toBinaryString(value.getRawValue()),
                                                    Integer.toHexString(getRegisterValue(Register.STACK_POINTER_HI).getRawValue()).toUpperCase());

       setByteOfMemoryAt(RoxWord.from(RoxByte.fromLiteral(0x01), getRegisterValue(Register.STACK_POINTER_HI)), value);
       setRegisterValue(Register.STACK_POINTER_HI, RoxByte.fromLiteral(getRegisterValue(Register.STACK_POINTER_HI).getRawValue() - 1));
    }

    private RoxByte getByteOfMemoryXIndexedAt(RoxWord location){
       return getByteOfMemoryAt(location, getRegisterValue(Register.X_INDEX));
    }

    private RoxByte getByteOfMemoryYIndexedAt(RoxWord location){
        return getByteOfMemoryAt(location, getRegisterValue(Register.Y_INDEX));
    }

    private void setByteOfMemoryYIndexedAt(RoxWord location, RoxByte newByte){
       setByteOfMemoryAt(location, getRegisterValue(Register.Y_INDEX), newByte);
    }

    private RoxByte getByteOfMemoryAt(RoxWord location){
        return getByteOfMemoryAt(location, RoxByte.ZERO);
    }

    private RoxByte getByteOfMemoryAt(RoxWord location, RoxByte index){
       final RoxByte memoryByte = memory.getByte(RoxWord.fromLiteral(location.getRawValue() + index.getRawValue()));
       debug("Got 0x{} from mem[{}]", Integer.toHexString(memoryByte.getRawValue()), (location + (index != RoxByte.ZERO ? "[" + index + "]" : "")));
       return memoryByte;
    }

    private void setByteOfMemoryXIndexedAt(RoxWord location, RoxByte newByte){
       setByteOfMemoryAt(location, getRegisterValue(Register.X_INDEX), newByte);
    }

    private void setByteOfMemoryAt(RoxWord location, RoxByte newByte){
        setByteOfMemoryAt(location, RoxByte.ZERO, newByte);
    }

    private void setByteOfMemoryAt(RoxWord location, RoxByte index, RoxByte newByte){
       memory.setByteAt(RoxWord.fromLiteral(location.getRawValue() + index.getRawValue()), newByte);
       debug("Stored 0x{} at mem[{}]", Integer.toHexString(newByte.getRawValue()), (location + (index != RoxByte.ZERO ? "[" + index + "]" : "")));
    }

    private RoxWord getIndirectYPointer(){
        RoxByte loc = nextProgramByte();
        RoxWord pointer = getWordOfMemoryAt(RoxWord.from(loc));
        RoxByte off = getRegisterValue(Register.Y_INDEX);

        return RoxWord.fromLiteral(pointer.getRawValue() + off.getRawValue());
    }

    private RoxWord getWordOfMemoryXIndexedAt(RoxWord location){
       RoxWord indexedLocation = RoxWord.fromLiteral(location.getRawValue() + getRegisterValue(Register.X_INDEX).getRawValue());
       return getWordOfMemoryAt(indexedLocation);
    }

    private RoxWord getWordOfMemoryAt(RoxWord location) {
       RoxWord memoryWord = memory.getWord(location);
       debug("Got 0x{} from mem[{}]", Integer.toHexString(memoryWord.getRawValue()), location.toString());
       return memoryWord;
    }

    /**
     * Call {@link Mos6502#branchTo(RoxByte)} with next program byte
     *
     * @param condition if {@code true} then branch is followed
     */
    private void branchIf(boolean condition){
        RoxByte location = nextProgramByte();
        debug("Branch:0x{} by {} {}", Integer.toHexString(registers.getPC().getRawValue()), Integer.toBinaryString(location.getRawValue()), (condition ? "YES->" : "NO..."));
        if (condition) branchTo(location);
    }

    /**
     * Branch to a relative location as defined by a signed byte
     *
     * @param displacement relative (-127 &rarr; 128) location from end of branch instruction
     */
    private void branchTo(RoxByte displacement) {
        RoxByte addr;
        if (displacement.isNegative()) {
            addr = RoxByte.fromLiteral(getRegisterValue(Register.PROGRAM_COUNTER_LOW).getRawValue() - displacement.asOnesCompliment().getRawValue());
        }else {
            addr = RoxByte.fromLiteral(getRegisterValue(Register.PROGRAM_COUNTER_LOW).getRawValue() + displacement.getRawValue());
        }
        setRegisterValue(Register.PROGRAM_COUNTER_LOW, addr);
    }

    private int fromOnesComplimented(int byteValue){
        return (~byteValue) & 0xFF;
    }

    private int fromTwosComplimented(int byteValue){
        return fromOnesComplimented(byteValue) - 1;
    }

    private void performCMP(RoxByte value, Register toRegister){
        RoxByte result = performSilently(this::performSBC, getRegisterValue(toRegister), value, true);
        registers.setFlagsBasedOn(result);

        registers.setFlagTo(Flag.CARRY, (fromTwosComplimented(result.getRawValue()) >=0));
    }

    @FunctionalInterface
    private interface SingleByteOperation {
        RoxByte perform(RoxByte byteValue);
    }

    private void withByteAt(RoxWord location, SingleByteOperation singleByteOperation){
        RoxByte b = getByteOfMemoryAt(location);
        setByteOfMemoryAt(location, singleByteOperation.perform(b));
    }

    private void withByteXIndexedAt(RoxWord location, SingleByteOperation singleByteOperation){
        RoxByte b = getByteOfMemoryXIndexedAt(location);
        setByteOfMemoryXIndexedAt(location, singleByteOperation.perform(b));
    }

    private void withRegister(Register registerId, SingleByteOperation singleByteOperation){
        RoxByte b = getRegisterValue(registerId);
        setRegisterValue(registerId, singleByteOperation.perform(b));
    }

    private RoxByte performASL(RoxByte byteValue){
        RoxByte newValue = alu.asl(byteValue);
        registers.setFlagsBasedOn(newValue);
        return newValue;
    }

    private RoxByte performROL(RoxByte initialValue){
        RoxByte rotatedValue = alu.rol(initialValue);
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue;
    }

    private RoxByte performROR(RoxByte initialValue){
        RoxByte rotatedValue = alu.ror(initialValue);
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue;
    }

    private RoxByte performLSR(RoxByte initialValue){
        RoxByte rotatedValue = alu.lsr(initialValue);
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue;
    }

    private RoxByte performINC(RoxByte initialValue){
        RoxByte incrementedValue = performSilently(this::performADC, initialValue, RoxByte.fromLiteral(1),false);
        registers.setFlagsBasedOn(incrementedValue);
        return incrementedValue;
    }

    private RoxByte performDEC(RoxByte initialValue){
        RoxByte incrementedValue = performSilently(this::performSBC, initialValue, RoxByte.fromLiteral(1),true);
        registers.setFlagsBasedOn(incrementedValue);
        return incrementedValue;
    }

    private void performBIT(RoxByte memData) {
       int comparison = (memData.getRawValue() & getRegisterValue(Register.ACCUMULATOR).getRawValue());
       registers.setFlagTo(Flag.ZERO, comparison == memData.getRawValue());

       //Set N, V to bits 7 and 6 of memory data
       RoxByte val = RoxByte.fromLiteral((memData.getRawValue() & 0b11000000) | (getRegisterValue(Register.STATUS_FLAGS).getRawValue() & 0b00111111));
       setRegisterValue(Register.STATUS_FLAGS, val);
    }

    @FunctionalInterface
    private interface TwoByteOperation {
       RoxByte perform(RoxByte byteValueOne, RoxByte byteValueTwo);
    }

    /**
     * Perform byteA given operation and have the state of the registers be the same as before the operation was performed
     *
     * @param operation operation to address
     * @param byteA byte A to pass into the operation
     * @param byteB byte B to pass into the operation
     * @param carryInState the state in which to assume the carry flag is in at the start of the operation
     * @return the result of the operation.
     */
    private RoxByte performSilently(TwoByteOperation operation, RoxByte byteA, RoxByte byteB, boolean carryInState){
       RoxByte statusState = registers.getRegister(Register.STATUS_FLAGS);

       registers.setFlagTo(Flag.CARRY, carryInState);            //To allow ignore of the carry: ignore = 0 for ADC, 1 for SBC
       RoxByte result = operation.perform(byteA, byteB);

       registers.setRegister(Register.STATUS_FLAGS, statusState);
       return result;
    }

    private void withRegisterAndByteAt(Register registerId, RoxWord memoryLocation, TwoByteOperation twoByteOperation){
       withRegisterAndByte(registerId, getByteOfMemoryAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByteXIndexedAt(Register registerId, RoxWord memoryLocation, TwoByteOperation twoByteOperation){
       withRegisterAndByte(registerId, getByteOfMemoryXIndexedAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByteYIndexedAt(Register registerId, RoxWord memoryLocation, TwoByteOperation twoByteOperation){
       withRegisterAndByte(registerId, getByteOfMemoryYIndexedAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByte(Register registerId, RoxByte byteValue, TwoByteOperation twoByteOperation){
       RoxByte registerByte = getRegisterValue(registerId);

       registers.setRegisterAndFlags(registerId, twoByteOperation.perform(registerByte, byteValue));
    }

    private RoxByte performAND(RoxByte byteValueA, RoxByte byteValueB){
        return alu.and(byteValueA, byteValueB);
    }

    private RoxByte performEOR(RoxByte byteValueA, RoxByte byteValueB){
        return alu.xor(byteValueA, byteValueB);
    }

    private RoxByte performORA(RoxByte byteValueA, RoxByte byteValueB){
        return alu.or(byteValueA, byteValueB);
    }

    private RoxByte performADC(RoxByte byteValueA, RoxByte byteValueB){
       return alu.adc(byteValueA, byteValueB);
    }

    private RoxByte performSBC(RoxByte byteValueA, RoxByte byteValueB){
       return alu.sbc(byteValueA, byteValueB);
    }
}
