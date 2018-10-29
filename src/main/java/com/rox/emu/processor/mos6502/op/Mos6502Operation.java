package com.rox.emu.processor.mos6502.op;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;

import java.util.Arrays;

import static com.rox.emu.processor.mos6502.Registers.Flag.*;
import static com.rox.emu.processor.mos6502.Registers.Register.*;

/**
 * MOS 6502 addressing-mode independent base operation
 */
public enum Mos6502Operation implements AddressedValueInstruction {
    /**
     * Operates like an interrupt, PC & Status is pushed to stack and PC is set to contents of {@code 0xFFFE}
     * and {@code 0xFFFF}.<br/>
     * <br/>
     * BRK is unlike an interrupt in that PC+2 is saved to the stack, this may not be the next instruction
     * and a correction may be necessary.  Due to the assumed use of BRK to path existing programs where
     * BRK replaces a 2-byte instruction.
     */
    BRK((a,r,m,v)->{
        final RoxWord pc = r.getPC();
        r.setPC(RoxWord.fromLiteral(pc.getRawValue() + 2));

        final RoxByte pcHi = r.getRegister(PROGRAM_COUNTER_HI);
        final RoxByte pcLo = r.getRegister(PROGRAM_COUNTER_LOW);
        final RoxByte status = r.getRegister(STATUS_FLAGS).withBit(BREAK.getIndex());

        Arrays.asList(pcHi, pcLo, status).forEach(value -> {
            final RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);
            final RoxByte nextStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() - 1);     //XXX Should use alu
            m.setByteAt(RoxWord.from(RoxByte.fromLiteral(0x01), stackIndex), value);
            r.setRegister(STACK_POINTER_LOW, nextStackIndex);
        });

        final RoxByte pcHiJmp = m.getByte(RoxWord.fromLiteral(0xFFFE));
        final RoxByte pcLoJmp = m.getByte(RoxWord.fromLiteral(0xFFFF));

        r.setRegister(PROGRAM_COUNTER_HI, pcHiJmp);
        r.setRegister(PROGRAM_COUNTER_LOW, pcLoJmp);

        return v;
    }),

    /** Shift all bits in byte left by one place, setting flags based on the result */
    ASL((a,r,m,v) -> {
        final RoxByte newValue = a.asl(v);
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    /** Shift all bits in byte right by one place, setting flags based on the result */
    LSR((a,r,m,v)->{
        final RoxByte newValue = a.lsr(v);
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    /** Add byte to that in the Accumulator and store the result in the accumulator */
    ADC((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(ACCUMULATOR);
        final RoxByte newValue = a.adc(accumulator, v);
        r.setFlagsBasedOn(newValue);
        r.setRegister(ACCUMULATOR, newValue);
        return v;
    }),

    /** Load byte into the accumulator */
    LDA((a,r,m,v)->{
        r.setFlagsBasedOn(v);
        r.setRegister(ACCUMULATOR, v);
        return v;
    }),

    /** Clear the Overflow flag */
    CLV((a,r,m,v)->{
        r.clearFlag(OVERFLOW);
        return v;
    }),

    /**
     * Logical AND byte with that in the Accumulator and store the result in the accumulator,
     * setting flags based on the result
     * */
    AND((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(ACCUMULATOR);
        final RoxByte newValue = a.and(accumulator, v);
        r.setFlagsBasedOn(newValue);
        r.setRegister(ACCUMULATOR, newValue);
        return v;
    }),

    /**
     * Logical OR byte with that in the Accumulator and store the result in the accumulator,
     * setting flags based on the result
     * */
    ORA((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(ACCUMULATOR);
        final RoxByte result = a.or(accumulator, v);
        r.setFlagsBasedOn(result);
        r.setRegister(ACCUMULATOR, result);
        return v;
    }),

    /**
     * Logical XOR byte with that in the Accumulator and store the result in the accumulator,
     * setting flags based on the result
     */
    EOR((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(ACCUMULATOR);
        final RoxByte result = a.xor(accumulator, v);
        r.setFlagsBasedOn(result);
        r.setRegister(ACCUMULATOR, result);
        return v;
    }),

    /**
     * Subtract byte from that in the Accumulator (including carry) and store the result in the accumulator,
     * setting flags based on the result
     */
    SBC((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(ACCUMULATOR);
        final RoxByte newValue = a.sbc(accumulator, v);
        r.setFlagsBasedOn(newValue);
        r.setRegister(ACCUMULATOR, newValue);
        return v;
    }),

    /** Clear the carry flag */
    CLC((a,r,m,v)->{
        r.clearFlag(CARRY);
        return v;
    }),

    /** Set the carry flag */
    SEC((a,r,m,v)->{
        r.setFlag(CARRY);
        return v;
    }),

    /** Load byte into Y, setting flag based on that value */
    LDY((a,r,m,v)->{
        r.setFlagsBasedOn(v);
        r.setRegister(Y_INDEX, v);
        return v;
    }),

    /** Load byte into X, setting flag based on that value */
    LDX((a,r,m,v)->{
        r.setFlagsBasedOn(v);
        r.setRegister(X_INDEX, v);
        return v;
    }),

    /** Load byte into Y */
    STY((a,r,m,v)->r.getRegister(Y_INDEX)),

    /** Load byte into Accumulator */
    STA((a,r,m,v)->r.getRegister(ACCUMULATOR)),

    /** Load byte into X */
    STX((a,r,m,v)-> r.getRegister(X_INDEX)),

    /** Increment the value of Y and set the flags based on the new value */
    INY((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(Y_INDEX);
        final RoxByte newValue = a.adc(xValue, RoxByte.fromLiteral(1));
        r.setFlagsBasedOn(newValue);
        r.setRegister(Y_INDEX, newValue);
        return v;
    }),

    /** Decrement the value of Y and set the flags based on the new value */
    DEY((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(Y_INDEX);

        boolean carryWasSet = r.getFlag(CARRY);
        r.setFlag(CARRY);
        final RoxByte newValue = a.sbc(xValue, RoxByte.fromLiteral(1));
        r.setFlagTo(CARRY, carryWasSet);

        r.setFlagsBasedOn(newValue);
        r.setRegister(Y_INDEX, newValue);
        return v;
    }),

    /** Increment the value of X and set the flags based on the new value */
    INX((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(X_INDEX);
        final RoxByte newValue = a.adc(xValue, RoxByte.fromLiteral(1));
        r.setFlagsBasedOn(newValue);
        r.setRegister(X_INDEX, newValue);
        return v;
    }),

    /** Decrement the value of X and set the flags based on the new value */
    DEX((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(X_INDEX);

        boolean carryWasSet = r.getFlag(CARRY);
        r.setFlag(CARRY);
        final RoxByte newValue = a.sbc(xValue, RoxByte.fromLiteral(1));
        r.setFlagTo(CARRY, carryWasSet);

        r.setFlagsBasedOn(newValue);
        r.setRegister(X_INDEX, newValue);
        return v;
    }),

    /** Increment the given byte in place and set the flags based on the new value */
    INC((a,r,m,v)->{
        boolean carryWasSet = r.getFlag(CARRY);
        final RoxByte newValue = a.adc(v, RoxByte.fromLiteral(1));
        r.setFlagsBasedOn(newValue);
        r.setFlagTo(CARRY, carryWasSet);
        return newValue;
    }),

    /** Decrement the given byte in place and set the flags based on the new value */
    DEC((a,r,m,v)->{
        boolean carryWasSet = r.getFlag(CARRY);
        r.setFlag(CARRY);
        final RoxByte newValue = a.sbc(v, RoxByte.fromLiteral(1));
        r.setFlagTo(CARRY, carryWasSet);
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    /** Push the Accumulator to the stack */
    PHA((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(ACCUMULATOR);

        final RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);
        final RoxWord stackEntryLocation = RoxWord.from(RoxByte.fromLiteral(0x01), stackIndex);
        m.setByteAt(stackEntryLocation, accumulatorValue);

        final RoxByte newStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() - 1); //XXX Should use alu
        r.setRegister(STACK_POINTER_LOW, newStackIndex);

        return v;
    }),

    /** Pull the Accumulator from the stack */
    PLA((a,r,m,v)->{
        final RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);
        final RoxByte newStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() + 1); //XXX Should use alu

        final RoxWord stackEntry = RoxWord.from(RoxByte.fromLiteral(0x01), newStackIndex);
        final RoxByte stackValue = m.getByte(stackEntry);

        r.setRegister(ACCUMULATOR, stackValue);
        r.setRegister(STACK_POINTER_LOW, newStackIndex);
        return v;
    }),

    /** Push the Status register to the stack */
    PHP((a,r,m,v)->{
        final RoxByte statusFlags = r.getRegister(STATUS_FLAGS);

        final RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);
        final RoxWord stackEntry = RoxWord.from(RoxByte.fromLiteral(0x01), stackIndex);
        m.setByteAt(stackEntry, statusFlags);

        final RoxByte newStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() - 1); //XXX Should use alu
        r.setRegister(STACK_POINTER_LOW, newStackIndex);
        return v;
    }),

    /** Pull the Status register from the stack */
    PLP((a,r,m,v)->{
        final RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);
        final RoxByte newStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() + 1); //XXX Should use alu

        final RoxWord stackEntry = RoxWord.from(RoxByte.fromLiteral(0x01), newStackIndex);
        final RoxByte stackValue = m.getByte(stackEntry);

        r.setRegister(STATUS_FLAGS, stackValue);
        r.setRegister(STACK_POINTER_LOW, newStackIndex);
        return v;
    }),

    /** Do nothing */
    NOP((a,r,m,v)->v),

    /** Set the Program Counter to the specified word value */
    JMP((a,r,m,v)->v), /* Essentially a NOP - Our structure deals with bytes but this deals with words and ABS is overridden*/

    /** Transfer Accumulator to the X register */
    TAX((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(ACCUMULATOR);
        r.setRegister(X_INDEX, accumulatorValue);
        return v;
    }),

    /** Transfer Accumulator to the Y register */
    TAY((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(ACCUMULATOR);
        r.setRegister(Y_INDEX, accumulatorValue);
        return v;
    }),

    /** Transfer the Y register to the Accumulator */
    TYA((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(Y_INDEX);
        r.setRegister(ACCUMULATOR, accumulatorValue);
        return v;
    }),

    /** Transfer the X register to the Accumulator */
    TXA((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(X_INDEX);
        r.setRegister(ACCUMULATOR, accumulatorValue);
        return v;
    }),

    /** Push the X register to the stack pointer */
    TXS((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(X_INDEX);
        r.setRegister(STACK_POINTER_LOW, xValue);
        return v;
    }),

    /** Pull the X register from the stack pointer */
    TSX((a,r,m,v)->{
        final RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);

        r.setRegister(X_INDEX, stackIndex);
        r.setFlagsBasedOn(stackIndex);
        return v;
    }),

    /**
     * Bitwise AND compare the accumulator with byte, set Zero flag if they match
     * and set Overflow and Negative flags based on bits 6 and 7 of the provided byte
     *
     * XXX Needs reviewed
     */
    BIT((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(ACCUMULATOR);
        final RoxByte result = a.and(accumulator, v);

        //XXX Need to properly look at this, http://obelisk.me.uk/6502/reference.html#BIT
        //    says that "Set if the result if the AND is zero", so only if no bits match?!
        r.setFlagTo(ZERO, (result.equals(accumulator)));
        r.setFlagTo(OVERFLOW, v.isBitSet(6));
        r.setFlagTo(NEGATIVE, v.isBitSet(7));
        return v;
    }),

    /** Compare (via subtraction) value with Accumulator, setting flags based on result */
    CMP((a,r,m,v)->{
        r.setFlag(CARRY); //XXX IS this the right place to be doing this?
        final RoxByte accumulator = r.getRegister(ACCUMULATOR);
        final RoxByte resultOfSbc = a.sbc(accumulator, v);
        r.setFlagsBasedOn(resultOfSbc);
        return v;
    }),

    /** Compare (via subtraction) value with X register, setting flags based on result */
    CPX((a,r,m,v)->{
        r.setFlag(CARRY); //XXX IS this the right place to be doing this?
        final RoxByte x = r.getRegister(X_INDEX);
        final RoxByte resultOfSbc = a.sbc(x, v);
        r.setFlagsBasedOn(resultOfSbc);
        return v;
    }),

    /** Compare (via subtraction) value with Y register, setting flags based on result */
    CPY((a,r,m,v)->{
        r.setFlag(CARRY); //XXX IS this the right place to be doing this?
        final RoxByte y = r.getRegister(Y_INDEX);
        final RoxByte resultOfSbc = a.sbc(y, v);
        r.setFlagsBasedOn(resultOfSbc);
        return v;
    }),

    /** Jump to the subroutine at the given absolute address */
    JSR((a,r,m,argument1)->{
        final RoxWord arg2Address = r.getAndStepProgramCounter();
        final RoxByte argument2 = m.getByte(arg2Address);

        final RoxByte pcHi = r.getRegister(PROGRAM_COUNTER_HI);
        final RoxByte pcLo = r.getRegister(PROGRAM_COUNTER_LOW);

        Arrays.asList(pcHi, pcLo).forEach(value -> {
            RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);
            RoxByte nextStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() - 1);     //XXX Should use alu
            m.setByteAt(RoxWord.from(RoxByte.fromLiteral(0x01), stackIndex), value);
            r.setRegister(STACK_POINTER_LOW, nextStackIndex);
        });

        r.setRegister(PROGRAM_COUNTER_HI, argument1);
        r.setRegister(PROGRAM_COUNTER_LOW, argument2);

        return argument1;
    }),

    /** Branch to offset if {@code NEGATIVE} flag is <em>not</em> set */
    BPL((a,r,m,offset)->{
        if (!r.getFlag(NEGATIVE))
            branchTo(r,offset);
        return offset;
    }),

    /** Branch to offset if {@code NEGATIVE} flag <em>is</em> set */
    BMI((a,r,m,offset)->{
        if (r.getFlag(NEGATIVE))
            branchTo(r,offset);
        return offset;
    }),

    /** Branch to offset if {@code OVERFLOW} flag is <em>not</em> set */
    BVC((a,r,m,offset)->{
        if (!r.getFlag(OVERFLOW))
            branchTo(r,offset);
        return offset;
    }),

    /** Branch to offset if {@code OVERFLOW} flag <em>is</em> set */
    BVS((a,r,m,offset)->{
        if (r.getFlag(OVERFLOW))
            branchTo(r,offset);
        return offset;
    }),

    /** Branch to offset if {@code CARRY} flag is <em>not</em> set */
    BCC((a,r,m,offset)->{
        if (!r.getFlag(CARRY))
            branchTo(r,offset);
        return offset;
    }),

    /** Branch to offset if {@code CARRY} flag <em>is</em> set */
    BCS((a,r,m,offset)->{
        if (r.getFlag(CARRY))
            branchTo(r,offset);
        return offset;
    }),

    /** Branch to offset if {@code ZERO} flag is <em>not</em> set */
    BNE((a,r,m,offset)->{
        if (!r.getFlag(ZERO))
            branchTo(r,offset);
        return offset;
    }),

    /** Branch to offset if {@code ZERO} flag <em>is</em> set */
    BEQ((a,r,m,offset)->{
        if (r.getFlag(ZERO))
            branchTo(r,offset);
        return offset;
    }),

    /** Perform a rotate left on the given value */
    ROL((a,r,m,v)->{
        final RoxByte newValue = a.rol(v);
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    /** Perform a rotate right on the given value */
    ROR((a,r,m,v)->{
        final RoxByte newValue = a.ror(v);
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    /** Set the {@code IRQ} flag */
    SEI((a,r,m,v)->{
        r.setFlag(IRQ_DISABLE);
        return v;
    }),

    /** Clear the {@code IRQ} flag */
    CLI((a,r,m,v)->{
        r.clearFlag(IRQ_DISABLE);
        return v;
    }),

    /** Set the {@code DECIMAL MODE} flag */
    SED((a,r,m,v)->{
        r.setFlag(DECIMAL_MODE);
        return v;
    }),

    /** Clear the {@code DECIMAL MODE} flag */
    CLD((a,r,m,v)->{
        r.clearFlag(DECIMAL_MODE);
        return v;
    }),

    /** Return from subroutine */
    RTS((a,r,m,v)->{
        Arrays.asList(PROGRAM_COUNTER_LOW,
                      PROGRAM_COUNTER_HI).forEach(registerId -> {
            final RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);
            final RoxByte nextStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() + 1);  //XXX alu?
            r.setRegister(STACK_POINTER_LOW, nextStackIndex);
            final RoxByte stackValue = m.getByte(RoxWord.from(RoxByte.fromLiteral(0x01), nextStackIndex));
            r.setRegister(registerId, stackValue);
        });

        return v;
    }),

    /** Return from interrupt, setting the status flags from the stack */
    RTI((a,r,m,v)->{
        Arrays.asList(STATUS_FLAGS,
                      PROGRAM_COUNTER_LOW,
                      PROGRAM_COUNTER_HI).forEach(registerId -> {
            final RoxByte stackIndex = r.getRegister(STACK_POINTER_LOW);
            final RoxByte nextStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() + 1);
            r.setRegister(STACK_POINTER_LOW, nextStackIndex);
            final RoxByte stackValue = m.getByte(RoxWord.from(RoxByte.fromLiteral(0x01), nextStackIndex));
            r.setRegister(registerId, stackValue);
        });

        return v;
    });

    //XXX Is there a better way to do this?
    private static void branchTo(final Registers registers, final RoxByte offset){
        //Create a silent register/alu pair, loading/unloading the carry for sbc/adc
        final Registers rCopy = registers.copy();
        rCopy.setFlagTo(CARRY, offset.isNegative());
        final Mos6502Alu silentAlu = new Mos6502Alu(rCopy);

        //Add to low byte, carry to high
        final RoxByte loAddressByte = silentAlu.adc(registers.getRegister(PROGRAM_COUNTER_LOW), offset);

        registers.setPC(RoxWord.from(loAddressByte));
    }

    private final AddressedValueInstruction instruction;

    Mos6502Operation(AddressedValueInstruction instruction){
        this.instruction = instruction;
    }

    @Override
    public RoxByte perform(Mos6502Alu alu, Registers registers, Memory memory, RoxByte value) {
        return instruction.perform(alu, registers, memory, value);
    }
}
