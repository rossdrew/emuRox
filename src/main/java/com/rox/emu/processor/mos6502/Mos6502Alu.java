package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;

/**
 * Arithmetic Logic Unit for a {@link Mos6502}.<br/>
 * <br/>
 * Functions: [ADD, OR, XOR, AND, Shift Right]
 */
public class Mos6502Alu {
    /**
     * Perform an ADD of <code>byteA ADD byteB</code>
     *
     * @return the result of the ADD operation
     */
    public RoxByte add(final RoxByte byteA, final RoxByte byteB){
        return RoxByte.literalFrom(byteA.getRawValue() + byteB.getRawValue());
    }

    /**
     * Perform an SBC of <code>byteA SBC byteB</code><br/>
     * <br/>
     * This is effectively an {@link #add} operation with <code>byteB</code> converted to it's twos compliment
     *
     * @return the result of the SBC operation
     */
    public RoxByte sbc(RoxByte byteA, RoxByte byteB) {
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
