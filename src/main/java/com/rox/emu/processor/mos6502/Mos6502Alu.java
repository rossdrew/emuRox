package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;

/**
 * Arithmetic Logic Unit for a {@link Mos6502}.<br/>
 * <br/>
 * Functions: [ADD, OR, XOR, AND, Shift Right]
 */
public class Mos6502Alu {

    private final Registers registers;

    public Mos6502Alu(Registers registers) {
        this.registers = registers;
    }

    /**
     * Perform an ADD of <code>byteA ADD byteB</code>
     *
     * @return the result of the ADD operation
     */
    public RoxByte add(final RoxByte byteA, final RoxByte byteB){
        final RoxWord result = RoxWord.literalFrom(byteA.getRawValue() + byteB.getRawValue() + (registers.getFlag(Registers.C)?1:0));

        registers.setFlagTo(Registers.C, result.getHighByte().isBitSet(0));

        //Set Overflow if the sign of both inputs is different from the sign of the result
        //  (a^result) & (b^result) -> bit 7 set?
//        if (and(xor(byteA, result.getLowByte()),
//                xor(byteB, result.getLowByte())).isBitSet(7))
//            registers.setFlag(Registers.V);

        return result.getLowByte();
    }

    /**
     * Perform an SBC of <code>byteA SBC byteB</code><br/>
     * <br/>
     * This is effectively an {@link #add} operation with <code>byteB</code> converted to it's twos compliment
     *
     * @return the result of the SBC operation
     */
    public RoxByte sub(RoxByte byteA, RoxByte byteB) {
        return add(byteA, byteB.inTwosCompliment());
    }

    public RoxByte or(RoxByte byteA, RoxByte byteB) {
        return RoxByte.literalFrom(byteA.getRawValue() | byteB.getRawValue());
    }

    public RoxByte and(RoxByte byteA, RoxByte byteB) {
        return RoxByte.literalFrom(byteA.getRawValue() & byteB.getRawValue());
    }

    public RoxByte xor(RoxByte byteA, RoxByte byteB) {
        return RoxByte.literalFrom(byteA.getRawValue() ^ byteB.getRawValue());
    }
}
