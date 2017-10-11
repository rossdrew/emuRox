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
 *  <li> {@link #rol} </li>
 *  <li> {@link #lsr} </li>
 *  <li> {@link #ror} </li>
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
     * Return the addition of <code>byteA</code>, <code>byteB</code> and the contents of the {@link Registers} carry
     * flag.<br/>
     * <br>
     * The carry flag is used for multi-byte addition, so it should be cleared at the start of any addition
     * that doesn't need to take into account a carry from a previous one.
     *
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
     * Return the subtraction of <code>byteB</code> from <code>byteA</code> using the contents of the
     * {@link Registers} carry flag as a borrow.<br/>
     * <br/>
     * This is effectively an {@link #adc} operation with <code>byteB</code> converted to it's ones compliment.
     * Combined with a <em>loaded carry flag</em> used as a borrow, this gives subtraction via twos compliment
     * addition.<br/>
     * <br>
     * This means the opposite of {@link #adc}s carry behaviour is expected.  Any usage that doesn't need
     * to take into account of a previous bytes borrow, should load the carry flag to get normal behaviour.
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
     * Shift bits left and write a zero into the low order bit, setting the carry to whatever
     * is shifted out of the high order bit.
     *
     * @return the result of <code>ASL byteA</code>
     */
    public RoxByte asl(RoxByte byteA) {
        final RoxWord result = RoxWord.literalFrom((byteA.getRawValue() << 1));
        registers.setFlagTo(Registers.C, result.getHighByte().isBitSet(0));
        return result.getLowByte();
    }

    /**
     * Shift bits left and write the contents of the carry flag into the low order bit, setting
     * the carry to whatever is shifted out of the high order bit.
     *
     * @return the result of <code>ROL byteA</code>
     */
    public RoxByte rol(RoxByte byteA) {
        int carry = registers.getFlag(Registers.C) ? 1 : 0;
        final RoxWord result = RoxWord.literalFrom((byteA.getRawValue() << 1) + carry);
        registers.setFlagTo(Registers.C, result.getHighByte().isBitSet(0));
        return result.getLowByte();
    }

    /**
     * Shift bits right and write a zero into the high order bit, setting the carry to whatever
     * is shifted out of the low order bit.
     *
     * @return the result of <code>LSR byteA</code>
     */
    public RoxByte lsr(RoxByte byteA) {
        final RoxByte result = RoxByte.literalFrom((byteA.getRawValue() >> 1));
        registers.setFlagTo(Registers.C, byteA.isBitSet(0));
        return result;
    }

    /**
     * Shift bits right and write the contents of the carry flag into the high order bit,
     * setting the carry to whatever is shifted out of the low order bit.
     *
     * @return the result of <code>ROR byteA</code>
     */
    public RoxByte ror(RoxByte byteA) {
        int carry = registers.getFlag(Registers.C) ? 0b10000000 : 0;
        final RoxByte result = RoxByte.literalFrom((byteA.getRawValue() >> 1) | carry);
        registers.setFlagTo(Registers.C, byteA.isBitSet(0));
        return result;
    }
}
