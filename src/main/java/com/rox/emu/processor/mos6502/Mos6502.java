package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.op.Mos6502OpCode;
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
       registers.setRegister(Register.ACCUMULATOR, RoxByte.ZERO);
       registers.setRegister(Register.X_INDEX, RoxByte.ZERO);
       registers.setRegister(Register.Y_INDEX, RoxByte.ZERO);
       registers.setRegister(Register.STATUS_FLAGS, RoxByte.fromLiteral(0x34));
       registers.setRegister(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFC)));
       registers.setRegister(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFD)));
       registers.setRegister(Register.STACK_POINTER_LOW, RoxByte.fromLiteral(0xFF));  //XXX Shouldmaybe be a max
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

        registers.setRegister(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFe)));
        registers.setRegister(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFF)));
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

        registers.setRegister(Register.PROGRAM_COUNTER_HI, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFA)));
        registers.setRegister(Register.PROGRAM_COUNTER_LOW, getByteOfMemoryAt(RoxWord.fromLiteral(0xFFFB)));
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

        final Mos6502OpCode opCode = Mos6502OpCode.from(nextProgramByte().getRawValue());

        //Execute the opcode
        log.debug("Instruction: {}...", opCode.getOpCodeName());
        switch (opCode){
            case JMP_ABS: //this is hard to deal with using my functional enums approach
                registers.setPC(nextProgramWord());
            break;

            case JMP_IND: //this is hard to deal with using my functional enums approach
                registers.setPC(getWordOfMemoryAt(nextProgramWord()));
            break;

            default:
                opCode.perform(alu, registers, memory);
                break;
        }
    }

    private RoxByte getRegisterValue(Register registerID){
        return registers.getRegister(registerID);
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
       return getByteOfMemoryAt(registers.getAndStepProgramCounter());
    }

    /**
     * Combine the next two bytes in program memory, as defined by
     * the Program Counter into a word so that:-><br/>
     * <br/>
     * PC[0] = high order byte<br/>
     * PC[1] = low order byte<br/>
     *<br/><br/>
     * <em>Increments the Program Counter by 1</em>
     *
     * @return word made up of both bytes
     */
    private RoxWord nextProgramWord(){
       return RoxWord.from(nextProgramByte(), nextProgramByte());
    }

    private void pushRegister(Register registerID){
        push(getRegisterValue(registerID));
    }

    /**
     * @param value {@link RoxByte} to push to the stack
     */
    private void push(RoxByte value){
       debug("PUSH {}(0b{}) to mem[0x{}]",  value.toString(),
                                                    Integer.toBinaryString(value.getRawValue()),
                                                    Integer.toHexString(getRegisterValue(Register.STACK_POINTER_LOW).getRawValue()).toUpperCase());

       setByteOfMemoryAt(RoxWord.from(RoxByte.fromLiteral(0x01), getRegisterValue(Register.STACK_POINTER_LOW)), value);
       registers.setRegister(Register.STACK_POINTER_LOW, RoxByte.fromLiteral(getRegisterValue(Register.STACK_POINTER_LOW).getRawValue() - 1));
    }

    private RoxByte getByteOfMemoryAt(RoxWord location){
       final RoxByte memoryByte = memory.getByte(RoxWord.fromLiteral(location.getRawValue()));
       debug("Got 0x{} from mem[{}]", Integer.toHexString(memoryByte.getRawValue()), Integer.toString(location.getRawValue()));
       return memoryByte;
    }

    private void setByteOfMemoryAt(RoxWord location, RoxByte newByte){
       memory.setByteAt(RoxWord.fromLiteral(location.getRawValue()), newByte);
       debug("Stored 0x{} at mem[{}]", Integer.toHexString(newByte.getRawValue()), Integer.toString(location.getRawValue()));
    }

    private RoxWord getWordOfMemoryAt(RoxWord location) {
       final RoxWord memoryWord = memory.getWord(location);
       debug("Got 0x{} from mem[{}]", Integer.toHexString(memoryWord.getRawValue()), location.toString());
       return memoryWord;
    }

    private void debug(final String message, String ... args){
        if (log.isDebugEnabled())
            log.debug(message, args);
    }
}
