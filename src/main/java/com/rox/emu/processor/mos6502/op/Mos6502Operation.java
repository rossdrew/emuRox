package com.rox.emu.processor.mos6502.op;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;

/**
 * MOS 6502 addressing-mode independent base operation
 */
public enum Mos6502Operation implements AddressedValueInstruction{
    BRK((a,r,m,v)->v),

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
        final RoxByte accumulator = r.getRegister(Registers.Register.ACCUMULATOR);
        final RoxByte newValue = a.adc(accumulator, v);
        r.setFlagsBasedOn(newValue);
        r.setRegister(Registers.Register.ACCUMULATOR, newValue);
        return v;
    }),

    /** Load byte into the accumulator */
    LDA((a,r,m,v)->{
        r.setFlagsBasedOn(v);
        r.setRegister(Registers.Register.ACCUMULATOR, v);
        return v;
    }),

    /** Clear the Overflow flag */
    CLV((a,r,m,v)->{
        r.clearFlag(Registers.Flag.OVERFLOW);
        return v;
    }),

    /**
     * Logical AND byte with that in the Accumulator and store the result in the accumulator,
     * setting flags based on the result
     * */
    AND((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(Registers.Register.ACCUMULATOR);
        final RoxByte newValue = a.and(accumulator, v);
        r.setFlagsBasedOn(newValue);
        r.setRegister(Registers.Register.ACCUMULATOR, newValue);
        return v;
    }),

    /**
     * Logical OR byte with that in the Accumulator and store the result in the accumulator,
     * setting flags based on the result
     * */
    ORA((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(Registers.Register.ACCUMULATOR);
        final RoxByte result = a.or(accumulator, v);
        r.setFlagsBasedOn(result);
        r.setRegister(Registers.Register.ACCUMULATOR, result);
        return v;
    }),

    /**
     * Logical XOR byte with that in the Accumulator and store the result in the accumulator,
     * setting flags based on the result
     */
    EOR((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(Registers.Register.ACCUMULATOR);
        final RoxByte result = a.xor(accumulator, v);
        r.setFlagsBasedOn(result);
        r.setRegister(Registers.Register.ACCUMULATOR, result);
        return v;
    }),

    /**
     * Subtract byte from that in the Accumulator (including carry) and store the result in the accumulator,
     * setting flags based on the result
     */
    SBC((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(Registers.Register.ACCUMULATOR);
        final RoxByte newValue = a.sbc(accumulator, v);
        r.setFlagsBasedOn(newValue);
        r.setRegister(Registers.Register.ACCUMULATOR, newValue);
        return v;
    }),

    /** Clear the carry flag */
    CLC((a,r,m,v)->{
        r.clearFlag(Registers.Flag.CARRY);
        return v;
    }),

    /** Set the carry flag */
    SEC((a,r,m,v)->{
        r.setFlag(Registers.Flag.CARRY);
        return v;
    }),

    /** Load byte into Y, setting flag based on that value */
    LDY((a,r,m,v)->{
        r.setFlagsBasedOn(v);
        r.setRegister(Registers.Register.Y_INDEX, v);
        return v;
    }),

    /** Load byte into X, setting flag based on that value */
    LDX((a,r,m,v)->{
        r.setFlagsBasedOn(v);
        r.setRegister(Registers.Register.X_INDEX, v);
        return v;
    }),

    /** Load byte into Y */
    STY((a,r,m,v)->{
        return r.getRegister(Registers.Register.Y_INDEX);
    }),

    /** Load byte into Accumulator */
    STA((a,r,m,v)->{
        return r.getRegister(Registers.Register.ACCUMULATOR);
    }),

    /** Load byte into X */
    STX((a,r,m,v)->{
        return r.getRegister(Registers.Register.X_INDEX);
    }),

    /** Increment the value of Y and set the flags based on the new value */
    INY((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(Registers.Register.Y_INDEX);
        final RoxByte newValue = a.adc(xValue, RoxByte.fromLiteral(1));
        r.setFlagsBasedOn(newValue);
        r.setRegister(Registers.Register.Y_INDEX, newValue);
        return v;
    }),

    /** Decrement the value of Y and set the flags based on the new value */
    DEY((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(Registers.Register.Y_INDEX);

        boolean carryWasSet = r.getFlag(Registers.Flag.CARRY);
        r.setFlag(Registers.Flag.CARRY);
        final RoxByte newValue = a.sbc(xValue, RoxByte.fromLiteral(1));
        if (carryWasSet) r.setFlag(Registers.Flag.CARRY); else r.clearFlag(Registers.Flag.CARRY);

        r.setFlagsBasedOn(newValue);
        r.setRegister(Registers.Register.Y_INDEX, newValue);
        return v;
    }),

    /** Increment the value of X and set the flags based on the new value */
    INX((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(Registers.Register.X_INDEX);
        final RoxByte newValue = a.adc(xValue, RoxByte.fromLiteral(1));
        r.setFlagsBasedOn(newValue);
        r.setRegister(Registers.Register.X_INDEX, newValue);
        return v;
    }),

    /** Decrement the value of X and set the flags based on the new value */
    DEX((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(Registers.Register.X_INDEX);

        boolean carryWasSet = r.getFlag(Registers.Flag.CARRY);
        r.setFlag(Registers.Flag.CARRY);
        final RoxByte newValue = a.sbc(xValue, RoxByte.fromLiteral(1));
        if (carryWasSet) r.setFlag(Registers.Flag.CARRY); else r.clearFlag(Registers.Flag.CARRY);

        r.setFlagsBasedOn(newValue);
        r.setRegister(Registers.Register.X_INDEX, newValue);
        return v;
    }),

    /** Increment the given byte in place and set the flags based on the new value */
    INC((a,r,m,v)->{
        final RoxByte newValue = a.adc(v, RoxByte.fromLiteral(1));
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    /** Decrement the given byte in place and set the flags based on the new value */
    DEC((a,r,m,v)->{
        boolean carryWasSet = r.getFlag(Registers.Flag.CARRY);
        r.setFlag(Registers.Flag.CARRY);
        final RoxByte newValue = a.sbc(v, RoxByte.fromLiteral(1));
        if (carryWasSet) r.setFlag(Registers.Flag.CARRY); else r.clearFlag(Registers.Flag.CARRY);
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    /** Push the Accumulator to the stack */
    PHA((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(Registers.Register.ACCUMULATOR);

        final RoxByte stackIndex = r.getRegister(Registers.Register.STACK_POINTER_HI);  //XXX Shouldn't this be LOW?
        final RoxWord stackEntry = RoxWord.from(RoxByte.fromLiteral(0x01), stackIndex);
        m.setByteAt(stackEntry, accumulatorValue);

        final RoxByte newStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() - 1); //XXX Should use alu
        r.setRegister(Registers.Register.STACK_POINTER_HI, newStackIndex);
        return v;
    }),

    /** Pull the Accumulator from the stack */
    PLA((a,r,m,v)->{
        final RoxByte stackIndex = r.getRegister(Registers.Register.STACK_POINTER_HI);  //XXX Shouldn't this be LOW?
        final RoxByte newStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() + 1); //XXX Should use alu

        final RoxWord stackEntry = RoxWord.from(RoxByte.fromLiteral(0x01), newStackIndex);
        final RoxByte stackValue = m.getByte(stackEntry);

        r.setRegister(Registers.Register.ACCUMULATOR, stackValue);
        r.setRegister(Registers.Register.STACK_POINTER_HI, newStackIndex);
        return v;
    }),

    /** Push the Status register to the stack */
    PHP((a,r,m,v)->{
        final RoxByte statusFlags = r.getRegister(Registers.Register.STATUS_FLAGS);

        final RoxByte stackIndex = r.getRegister(Registers.Register.STACK_POINTER_HI);  //XXX Shouldn't this be LOW?
        final RoxWord stackEntry = RoxWord.from(RoxByte.fromLiteral(0x01), stackIndex);
        m.setByteAt(stackEntry, statusFlags);

        final RoxByte newStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() - 1); //XXX Should use alu
        r.setRegister(Registers.Register.STACK_POINTER_HI, newStackIndex);
        return v;
    }),

    /** Pull the Status register from the stack */
    PLP((a,r,m,v)->{
        final RoxByte stackIndex = r.getRegister(Registers.Register.STACK_POINTER_HI);  //XXX Shouldn't this be LOW?
        final RoxByte newStackIndex = RoxByte.fromLiteral(stackIndex.getRawValue() + 1); //XXX Should use alu

        final RoxWord stackEntry = RoxWord.from(RoxByte.fromLiteral(0x01), newStackIndex);
        final RoxByte stackValue = m.getByte(stackEntry);

        r.setRegister(Registers.Register.STATUS_FLAGS, stackValue);
        r.setRegister(Registers.Register.STACK_POINTER_HI, newStackIndex);
        return v;
    }),

    /** Do nothing */
    NOP((a,r,m,v)->v),

    /** Set the Program Counter to the specified word value */
    JMP((a,r,m,v)->v), /* TODO Needs some thought, our structure deals with bytes but this deals with words and ABS is overridden*/

    /** Transfer Accumulator to the X register */
    TAX((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(Registers.Register.ACCUMULATOR);
        r.setRegister(Registers.Register.X_INDEX, accumulatorValue);
        return v;
    }),

    /** Transfer Accumulator to the Y register */
    TAY((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(Registers.Register.ACCUMULATOR);
        r.setRegister(Registers.Register.Y_INDEX, accumulatorValue);
        return v;
    }),

    /** Transfer the Y register to the Accumulator */
    TYA((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(Registers.Register.Y_INDEX);
        r.setRegister(Registers.Register.ACCUMULATOR, accumulatorValue);
        return v;
    }),

    /** Transfer the X register to the Accumulator */
    TXA((a,r,m,v)->{
        final RoxByte accumulatorValue = r.getRegister(Registers.Register.X_INDEX);
        r.setRegister(Registers.Register.ACCUMULATOR, accumulatorValue);
        return v;
    }),

    /** Push the X register to the stack */
    TXS((a,r,m,v)->{
        final RoxByte xValue = r.getRegister(Registers.Register.X_INDEX);
        r.setRegister(Registers.Register.STACK_POINTER_HI, xValue); //XXX Shouldn't this be LOW?
        return v;
    }),

    /** Pull the X register from the stack */
    TSX((a,r,m,v)->{
        final RoxByte stackIndex = r.getRegister(Registers.Register.STACK_POINTER_HI);  //XXX Shouldn't this be LOW?

        r.setRegister(Registers.Register.X_INDEX, stackIndex);
        r.setFlagsBasedOn(stackIndex);
        return v;
    }),

    /**
     * Bitwise AND compare the accumulator with byte, set Zero flag if they match
     * and set Overflow and Negative flags based on bits 6 and 7 of the provided byte
     */
    BIT((a,r,m,v)->{
        final RoxByte accumulator = r.getRegister(Registers.Register.ACCUMULATOR);
        final RoxByte result = a.and(accumulator, v);

        //XXX Need to properly look at this, http://obelisk.me.uk/6502/reference.html#BIT
        //    says that "Set if the result if the AND is zero", so only if no bits match?!
        r.setFlagTo(Registers.Flag.ZERO, (result.equals(accumulator)));
        r.setFlagTo(Registers.Flag.OVERFLOW, v.isBitSet(6));
        r.setFlagTo(Registers.Flag.NEGATIVE, v.isBitSet(7));
        return v;
    }),

    /** Compare (via subtraction) value with Accumulator, setting flags based on result */
    CMP((a,r,m,v)->{
        r.setFlag(Registers.Flag.CARRY); //XXX IS this the right place to be doing this?
        final RoxByte accumulator = r.getRegister(Registers.Register.ACCUMULATOR);
        final RoxByte resultOfSbc = a.sbc(accumulator, v);
        r.setFlagsBasedOn(resultOfSbc);
        return v;
    }),

    /** Compare (via subtraction) value with X register, setting flags based on result */
    CPX((a,r,m,v)->{
        r.setFlag(Registers.Flag.CARRY); //XXX IS this the right place to be doing this?
        final RoxByte x = r.getRegister(Registers.Register.X_INDEX);
        final RoxByte resultOfSbc = a.sbc(x, v);
        r.setFlagsBasedOn(resultOfSbc);
        return v;
    }),

    /** Compare (via subtraction) value with Y register, setting flags based on result */
    CPY((a,r,m,v)->{
        r.setFlag(Registers.Flag.CARRY); //XXX IS this the right place to be doing this?
        final RoxByte y = r.getRegister(Registers.Register.Y_INDEX);
        final RoxByte resultOfSbc = a.sbc(y, v);
        r.setFlagsBasedOn(resultOfSbc);
        return v;
    }),

    JSR((a,r,m,v)->v),

    BPL((a,r,m,v)->v),
    BMI((a,r,m,v)->v),
    BVC((a,r,m,v)->v),
    BVS((a,r,m,v)->v),

    BCC((a,r,m,v)->{
        if (!r.getFlag(Registers.Flag.CARRY))
            branchTo(a,r,m,v);
        return v;
    }),

    BCS((a,r,m,v)->{
        if (r.getFlag(Registers.Flag.CARRY))
            branchTo(a,r,m,v);
        return v;
    }),

    BNE((a,r,m,v)->v),
    BEQ((a,r,m,v)->v),

    ROL((a,r,m,v)->{
        final RoxByte newValue = a.rol(v);
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    ROR((a,r,m,v)->{
        final RoxByte newValue = a.ror(v);
        r.setFlagsBasedOn(newValue);
        return newValue;
    }),

    SEI((a,r,m,v)->{
        r.setFlag(Registers.Flag.IRQ_DISABLE);
        return v;
    }),

    CLI((a,r,m,v)->{
        r.clearFlag(Registers.Flag.IRQ_DISABLE);
        return v;
    }),

    SED((a,r,m,v)->{
        r.setFlag(Registers.Flag.DECIMAL_MODE);
        return v;
    }),

    CLD((a,r,m,v)->{
        r.clearFlag(Registers.Flag.DECIMAL_MODE);
        return v;
    }),

    RTS((a,r,m,v)->v),
    RTI((a,r,m,v)->v);

    //XXX Is there a better way to do this?
    private static void branchTo(Mos6502Alu alu, Registers registers, Memory mem, final RoxByte offset){
        //Create a silent register/alu pair, loading/unloading the carry for sbc/adc
        final Registers rCopy = registers.copy();
        rCopy.setFlagTo(Registers.Flag.CARRY, offset.isNegative());
        final Mos6502Alu silentAlu = new Mos6502Alu(rCopy);

        //Add to low byte, carry to high
        final RoxByte loAddressByte = silentAlu.adc(registers.getRegister(Registers.Register.PROGRAM_COUNTER_LOW), offset);

        //TODO Test overflow
        final RoxByte hiAddressByte = silentAlu.adc(registers.getRegister(Registers.Register.PROGRAM_COUNTER_HI), RoxByte.ZERO);

        registers.setPC(RoxWord.from(loAddressByte));
    }

    private AddressedValueInstruction instruction;

    Mos6502Operation(AddressedValueInstruction instruction){
        this.instruction = instruction;
    }

    @Override
    public RoxByte perform(Mos6502Alu alu, Registers registers, Memory memory, RoxByte value) {
        return instruction.perform(alu, registers, memory, value);
    }
}