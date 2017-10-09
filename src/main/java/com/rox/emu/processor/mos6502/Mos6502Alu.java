package com.rox.emu.processor.mos6502;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;

/**
 * Arithmetic Logic Unit for a {@link Mos6502}.<br/>
 * <br/>
 * Operations:
 * <ul>
 *  <li> {@link #adc} </li>
 *  <li> {@link #sbc} </li>
 *  <li> {@link #or} </li>
 *  <li> {@link #xor} </li>
 *  <li> {@link #and} </li>
 *  <li> {@link #asl} </li>
 * </ul>
 *
 * TODO think about multi byte addition - using this calculating > 1 byte memory doesn't work, of course.
 */
public class Mos6502Alu {

    private final Registers registers;

    public Mos6502Alu(Registers registers) {
        this.registers = registers;
    }

    /**
     * @return the result of <code>byteA ADD byteB</code>
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
     * This is effectively an {@link #adc} operation with <code>byteB</code> converted to it's ones compliment.
     * Combined with a <em>loaded carry flag</em>, this gives subtraction via twos compliment addition
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
        int carry = registers.getFlag(Registers.C) ? 1 : 0;
        final RoxWord result = RoxWord.literalFrom((byteA.getRawValue() << 1) + carry);
        registers.setFlagTo(Registers.C, result.getHighByte().isBitSet(0));
        return result.getLowByte();
    }
}
