package com.rox.emu.processor.mos6502;

import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.op.OpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A emulated representation of MOS 6502, 8 bit
 * microprocessor functionality.
 *
 * @author Ross Drew
 */
public class CPU {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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
        LOG.trace("RESETTING...");
        setRegisterValue(Registers.REG_ACCUMULATOR, 0x0);
        setRegisterValue(Registers.REG_X_INDEX, 0x0);
        setRegisterValue(Registers.REG_Y_INDEX, 0x0);
        setRegisterValue(Registers.REG_STATUS, 0x34);
        setRegisterValue(Registers.REG_PC_HIGH, getByteOfMemoryAt(0xFFFC));
        setRegisterValue(Registers.REG_PC_LOW, getByteOfMemoryAt(0xFFFD));
        setRegisterValue(Registers.REG_SP, 0xFF);
        LOG.trace("...READY!");
    }

    public void irq() {
        LOG.debug("IRQ!");
        registers.setFlag(Registers.STATUS_FLAG_IRQ_DISABLE);

        push(getRegisterValue(Registers.REG_PC_HIGH));
        push(getRegisterValue(Registers.REG_PC_LOW));
        push(getRegisterValue(Registers.REG_STATUS));

        setRegisterValue(Registers.REG_PC_HIGH, getByteOfMemoryAt(0xFFFe));
        setRegisterValue(Registers.REG_PC_LOW, getByteOfMemoryAt(0xFFFF));
    }

    public void nmi() {
        LOG.debug("NMI!");
        registers.setFlag(Registers.STATUS_FLAG_IRQ_DISABLE);

        push(getRegisterValue(Registers.REG_PC_HIGH));
        push(getRegisterValue(Registers.REG_PC_LOW));
        push(getRegisterValue(Registers.REG_STATUS));

        setRegisterValue(Registers.REG_PC_HIGH, getByteOfMemoryAt(0xFFFA));
        setRegisterValue(Registers.REG_PC_LOW, getByteOfMemoryAt(0xFFFB));
    }

    /**
     * @return the {@link Registers} being used
     */
    public Registers getRegisters(){
        return registers;
    }

    private int getRegisterValue(int registerID){
        return registers.getRegister(registerID);
    }

    private void setRegisterValue(int registerID, int value){
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
        LOG.trace("STEP >>>");

        final int opCodeByte = nextProgramByte();
        final OpCode opCode = OpCode.from(opCodeByte);

        //Execute the opcode
        LOG.debug("Instruction: " + opCode.getOpCodeName() + "...");
        switch (opCode){
            default:
            case OP_BRK:
                registers.setPC(registers.getPC() + 2);
                push(registers.getRegister(Registers.REG_PC_HIGH));
                push(registers.getRegister(Registers.REG_PC_LOW));
                push(registers.getRegister(Registers.REG_STATUS) | Registers.STATUS_FLAG_BREAK);

                registers.setRegister(Registers.REG_PC_HIGH, getByteOfMemoryAt(0xFFFE));
                registers.setRegister(Registers.REG_PC_LOW, getByteOfMemoryAt(0xFFFF));
                break;

            case OP_ASL_A:
                withRegister(Registers.REG_ACCUMULATOR, this::performROL);
            break;

            case OP_ASL_Z:
                withByteAt(nextProgramByte(), this::performASL);
            break;

            case OP_ASL_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performASL);
            break;

            case OP_ASL_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performASL);
            break;

            case OP_ASL_ABS:
                withByteAt(nextProgramWord(), this::performASL);
            break;

            case OP_LSR_A:
                withRegister(Registers.REG_ACCUMULATOR, this::performLSR);
            break;

            case OP_LSR_Z:
                withByteAt(nextProgramByte(), this::performLSR);
            break;

            case OP_LSR_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performLSR);
            break;

            case OP_LSR_ABS:
                withByteAt(nextProgramWord(), this::performLSR);
            break;

            case OP_LSR_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performLSR);
            break;

            case OP_ROL_A:
                withRegister(Registers.REG_ACCUMULATOR, this::performROL);
            break;

            case OP_ROL_Z:
                withByteAt(nextProgramByte(), this::performROL);
            break;

            case OP_ROL_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performROL);
             break;

            case OP_ROL_ABS:
                withByteAt(nextProgramWord(), this::performROL);
                break;

            case OP_ROL_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performROL);
             break;

            /* Not implemented and/or not published on older 6502s */
            case OP_ROR_A:
                withRegister(Registers.REG_ACCUMULATOR, this::performROR);
                break;

            case OP_SEC:
                registers.setFlag(Registers.STATUS_FLAG_CARRY);
                break;

            case OP_CLC:
                registers.clearFlag(Registers.STATUS_FLAG_CARRY);
                break;

            case OP_CLV:
                registers.clearFlag(Registers.STATUS_FLAG_OVERFLOW);
                break;

            case OP_INC_Z:
                withByteAt(nextProgramByte(), this::performINC);
            break;

            case OP_INC_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performINC);
            break;

            case OP_INC_ABS:
                withByteAt(nextProgramWord(), this::performINC);break;

            case OP_INC_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performINC);
            break;

            case OP_DEC_Z:
                withByteAt(nextProgramByte(), this::performDEC);
            break;

            case OP_DEC_Z_IX:
                withByteXIndexedAt(nextProgramByte(), this::performDEC);
            break;

            case OP_DEC_ABS:
                withByteAt(nextProgramWord(), this::performDEC);
            break;

            case OP_DEC_ABS_IX:
                withByteXIndexedAt(nextProgramWord(), this::performDEC);
            break;

            case OP_INX:
                withRegister(Registers.REG_X_INDEX, this::performINC);
                break;

            case OP_DEX:
                withRegister(Registers.REG_X_INDEX, this::performDEC);
                break;

            case OP_INY:
                withRegister(Registers.REG_Y_INDEX, this::performINC);
                break;

            case OP_DEY:
                withRegister(Registers.REG_Y_INDEX, this::performDEC);
                break;

            case OP_LDX_I:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, nextProgramByte());
                break;

            case OP_LDX_Z:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, getByteOfMemoryAt(nextProgramByte()));
                break;

            case OP_LDX_Z_IY:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, getByteOfMemoryYIndexedAt(nextProgramByte()));
                break;

            case OP_LDX_ABS:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, getByteOfMemoryAt(nextProgramWord()));
                break;

            case OP_LDX_ABS_IY:
                registers.setRegisterAndFlags(Registers.REG_X_INDEX, getByteOfMemoryYIndexedAt(nextProgramWord()));
                break;

            case OP_LDY_I:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, nextProgramByte());
                break;

            case OP_LDY_Z:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, getByteOfMemoryAt(nextProgramByte()));
                break;

            case OP_LDY_Z_IX:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, getByteOfMemoryXIndexedAt(nextProgramByte()));
                break;

            case OP_LDY_ABS:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, getByteOfMemoryAt(nextProgramWord()));
                break;

            case OP_LDY_ABS_IX:
                registers.setRegisterAndFlags(Registers.REG_Y_INDEX, getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case OP_LDA_I:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, nextProgramByte());
                break;

            case OP_LDA_Z:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramByte()));
                break;

            case OP_LDA_Z_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramByte()));
                break;

            case OP_LDA_ABS:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(nextProgramWord()));
                break;

            case OP_LDA_ABS_IY:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryYIndexedAt(nextProgramWord()));
                break;

            case OP_LDA_ABS_IX:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryXIndexedAt(nextProgramWord()));
                break;

            case OP_LDA_IND_IX: {
                int pointerLocation = getWordOfMemoryXIndexedAt(nextProgramByte());
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(pointerLocation));
            }break;

            case OP_LDA_IND_IY: {
                int pointerLocation = getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Registers.REG_Y_INDEX);
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, getByteOfMemoryAt(pointerLocation));
            }break;

            case OP_AND_Z:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performAND);
                break;

            case OP_AND_ABS:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case OP_AND_I:
                withRegisterAndByte(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performAND);
                break;

            case OP_AND_Z_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performAND);
                break;

            case OP_AND_ABS_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case OP_AND_ABS_IY:
                withRegisterAndByteYIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performAND);
                break;

            case OP_AND_IND_IX: {
                int pointerLocation = getWordOfMemoryXIndexedAt(nextProgramByte());
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performAND);
            }break;

            case OP_AND_IND_IY: {
                int pointerLocation = getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Registers.REG_Y_INDEX);
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performAND);
            }break;

            case OP_BIT_Z:
                performBIT(getByteOfMemoryAt(nextProgramByte()));
            break;

            case OP_BIT_ABS:
                performBIT(getByteOfMemoryAt(nextProgramWord()));
            break;

            case OP_ORA_I:
                withRegisterAndByte(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performORA);
                break;

            case OP_ORA_Z:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performORA);
                break;

            case OP_ORA_Z_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performORA);
                break;

            case OP_ORA_ABS:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performORA);
                break;

            case OP_ORA_ABS_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performORA);
                break;

            case OP_ORA_ABS_IY:
                withRegisterAndByteYIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performORA);
                break;

            case OP_ORA_IND_IX: {
                int pointerLocation = getWordOfMemoryXIndexedAt(nextProgramByte());
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performORA);
            }break;

            case OP_ORA_IND_IY: {
                int pointerLocation = getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Registers.REG_Y_INDEX);
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performORA);
            }break;

            case OP_EOR_I:
                withRegisterAndByte(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performEOR);
                break;

            case OP_EOR_Z:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performEOR);
                break;

            case OP_EOR_Z_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performEOR);
                break;

            case OP_EOR_ABS:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performEOR);
                break;

            case OP_EOR_ABS_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performEOR);
                break;

            case OP_EOR_ABS_IY:
                withRegisterAndByteYIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performEOR);
                break;

            case OP_EOR_IND_IX: {
                int pointerLocation = getWordOfMemoryXIndexedAt(nextProgramByte());
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performEOR);
            }break;

            case OP_EOR_IND_IY: {
                int pointerLocation = getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Registers.REG_Y_INDEX);
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performEOR);
            }break;

            case OP_ADC_Z:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performADC);
                break;

            case OP_ADC_I:
                withRegisterAndByte(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performADC);
                break;

            case OP_ADC_ABS:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performADC);
                break;

            case OP_ADC_ABS_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performADC);
                break;

            case OP_ADC_ABS_IY:
                withRegisterAndByteYIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performADC);
                break;

            case OP_ADC_Z_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performADC);
                break;

            case OP_ADC_IND_IX: {
                int pointerLocation = getWordOfMemoryXIndexedAt(nextProgramByte());
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performADC);
            }break;

            case OP_ADC_IND_IY: {
                int pointerLocation = getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Registers.REG_Y_INDEX);
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performADC);
            }break;

            case OP_CMP_I:
                performCMP(nextProgramByte(), Registers.REG_ACCUMULATOR);
                break;

            case OP_CMP_Z:
                performCMP(getByteOfMemoryAt(nextProgramByte()), Registers.REG_ACCUMULATOR);
                break;

            case OP_CMP_Z_IX:
                performCMP(getByteOfMemoryXIndexedAt(nextProgramByte()), Registers.REG_ACCUMULATOR);
                break;

            case OP_CMP_ABS:
                performCMP(getByteOfMemoryAt(nextProgramWord()), Registers.REG_ACCUMULATOR);
                break;

            case OP_CMP_ABS_IX:
                performCMP(getByteOfMemoryXIndexedAt(nextProgramWord()), Registers.REG_ACCUMULATOR);
                break;

            case OP_CMP_ABS_IY:
                performCMP(getByteOfMemoryYIndexedAt(nextProgramWord()), Registers.REG_ACCUMULATOR);
                break;

            case OP_CMP_IND_IX: {
                int pointerLocation = getWordOfMemoryXIndexedAt(nextProgramByte());
                performCMP(getByteOfMemoryAt(pointerLocation), Registers.REG_ACCUMULATOR);
            }break;

            case OP_CMP_IND_IY: {
                int pointerLocation = getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Registers.REG_Y_INDEX);
                performCMP(getByteOfMemoryAt(pointerLocation), Registers.REG_ACCUMULATOR);
            }break;

            case OP_CPX_I:
                performCMP(nextProgramByte(), Registers.REG_X_INDEX);
                break;

            case OP_CPX_Z:
                performCMP(getByteOfMemoryAt(nextProgramByte()), Registers.REG_X_INDEX);
                break;

            case OP_CPX_ABS:
                performCMP(getByteOfMemoryAt(nextProgramWord()), Registers.REG_X_INDEX);
                break;

            case OP_CPY_I:
                performCMP(nextProgramByte(), Registers.REG_Y_INDEX);
                break;

            case OP_CPY_Z:
                performCMP(getByteOfMemoryAt(nextProgramByte()), Registers.REG_Y_INDEX);
                break;

            case OP_CPY_ABS:
                performCMP(getByteOfMemoryAt(nextProgramWord()), Registers.REG_Y_INDEX);
                break;

            case OP_SBC_I:
                withRegisterAndByte(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performSBC);
                break;

            case OP_SBC_Z:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performSBC);
                break;

            case OP_SBC_Z_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramByte(), this::performSBC);
                break;

            case OP_SBC_ABS:
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performSBC);
                break;

            case OP_SBC_ABS_IX:
                withRegisterAndByteXIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performSBC);
                break;

            case OP_SBC_ABS_IY:
                withRegisterAndByteYIndexedAt(Registers.REG_ACCUMULATOR, nextProgramWord(), this::performSBC);
                break;

            case OP_SBC_IND_IX: {
                int pointerLocation = getWordOfMemoryXIndexedAt(nextProgramByte());
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performSBC);
            }break;

            case OP_SBC_IND_IY: {
                int pointerLocation = getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Registers.REG_Y_INDEX);
                withRegisterAndByteAt(Registers.REG_ACCUMULATOR, pointerLocation, this::performSBC);
            }break;

            case OP_STY_Z:
                setByteOfMemoryAt(nextProgramByte(), getRegisterValue(Registers.REG_Y_INDEX));
                break;

            case OP_STY_ABS:
                setByteOfMemoryAt(nextProgramWord(), getRegisterValue(Registers.REG_Y_INDEX));
                break;

            case OP_STY_Z_IX:
                setByteOfMemoryXIndexedAt(nextProgramByte(), getRegisterValue(Registers.REG_Y_INDEX));
                break;

            case OP_STA_Z:
                setByteOfMemoryAt(nextProgramByte(), getRegisterValue(Registers.REG_ACCUMULATOR));
                break;

            case OP_STA_ABS:
                setByteOfMemoryAt(nextProgramWord(), getRegisterValue(Registers.REG_ACCUMULATOR));
                break;

            case OP_STA_Z_IX:
                setByteOfMemoryXIndexedAt(nextProgramByte(), getRegisterValue(Registers.REG_ACCUMULATOR));
                break;

            case OP_STA_ABS_IX:
                setByteOfMemoryXIndexedAt(nextProgramWord(), getRegisterValue(Registers.REG_ACCUMULATOR));
                break;

            case OP_STA_ABS_IY:
                setByteOfMemoryYIndexedAt(nextProgramWord(), getRegisterValue(Registers.REG_ACCUMULATOR));
                break;

            case OP_STA_IND_IX: {
                int pointerLocation = getWordOfMemoryXIndexedAt(nextProgramByte());
                setByteOfMemoryAt(pointerLocation, getRegisterValue(Registers.REG_ACCUMULATOR));
            }break;

            case OP_STA_IND_IY: {
                int pointerLocation = getWordOfMemoryAt(nextProgramByte()) + getRegisterValue(Registers.REG_Y_INDEX);
                setByteOfMemoryAt(pointerLocation, getRegisterValue(Registers.REG_ACCUMULATOR));
            }break;

            case OP_STX_Z:
                setByteOfMemoryAt(nextProgramByte(), getRegisterValue(Registers.REG_X_INDEX));
                break;

            case OP_STX_Z_IY:
                setByteOfMemoryYIndexedAt(nextProgramByte(), getRegisterValue(Registers.REG_X_INDEX));
                break;

            case OP_STX_ABS:
                setByteOfMemoryAt(nextProgramWord(), getRegisterValue(Registers.REG_X_INDEX));
                break;

            case OP_PHA:
                push(getRegisterValue(Registers.REG_ACCUMULATOR));
                break;

            case OP_PLA:
                registers.setRegisterAndFlags(Registers.REG_ACCUMULATOR, pop());
                break;

            case OP_PHP:
                push(getRegisterValue(Registers.REG_STATUS));
                break;

            case OP_PLP:
                registers.setRegister(Registers.REG_STATUS, pop());
                break;

            case OP_JMP_ABS: {
                int h = nextProgramByte();
                int l = nextProgramByte();
                registers.setRegister(Registers.REG_PC_HIGH, h);
                setRegisterValue(Registers.REG_PC_LOW, l);
            }break;

            case OP_JMP_IND: {
                int h = nextProgramByte();
                int l = nextProgramByte();
                int pointer = (h << 8 | l);
                registers.setPC(getWordOfMemoryAt(pointer));
            }break;

            case OP_BCS:
                branchIf(registers.getFlag(Registers.STATUS_FLAG_CARRY));
                break;

            case OP_BCC:
                branchIf(!registers.getFlag(Registers.STATUS_FLAG_CARRY));
                break;

            case OP_BEQ:
                branchIf(registers.getFlag(Registers.STATUS_FLAG_ZERO));
                break;

            case OP_BNE:
                branchIf(!registers.getFlag(Registers.STATUS_FLAG_ZERO));
                break;

            case OP_BMI:
                branchIf(registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
                break;

            case OP_JSR:
                int hi = nextProgramByte();
                int lo = nextProgramByte();
                push(getRegisterValue(Registers.REG_PC_HIGH));
                push(getRegisterValue(Registers.REG_PC_LOW));
                setRegisterValue(Registers.REG_PC_HIGH, hi);
                setRegisterValue(Registers.REG_PC_LOW, lo);
                break;

            case OP_BPL:
                branchIf(!registers.getFlag(Registers.STATUS_FLAG_NEGATIVE));
                break;

            case OP_BVS:
                branchIf(registers.getFlag(Registers.STATUS_FLAG_OVERFLOW));
                break;

            case OP_BVC:
                branchIf(!registers.getFlag(Registers.STATUS_FLAG_OVERFLOW));
                break;

            case OP_TAX:
                setRegisterValue(Registers.REG_X_INDEX, getRegisterValue(Registers.REG_ACCUMULATOR));
                break;

            case OP_TAY:
                setRegisterValue(Registers.REG_Y_INDEX, getRegisterValue(Registers.REG_ACCUMULATOR));
                break;

            case OP_TYA:
                setRegisterValue(Registers.REG_ACCUMULATOR, getRegisterValue(Registers.REG_Y_INDEX));
                break;

            case OP_TXA:
                setRegisterValue(Registers.REG_ACCUMULATOR, getRegisterValue(Registers.REG_X_INDEX));
                break;

            case OP_TXS:
                setRegisterValue(Registers.REG_SP, getRegisterValue(Registers.REG_X_INDEX));
                break;

            case OP_TSX:
                setRegisterValue(Registers.REG_X_INDEX, getRegisterValue(Registers.REG_SP));
                registers.setFlagsBasedOn(getRegisterValue(Registers.REG_X_INDEX));
                break;

            case OP_NOP:
                //Do nothing
                break;

            case OP_SEI:
                registers.setFlag(Registers.STATUS_FLAG_IRQ_DISABLE);
                break;

            case OP_CLI:
                registers.clearFlag(Registers.STATUS_FLAG_IRQ_DISABLE);
                break;

            case OP_SED:
                registers.setFlag(Registers.STATUS_FLAG_DEC);
                break;

            case OP_CLD:
                registers.clearFlag(Registers.STATUS_FLAG_DEC);
                break;

            case OP_RTS:
                setRegisterValue(Registers.REG_PC_LOW, pop());
                setRegisterValue(Registers.REG_PC_HIGH, pop());
                break;

            case OP_RTI:
                setRegisterValue(Registers.REG_STATUS, pop());
                setRegisterValue(Registers.REG_PC_LOW, pop());
                setRegisterValue(Registers.REG_PC_HIGH, pop());
                break;
        }
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
        return (byte1 << 8) | nextProgramByte() ;
    }

    /**
     * Pop value from stack
     *
     * @return popped value
     */
    private int pop(){
        setRegisterValue(Registers.REG_SP, getRegisterValue(Registers.REG_SP) + 1);
        int address = 0x0100 | getRegisterValue(Registers.REG_SP);
        int value = getByteOfMemoryAt(address);
        LOG.trace("POP " + value + "(0b" + Integer.toBinaryString(value) + ") from mem[0x" + Integer.toHexString(address).toUpperCase() + "]");
        return value;
    }

    /**
     * Push to stack
     *
     * @param value value to push
     */
    private void push(int value){
        LOG.trace("PUSH " + value + "(0b" + Integer.toBinaryString(value) + ") to mem[0x" + Integer.toHexString(getRegisterValue(Registers.REG_SP)).toUpperCase() + "]");
        setByteOfMemoryAt(0x0100 | getRegisterValue(Registers.REG_SP), value);
        setRegisterValue(Registers.REG_SP, getRegisterValue(Registers.REG_SP) - 1);
    }

    private int getByteOfMemoryXIndexedAt(int location){
        return getByteOfMemoryAt(location, getRegisterValue(Registers.REG_X_INDEX));
    }

    private int getByteOfMemoryYIndexedAt(int location){
        return getByteOfMemoryAt(location, getRegisterValue(Registers.REG_Y_INDEX));
    }

    private void setByteOfMemoryYIndexedAt(int location, int newByte){
        setByteOfMemoryAt(location, getRegisterValue(Registers.REG_Y_INDEX), newByte);
    }

    private int getByteOfMemoryAt(int location){
        return getByteOfMemoryAt(location, 0);
    }

    private int getByteOfMemoryAt(int location, int index){
        final int memoryByte = memory.getByte(location + index);
        LOG.trace("Got 0x" + Integer.toHexString(memoryByte) + " from mem[" + location + (index != 0 ? "[" + index + "]" : "") +"]");
        return memoryByte;
    }

    private void setByteOfMemoryXIndexedAt(int location, int newByte){
        setByteOfMemoryAt(location, getRegisterValue(Registers.REG_X_INDEX), newByte);
    }

    private void setByteOfMemoryAt(int location, int newByte){
        setByteOfMemoryAt(location, 0, newByte);
    }

    private void setByteOfMemoryAt(int location, int index, int newByte){
        memory.setByteAt(location + index, newByte);
        LOG.trace("Stored 0x" + Integer.toHexString(newByte) + " at mem[" + location + (index != 0 ? "[" + index + "]" : "") +"]");
    }

    private int getWordOfMemoryXIndexedAt(int location){
        int indexedLocation = location + getRegisterValue(Registers.REG_X_INDEX);
        return getWordOfMemoryAt(indexedLocation);
    }

    private int getWordOfMemoryAt(int location) {
        int memoryWord = memory.getWord(location);
        LOG.trace("Got 0x" + Integer.toHexString(memoryWord) + " from mem[" + location +"]");
        return memoryWord;
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

    /**
     * Call {@link CPU#branchTo(int)} with next program byte
     *
     * @param condition if {@code true} then branch is followed
     */
    private void branchIf(boolean condition){
        int location = nextProgramByte();
        LOG.debug("{Branch:0x" + Integer.toHexString(registers.getPC()) + " by " + Integer.toBinaryString(location) + "} " + (condition ? "YES->" : "NO..."));
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
            setRegisterValue(Registers.REG_PC_LOW, getRegisterValue(Registers.REG_PC_LOW) - fromTwosComplimented(displacementByte));
        else
            setRegisterValue(Registers.REG_PC_LOW, getRegisterValue(Registers.REG_PC_LOW) + displacementByte);
    }

    private int twosComplimentOf(int byteValue){
        return ((~byteValue) + 1) & 0xFF;
    }

    private int fromTwosComplimented(int byteValue){
        return ((~byteValue)) & 0xFF;
    }

    //XXX Need to use 2s compliment addition (subtraction)
    private void performCMP(int value, int toRegister){
        int result = getRegisterValue(toRegister) - value;
        int twosComplimentResult = performSilentSBC(getRegisterValue(toRegister), value);
        registers.setFlagsBasedOn(result & 0xFF);

        if (result >=0)
            registers.setFlag(Registers.STATUS_FLAG_CARRY);
        else
            registers.clearFlag(Registers.STATUS_FLAG_CARRY);
    }

    private int performSilentSBC(int a, int b){
        int statusState = registers.getRegister(Registers.REG_STATUS);
        registers.setFlag(Registers.STATUS_FLAG_CARRY);

        int result = performSBC(a,b);

        registers.setRegister(Registers.REG_STATUS, statusState);
        return result;
    }

    private int rightShift(int value, boolean carryIn){
        return (value >> 1) | (carryIn ? 0b10000000 : 0);
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

    private void withRegister(int registerId, SingleByteOperation singleByteOperation){
        int b = getRegisterValue(registerId);
        setRegisterValue(registerId, singleByteOperation.perform(b));
    }

    private int performASL(int byteValue){
        int newValue = byteValue << 1;
        setCarryFlagBasedOn(newValue);
        registers.setFlagsBasedOn(newValue);
        return newValue;
    }

    private int performROL(int initialValue){
        int rotatedValue = (initialValue << 1) | (registers.getFlag(Registers.STATUS_FLAG_CARRY) ? 1 : 0);
        setCarryFlagBasedOn(rotatedValue);
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue & 0xFF;
    }

    private int performROR(int initialValue){
        int rotatedValue = rightShift(initialValue, (registers.getFlag(Registers.STATUS_FLAG_CARRY)));
        setBorrowFlagFor(initialValue);
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue & 0xFF;
    }

    private int performLSR(int initialValue){
        int rotatedValue = rightShift(initialValue, false);
        setBorrowFlagFor(initialValue);
        registers.setFlagsBasedOn(rotatedValue);
        return rotatedValue;
    }

    private int performINC(int initialValue){
        int incrementedValue = (initialValue + 1) & 0xFF;
        registers.setFlagsBasedOn(incrementedValue);
        return incrementedValue;
    }

    private int performDEC(int initialValue){
        int incrementedValue = (initialValue - 1) & 0xFF;
        registers.setFlagsBasedOn(incrementedValue);
        return incrementedValue;
    }

    private void performBIT(int memData) {
        if ((memData & getRegisterValue(Registers.REG_ACCUMULATOR)) == memData)
            registers.setFlag(Registers.STATUS_FLAG_ZERO);
        else
            registers.clearFlag(Registers.STATUS_FLAG_ZERO);

        //Set N, V to bits 7 and 6 of memory data
        setRegisterValue(Registers.REG_STATUS, (memData & 0b11000000) | (getRegisterValue(Registers.REG_STATUS) & 0b00111111));
    }

    @FunctionalInterface
    private interface TwoByteOperation {
        int perform(int byteValueOne, int byteValueTwo);
    }

    private void withRegisterAndByteAt(int registerId, int memoryLocation, TwoByteOperation twoByteOperation){
        withRegisterAndByte(registerId, getByteOfMemoryAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByteXIndexedAt(int registerId, int memoryLocation, TwoByteOperation twoByteOperation){
        withRegisterAndByte(registerId, getByteOfMemoryXIndexedAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByteYIndexedAt(int registerId, int memoryLocation, TwoByteOperation twoByteOperation){
        withRegisterAndByte(registerId, getByteOfMemoryYIndexedAt(memoryLocation), twoByteOperation);
    }

    private void withRegisterAndByte(int registerId, int byteValue, TwoByteOperation twoByteOperation){
        int registerByte = getRegisterValue(registerId);

        registers.setRegisterAndFlags(registerId, twoByteOperation.perform(registerByte, byteValue));
//        else
//            setRegisterValue(registerId, twoByteOperation.perform(registerByte, byteValue));
    }

    private int performAND(int byteValueA, int byteValueB){
        return byteValueA & byteValueB;
    }

    private int performEOR(int byteValueA, int byteValueB){
        return byteValueA ^ byteValueB;
    }

    private int performORA(int byteValueA, int byteValueB){
        return byteValueA | byteValueB;
    }

    private int performADC(int byteValueA, int byteValueB){
        int carry = (registers.getFlag(Registers.STATUS_FLAG_CARRY) ? 1 : 0);
        return (adc(byteValueA, byteValueB + carry) & 0xFF);
    }

    private int performSBC(int byteValueA, int byteValueB){
        registers.setFlag(Registers.STATUS_FLAG_NEGATIVE);
        int borrow = (registers.getFlag(Registers.STATUS_FLAG_CARRY) ? 0 : 1);
        int byteValueBAndBorrow = twosComplimentOf(byteValueB + borrow);

        return adc(byteValueA, byteValueBAndBorrow) & 0xFF;
    }

    /**
     * Perform a binary addition, setting Carry and Overflow flags as required.
     *
     * @param byteValueA addition term 1
     * @param byteValueB addition term 2
     * @return the result of (byteValueA + byteValueB)
     */
    private int adc(int byteValueA, int byteValueB) {
        int result = byteValueA + byteValueB;

        //Set Carry, if bit 8 is set on new accumulator value, ignoring in 2s compliment addition (subtraction)
        if (!registers.getFlag(Registers.STATUS_FLAG_NEGATIVE)){
            setCarryFlagBasedOn(result);
        }else {
            registers.clearFlag(Registers.STATUS_FLAG_CARRY);
        }

        //Set Overflow if the sign of both inputs is different from the sign of the result
        if (((byteValueA ^ result) & (byteValueB ^ result) & 0x80) != 0)
            registers.setFlag(Registers.STATUS_FLAG_OVERFLOW);
        return result;
    }
}
