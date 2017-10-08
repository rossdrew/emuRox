package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;

/**
 * Arithmetic Logic Unit for a {@link Mos6502}.<br/>
 * <br/>
 * Functions: [ADC, ABC, OR, XOR, AND]
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
    public RoxByte adc(final RoxByte byteA, final RoxByte byteB){
        int carry = registers.getFlag(Registers.C) ? 1 : 0;

        final RoxWord result = RoxWord.literalFrom(byteA.getRawValue() + byteB.getRawValue() + carry);

        registers.setFlagTo(Registers.C, result.getHighByte().isBitSet(0));

        //Set Overflow if the sign of both inputs is different from the sign of the result i.e. bit 7 set on ((a^result) & (b^result))
        if (and(xor(byteA, result.getLowByte()),
                xor(byteB, result.getLowByte())).isBitSet(7))
            registers.setFlag(Registers.V);

        return result.getLowByte();
    }

    /**
     * Perform an SBC of <code>byteA SBC byteB</code><br/>
     * <br/>
     * This is effectively an {@link #adc} operation with <code>byteB</code> converted to it's twos compliment
     *
     * @return the result of the SBC operation
     */
    public RoxByte sbc(RoxByte byteA, RoxByte byteB) {
        registers.setFlag(Registers.N);
        return adc(byteA, byteB.asOnesCompliment());
    }

    /**
     * @return the result of <code>byteA OR byteB</code><br/>
     */
    public RoxByte or(RoxByte byteA, RoxByte byteB) {
        return RoxByte.literalFrom(byteA.getRawValue() | byteB.getRawValue());
    }

    /**
     * @return the result of <code>byteA AND byteB</code><br/>
     */
    public RoxByte and(RoxByte byteA, RoxByte byteB) {
        return RoxByte.literalFrom(byteA.getRawValue() & byteB.getRawValue());
    }

    /**
     * @return the result of <code>byteA XOR byteB</code><br/>
     */
    public RoxByte xor(RoxByte byteA, RoxByte byteB) {
        return RoxByte.literalFrom(byteA.getRawValue() ^ byteB.getRawValue());
    }

    /**
     * @return the result of <code>ASL byteA</code>
     */
    public RoxByte asl(RoxByte byteA) {
        final RoxWord result = RoxWord.literalFrom(byteA.getRawValue() << 1);
        registers.setFlagTo(Registers.C, result.getHighByte().isBitSet(0));
        return result.getLowByte();
    }
}
