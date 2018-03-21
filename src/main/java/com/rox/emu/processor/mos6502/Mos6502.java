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
       setRegisterValue(Register.ACCUMULATOR, 0x0);
       setRegisterValue(Register.X_INDEX, 0x0);
       setRegisterValue(Register.Y_INDEX, 0x0);
       setRegisterValue(Register.STATUS_FLAGS, 0x34);
       setRegisterValue(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(0xFFFC));
       setRegisterValue(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(0xFFFD));
       setRegisterValue(Register.STACK_POINTER_HI, 0xFF);
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

        setRegisterValue(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(0xFFFe));
        setRegisterValue(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(0xFFFF));
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

        setRegisterValue(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(0xFFFA));
        setRegisterValue(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(0xFFFB));
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

        final OpCode opCode = OpCode.from(nextProgramByte());

        //Execute the opcode
        log.debug("Instruction: {}...", opCode.getOpCodeName());
        switch (opCode){
            case ASL_A:
                withRegister(Register.ACCUMULATOR, this::performASL);
            break;

            case ASL_Z:
                withByteAt(nextProgramByte(), this::performASL);
            break;

            case ASL_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performASL);
            break;

            case ASL_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performASL);
            break;

            case ASL_ABS:
                withByteAt(nextProgramWord(), this::performASL);
            break;

            case LSR_A:
                withRegister(Register.ACCUMULATOR, this::performLSR);
            break;

            case LSR_Z:
                withByteAt(nextProgramByte(), this::performLSR);
            break;

            case LSR_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performLSR);
            break;

            case LSR_ABS:
                withByteAt(nextProgramWord(), this::performLSR);
            break;

            case LSR_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performLSR);
            break;

            case ROL_A:
                withRegister(Register.ACCUMULATOR, this::performROL);
            break;

            case ROL_Z:
                withByteAt(nextProgramByte(), this::performROL);
            break;

            case ROL_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performROL);
             break;

            case ROL_ABS:
                withByteAt(nextProgramWord(), this::performROL);
                break;

            case ROL_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performROL);
             break;

            case ROR_A:
                withRegister(Register.ACCUMULATOR, this::performROR);
                break;

            case SEC:
                registers.setFlag(Flag.CARRY);
                break;

            case CLC:
                registers.clearFlag(Flag.CARRY);
                break;

            case CLV:
                registers.clearFlag(Flag.OVERFLOW);
                break;

            case INC_Z:
                withByteAt(nextProgramByte(), this::performINC);
            break;

            case INC_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performINC);
            break;

            case INC_ABS:
                withByteAt(nextProgramWord(), this::performINC);break;

            case INC_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performINC);
            break;

            case DEC_Z:
                withByteAt(nextProgramByte(), this::performDEC);
            break;

            case DEC_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performDEC);
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
                registers.setRegisterAndFlags(Register.X_INDEX, getByteOfMemoryAt(nextProgramByte()));
                break;

            case LDX_Z_IY:
                registers.setRegisterAndFlags(Register.X_INDEX, getByteOfMemoryYIndexedAt(nextProgramByte()));
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
                registers.setRegisterAndFlags(Register.Y_INDEX, getByteOfMemoryAt(nextProgramByte()));
                break;

            case LDY_Z_IX:
                registers.setRegisterAndFlags(Register.Y_INDEX, getByteOfMemoryXIndexedAt(nextProgramByte()));
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
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryAt(nextProgramByte()));
                break;

            case LDA_Z_IX:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramByte()));
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
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryAt(getWordOfMemoryXIndexedAt(nextProgramByte())));
            break;

            case LDA_IND_IY:
                registers.setRegisterAndFlags(Register.ACCUMULATOR, getByteOfMemoryAt(getIndirectYPointer()));
            break;

            case AND_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramByte(), this::performAND);
                break;

            case AND_ABS:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case AND_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performAND);
                break;

            case AND_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramByte(), this::performAND);
                break;

            case AND_ABS_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case AND_ABS_IY:
                withRegisterAndByteYIndexedAt(Register.ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case AND_IND_IX:
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(nextProgramByte()), this::performAND);
            break;

            case AND_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performAND);
            break;

            case BIT_Z:
                performBIT(getByteOfMemoryAt(nextProgramByte()));
            break;

            case BIT_ABS:
                performBIT(getByteOfMemoryAt(nextProgramWord()));
            break;

            case ORA_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performORA);
                break;

            case ORA_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramByte(), this::performORA);
                break;

            case ORA_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramByte(), this::performORA);
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
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(nextProgramByte()), this::performORA);
            break;

            case ORA_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performORA);
            break;

            case EOR_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performEOR);
                break;

            case EOR_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramByte(), this::performEOR);
                break;

            case EOR_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramByte(), this::performEOR);
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
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(nextProgramByte()), this::performEOR);
            break;

            case EOR_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performEOR);
            break;

            case ADC_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramByte(), this::performADC);
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
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramByte(), this::performADC);
                break;

            case ADC_IND_IX:
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(nextProgramByte()), this::performADC);
            break;

            case ADC_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performADC);
            break;

            case CMP_I:
                performCMP(nextProgramByte(), Register.ACCUMULATOR);
                break;

            case CMP_Z:
                performCMP(getByteOfMemoryAt(nextProgramByte()), Register.ACCUMULATOR);
                break;

            case CMP_Z_IX:
                performCMP(getByteOfMemoryXIndexedAt(nextProgramByte()), Register.ACCUMULATOR);
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
                performCMP(getByteOfMemoryAt(getWordOfMemoryXIndexedAt(nextProgramByte())), Register.ACCUMULATOR);
            break;

            case CMP_IND_IY:
                performCMP(getByteOfMemoryAt(getIndirectYPointer()), Register.ACCUMULATOR);
            break;

            case CPX_I:
                performCMP(nextProgramByte(), Register.X_INDEX);
                break;

            case CPX_Z:
                performCMP(getByteOfMemoryAt(nextProgramByte()), Register.X_INDEX);
                break;

            case CPX_ABS:
                performCMP(getByteOfMemoryAt(nextProgramWord()), Register.X_INDEX);
                break;

            case CPY_I:
                performCMP(nextProgramByte(), Register.Y_INDEX);
                break;

            case CPY_Z:
                performCMP(getByteOfMemoryAt(nextProgramByte()), Register.Y_INDEX);
                break;

            case CPY_ABS:
                performCMP(getByteOfMemoryAt(nextProgramWord()), Register.Y_INDEX);
                break;

            case SBC_I:
                withRegisterAndByte(Register.ACCUMULATOR, nextProgramByte(), this::performSBC);
                break;

            case SBC_Z:
                withRegisterAndByteAt(Register.ACCUMULATOR, nextProgramByte(), this::performSBC);
                break;

            case SBC_Z_IX:
                withRegisterAndByteXIndexedAt(Register.ACCUMULATOR, nextProgramByte(), this::performSBC);
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
                withRegisterAndByteAt(Register.ACCUMULATOR, getWordOfMemoryXIndexedAt(nextProgramByte()), this::performSBC);
            break;

            case SBC_IND_IY:
                withRegisterAndByteAt(Register.ACCUMULATOR, getIndirectYPointer(), this::performSBC);
            break;

            case STY_Z:
                setByteOfMemoryAt(nextProgramByte(), getRegisterValue(Register.Y_INDEX));
                break;

            case STY_ABS:
                setByteOfMemoryAt(nextProgramWord(), getRegisterValue(Register.Y_INDEX));
                break;

            case STY_Z_IX:
                setByteOfMemoryXIndexedAt(nextProgramByte(), getRegisterValue(Register.Y_INDEX));
                break;

            case STA_Z:
                setByteOfMemoryAt(nextProgramByte(), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_ABS:
                setByteOfMemoryAt(nextProgramWord(), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_Z_IX:
                setByteOfMemoryXIndexedAt(nextProgramByte(), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_ABS_IX:
                setByteOfMemoryXIndexedAt(nextProgramWord(), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_ABS_IY:
                setByteOfMemoryYIndexedAt(nextProgramWord(), getRegisterValue(Register.ACCUMULATOR));
                break;

            case STA_IND_IX:
                setByteOfMemoryAt(getWordOfMemoryXIndexedAt(nextProgramByte()), getRegisterValue(Register.ACCUMULATOR));
            break;

            case STA_IND_IY:
                setByteOfMemoryAt(getIndirectYPointer(), getRegisterValue(Register.ACCUMULATOR));
            break;

            case STX_Z:
                setByteOfMemoryAt(nextProgramByte(), getRegisterValue(Register.X_INDEX));
                break;

            case STX_Z_IY:
                setByteOfMemoryYIndexedAt(nextProgramByte(), getRegisterValue(Register.X_INDEX));
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
                final RoxWord absoluteAddress = RoxWord.literalFrom(nextProgramWord());
                setRegisterValue(Register.PROGRAM_COUNTER_HI, absoluteAddress.getHighByte().getAsInt());
                setRegisterValue(Register.PROGRAM_COUNTER_LOW, absoluteAddress.getLowByte().getAsInt());
            break;

            case JMP_IND:
                final RoxWord indirectAddress = RoxWord.literalFrom(nextProgramWord());
                //XXX Why does't this work by just using nextProgramWord
                registers.setPC(getWordOfMemoryAt(indirectAddress.getAsInt()));
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
                int hi = nextProgramByte();
                int lo = nextProgramByte();
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
            default:
                //XXX Why do we do this at all? A program of [BRK] will push 0x03 as the PC...why is that right?
                registers.setPC(performSilently(this::performADC, registers.getPC(), 2, false));
                push(registers.getRegister(Register.PROGRAM_COUNTER_HI));
                push(registers.getRegister(Register.PROGRAM_COUNTER_LOW));
                push(registers.getRegister(Register.STATUS_FLAGS) | Flag.BREAK.getPlaceValue());

                registers.setRegister(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(0xFFFE));
                registers.setRegister(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(0xFFFF));
                break;
        }
    }

    private int getRegisterValue(Register registerID){
        return registers.getRegister(registerID);
    }

    private void setRegisterValue(Register registerID, int value){
        registers.setRegister(registerID, value);
    }

    /**
     * Get the value of the 16 bit Program Counter (PC) and increment
     */
    private int getAndStepPC(){
       final int originalPC = registers.getPC();

       registers.setPC(originalPC + 1);

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
     *<br/><br/>
     * <em>Increments the Program Counter by 1</em>
     *
     * @return word made up of both bytes
     */
    private int nextProgramWord(){
       int byte1 = nextProgramByte();

       return RoxWord.from(RoxByte.fromLiteral(byte1),
                           RoxByte.fromLiteral(nextProgramByte())).getAsInt();
    }

    /**
     * Pop value from stack
     *
     * @return popped value
     */
    private int pop(){
       setRegisterValue(Register.STACK_POINTER_HI, getRegisterValue(Register.STACK_POINTER_HI) + 1);
       int address = 0x0100 | getRegisterValue(Register.STACK_POINTER_HI);
       int value = getByteOfMemoryAt(address);
       debug("POP {}(0b{}) from mem[0x{}]", Integer.toString(value), Integer.toBinaryString(value), Integer.toHexString(address).toUpperCase());
       return value;
    }

    private void debug(final String message, String ... args){
        if (!log.isDebugEnabled())
            return;

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
    private void push(int value){
       debug("PUSH {}(0b{}) to mem[0x{}]",  Integer.toString(value),
                                                    Integer.toBinaryString(value),
                                                    Integer.toHexString(getRegisterValue(Register.STACK_POINTER_HI)).toUpperCase());

       setByteOfMemoryAt(0x0100 | getRegisterValue(Register.STACK_POINTER_HI), value);
       setRegisterValue(Register.STACK_POINTER_HI, getRegisterValue(Register.STACK_POINTER_HI) - 1);
    }

    private int getByteOfMemoryXIndexedAt(int location){
       return getByteOfMemoryAt(location, getRegisterValue(Register.X_INDEX));
    }

    private int getByteOfMemoryYIndexedAt(int location){
       return getByteOfMemoryAt(location, getRegisterValue(Register.Y_INDEX));
    }

    private void setByteOfMemoryYIndexedAt(int location, int newByte){
       setByteOfMemoryAt(location, getRegisterValue(Register.Y_INDEX), newByte);
    }

    private int getByteOfMemoryAt(int location){
        return getByteOfMemoryAt(location, 0);
    }

    private int getByteOfMemoryAt(int location, int index){
       final int memoryByte = memory.getByte(location + index);
       debug("Got 0x{} from mem[{}]", Integer.toHexString(memoryByte), (location + (index != 0 ? "[" + index + "]" : "")));
       return memoryByte;
    }

    private void setByteOfMemoryXIndexedAt(int location, int newByte){
       setByteOfMemoryAt(location, getRegisterValue(Register.X_INDEX), newByte);
    }

    private void setByteOfMemoryAt(int location, int newByte){
        setByteOfMemoryAt(location, 0, newByte);
    }

    private void setByteOfMemoryAt(int location, int index, int newByte){
       memory.setByteAt(location + index, newByte);
       debug("Stored 0x{} at mem[{}]", Integer.toHexString(newByte), (location + (index != 0 ? "[" + index + "]" : "")));
    }

    private int getIndirectYPointer(){
        return (getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Register.Y_INDEX));
    }

    private int getWordOfMemoryXIndexedAt(int location){
       int indexedLocation = location + getRegisterValue(Register.X_INDEX);
       return getWordOfMemoryAt(indexedLocation);
    }

    private int getWordOfMemoryAt(int location) {
       int memoryWord = memory.getWord(location);
       debug("Got 0x{} from mem[{}]", Integer.toHexString(memoryWord), Integer.toString(location));
       return memoryWord;
    }

    /**
     * Call {@link Mos6502#branchTo(int)} with next program byte
     *
     * @param condition if {@code true} then branch is followed
     */
    private void branchIf(boolean condition){
        int location = nextProgramByte();
        debug("Branch:0x{} by {} {}", Integer.toHexString(registers.getPC()), Integer.toBinaryString(location), (condition ? "YES->" : "NO..."));
        if (condition) branchTo(location);
    }

    /**
     * Branch to a relative location as defined by a signed byte
     *
     * @param displacement relative (-127 &rarr; 128) location from end of branch instruction
     */
    private void branchTo(int displacement) {
        //possible improvement?
//        int v = performSilently(this::performADC, getRegisterValue(Register.PROGRAM_COUNTER_LOW), displacement, registers.getFlag(STATUS_FLAG_CARRY));
//        setRegisterValue(Register.PROGRAM_COUNTER_LOW, v);

        final RoxByte displacementByte = RoxByte.fromLiteral(displacement);
        if (displacementByte.isNegative())
            setRegisterValue(Register.PROGRAM_COUNTER_LOW, getRegisterValue(Register.PROGRAM_COUNTER_LOW) - displacementByte.asOnesCompliment().getRawValue());
        else
            setRegisterValue(Register.PROGRAM_COUNTER_LOW, getRegisterValue(Register.PROGRAM_COUNTER_LOW) + displacementByte.getRawValue());
    }

    private int fromOnesComplimented(int byteValue){
        return (~byteValue) & 0xFF;
    }

    private int fromTwosComplimented(int byteValue){
        return fromOnesComplimented(byteValue) - 1;
    }

    private void performCMP(int value, Register toRegister){
        int result = performSilently(this::performSBC, getRegisterValue(toRegister), value, true);
        registers.setFlagsBasedOn(result & 0xFF);

        registers.setFlagTo(Flag.CARRY, (fromTwosComplimented(result) >=0));
    }

    @FunctionalInterface
    private interface SingleByteOperation {
        int perform(int byteValue);
    }

    private void withByteAt(int location, SingleByteOperation singleByteOperation){
        int b = getByteOfMemoryAt(location);
        setByteOfMemoryAt(location, singleByteOperation.perform(b));
    }

    private void withByteXIndexedAt(int location, SingleByteOperation singleByteOperation){
        int b = getByteOfMemoryXIndexedAt(location);
        setByteOfMemoryXIndexedAt(location, singleByteOperation.perform(b));
    }

    private void withRegister(Register registerId, SingleByteOperation singleByteOperation){
        int b = getRegisterValue(registerId);
        setRegisterValue(registerId, singleByteOperation.perform(b));
    }

    private int performASL(int byteValue){
        int newValue = alu.asl(RoxByte.fromLiteral(byteValue)).getRawValue();
        registers.setFlagsBasedOn(newValue);
        return newValue;
    }

    private int performROL(int initialValue){
        int rotatedValue = alu.rol(RoxByte.fromLiteral(initialValue)).getRawValue();
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue;
    }

    private int performROR(int initialValue){
        int rotatedValue = alu.ror(RoxByte.fromLiteral(initialValue)).getRawValue();
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue;
    }

    private int performLSR(int initialValue){
        int rotatedValue = alu.lsr(RoxByte.fromLiteral(initialValue)).getRawValue();
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue;
    }

    private int performINC(int initialValue){
        int incrementedValue = performSilently(this::performADC, initialValue, 1,false);
        registers.setFlagsBasedOn(incrementedValue);
        return incrementedValue;
    }

    private int performDEC(int initialValue){
        int incrementedValue = performSilently(this::performSBC, initialValue, 1,true);
        registers.setFlagsBasedOn(incrementedValue);
        return incrementedValue;
    }

    private void performBIT(int memData) {
       registers.setFlagTo(Flag.ZERO, ((memData & getRegisterValue(Register.ACCUMULATOR)) == memData));

       //Set N, V to bits 7 and 6 of memory data
       setRegisterValue(Register.STATUS_FLAGS, (memData & 0b11000000) | (getRegisterValue(Register.STATUS_FLAGS) & 0b00111111));
    }

    @FunctionalInterface
    private interface TwoByteOperation {
       int perform(int byteValueOne, int byteValueTwo);
    }

    /**
     * Perform byteA given operation and have the state of the registers be the same as before the operation was performed
     *
     * @param operation operation to perform
     * @param byteA byte A to pass into the operation
     * @param byteB byte B to pass into the operation
     * @param carryInState the state in which to assume the carry flag is in at the start of the operation
     * @return the result of the operation.
     */
    private int performSilently(TwoByteOperation operation, int byteA, int byteB, boolean carryInState){
       int statusState = registers.getRegister(Register.STATUS_FLAGS);

       registers.setFlagTo(Flag.CARRY, carryInState);            //To allow ignore of the carry: ignore = 0 for ADC, 1 for SBC
       int result = operation.perform(byteA, byteB);

       registers.setRegister(Register.STATUS_FLAGS, statusState);
       return result;
    }

    private void withRegisterAndByteAt(Register registerId, int memoryLocation, TwoByteOperation twoByteOperation){
       withRegisterAndByte(registerId, getByteOfMemoryAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByteXIndexedAt(Register registerId, int memoryLocation, TwoByteOperation twoByteOperation){
       withRegisterAndByte(registerId, getByteOfMemoryXIndexedAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByteYIndexedAt(Register registerId, int memoryLocation, TwoByteOperation twoByteOperation){
       withRegisterAndByte(registerId, getByteOfMemoryYIndexedAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByte(Register registerId, int byteValue, TwoByteOperation twoByteOperation){
       int registerByte = getRegisterValue(registerId);

       registers.setRegisterAndFlags(registerId, twoByteOperation.perform(registerByte, byteValue));
    }

    private int performAND(int byteValueA, int byteValueB){
        return alu.and(RoxByte.fromLiteral(byteValueA), RoxByte.fromLiteral(byteValueB)).getRawValue();
    }

    private int performEOR(int byteValueA, int byteValueB){
        return alu.xor(RoxByte.fromLiteral(byteValueA), RoxByte.fromLiteral(byteValueB)).getRawValue();
    }

    private int performORA(int byteValueA, int byteValueB){
        return alu.or(RoxByte.fromLiteral(byteValueA), RoxByte.fromLiteral(byteValueB)).getRawValue();
    }

    private int performADC(int byteValueA, int byteValueB){
       return alu.adc(RoxByte.fromLiteral(byteValueA), RoxByte.fromLiteral(byteValueB)).getRawValue();
    }

    private int performSBC(int byteValueA, int byteValueB){
       return alu.sbc(RoxByte.fromLiteral(byteValueA), RoxByte.fromLiteral(byteValueB)).getRawValue();
    }
}
