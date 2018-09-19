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
            case INC_Z:
            case INC_Z_IX:
            case INC_ABS:
            case INC_ABS_IX:
            case DEC_Z:
            case DEC_Z_IX:
            case DEC_ABS:
            case DEC_ABS_IX:
            case INX:
            case DEX:
            case INY:
            case DEY:
            case LDX_I:
            case LDX_Z:
            case LDX_Z_IY:
            case LDX_ABS:
            case LDX_ABS_IY:
            case LDY_I:
            case LDY_Z:
            case LDY_Z_IX:
            case LDY_ABS:
            case LDY_ABS_IX:
            case LDA_I:
            case LDA_Z:
            case LDA_Z_IX:
            case LDA_ABS:
            case LDA_ABS_IY:
            case LDA_ABS_IX:
            case LDA_IND_IX:
            case LDA_IND_IY:
            case AND_Z:
            case AND_ABS:
            case AND_I:
            case AND_Z_IX:
            case AND_ABS_IX:
            case AND_ABS_IY:
            case AND_IND_IX:
            case AND_IND_IY:
            case ROR_A:
            case BIT_Z:   //XXX Needs reviewed
            case BIT_ABS: //XXX Needs reviewed
            case ORA_I:
            case ORA_Z:
            case ORA_Z_IX:
            case ORA_ABS:
            case ORA_ABS_IX:
            case ORA_ABS_IY:
            case ORA_IND_IX:
            case ORA_IND_IY:
            case EOR_I:
            case EOR_Z:
            case EOR_Z_IX:
            case EOR_ABS:
            case EOR_ABS_IX:
            case EOR_ABS_IY:
            case EOR_IND_IX:
            case EOR_IND_IY:
            case ADC_Z:
            case ADC_I:
            case ADC_ABS:
            case ADC_ABS_IX:
            case ADC_ABS_IY:
            case ADC_Z_IX:
            case ADC_IND_IX:
            case ADC_IND_IY:
            case CMP_I:
            case CMP_Z:
            case CMP_Z_IX:
            case CMP_ABS:
            case CMP_ABS_IX:
            case CMP_ABS_IY:
            case CMP_IND_IX:
            case CMP_IND_IY:
            case CPX_I:
            case CPX_Z:
            case CPX_ABS:
            case CPY_I:
            case CPY_Z:
            case CPY_ABS:
            case SBC_I:
            case SBC_Z:
            case SBC_Z_IX:
            case SBC_ABS:
            case SBC_ABS_IX:
            case SBC_ABS_IY:
            case SBC_IND_IX:
            case SBC_IND_IY:
            case STY_Z:
            case STY_ABS:
            case STY_Z_IX:
            case STA_Z:
            case STA_ABS:
            case STA_Z_IX:
            case STA_ABS_IX:
            case STA_ABS_IY:
            case STA_IND_IX:
            case STA_IND_IY:
            case STX_Z:
            case STX_Z_IY:
            case STX_ABS:
            case PHA:
            case PLA:
            case PHP:
            case PLP:
            case TAX:
            case TAY:
            case TYA:
            case TXA:
            case NOP:
            case SEI:
            case CLI:
            case SED:
            case CLD:
            case TXS:
            case TSX:
            case BCC:
            case BCS:
            case BEQ:
            case BNE:
            case BMI:
            case BPL:
            case BVS:
            case BVC:
            case JSR:
            case RTS:
            case RTI:
                opCode.perform(alu, registers, memory);
            break;

            case JMP_ABS: //this is hard to deal with using my functional enums approach
                registers.setPC(nextProgramWord());
            break;

            case JMP_IND: //this is hard to deal with using my functional enums approach
                registers.setPC(getWordOfMemoryAt(nextProgramWord()));
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

    private int fromOnesComplimented(int byteValue){
        return (~byteValue) & 0xFF;
    }

    private int fromTwosComplimented(int byteValue){
        return fromOnesComplimented(byteValue) - 1;
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

    @FunctionalInterface
    private interface TwoByteOperation {
       RoxByte perform(RoxByte byteValueOne, RoxByte byteValueTwo);
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

}
