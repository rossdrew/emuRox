package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;

/**
 * Arithmetic Logic Unit for a {@link Mos6502}.<br/>
 * <br/>
 * Functions: [ADD, OR, XOR, AND, Shift Right]
 */
public class Mos6502Alu {

    public RoxByte add(final RoxByte byteA, final RoxByte byteB){
        return RoxByte.literalFrom(byteA.getRawValue() + byteB.getRawValue());
    }

    public RoxByte sbc(RoxByte byteA, RoxByte byteB) {
        return add(byteA, byteB.inTwosCompliment());
    }
}
