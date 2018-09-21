package com.rox.emu.env;

import java.util.Objects;

/**
 * A representation of a word, i.e. the combination of two {@link RoxByte}s.
 *
 * {@see RoxByte}
 */
public final class RoxWord {
    private final int wordValue;

    /** Binary digit place values */
    private static final int[] PLACE_VALUE = {  0b0000000000000001, //Byte 1
                                                0b0000000000000010,
                                                0b0000000000000100,
                                                0b0000000000001000,
                                                0b0000000000010000,
                                                0b0000000000100000,
                                                0b0000000001000000,
                                                0b0000000010000000,
                                                0b0000000100000000, //Byte 2
                                                0b0000001000000000,
                                                0b0000010000000000,
                                                0b0000100000000000,
                                                0b0001000000000000,
                                                0b0010000000000000,
                                                0b0100000000000000,
                                                0b1000000000000000
                                              };

    public static final RoxWord ZERO = new RoxWord(0);

    private RoxWord(int wordValue) {
        this.wordValue = wordValue;
    }

    /**
     * Create a {@link RoxWord} from the given high and low {@link RoxByte}s
     */
    public static RoxWord from(final RoxByte highByte,
                               final RoxByte lowByte){
        return new RoxWord(highByte.getRawValue() << 8 | lowByte.getRawValue());
    }

    /**
     * Create a {@link RoxWord} with the given {@link RoxByte} as the lowest significant byte.
     */
    public static RoxWord from(final RoxByte lowByte){
        return new RoxWord(lowByte.getRawValue());
    }

    /**
     * Extract a literal {@link RoxWord} from the first two least significant bytes of the given {@link int}
     */
    public static RoxWord fromLiteral(final int literalValue) {
        return new RoxWord(literalValue & 0xFFFF);
    }

    /**
     * Take the {@link RoxWord word} and move it's sign bit to the most significant
     * bit of an {@link int} representation of it
     *
     * @return This {@link RoxWord word} as it's {@link int} representation.
     */
    public int getAsInt() {
        return intFromTwosComplimented(wordValue);
    }

    private int intFromTwosComplimented(int wordValue){
        if (isBitSet(15))
            return -((~(wordValue-1)) & 0xFFFF);
        else
            return wordValue;
    }

    /**
     * @param bitToTest bit number (<code>0-7</code>) of the bit to test
     * @return weather the specified bit is set in this byte
     */
    public boolean isBitSet(int bitToTest) {
        validateBit(bitToTest);
        return (wordValue & PLACE_VALUE[bitToTest]) == PLACE_VALUE[bitToTest];
    }

    private void validateBit(final int bit){
        if ((bit < 0) || (bit > 15))
            throw new ArrayIndexOutOfBoundsException("Bit #"+ bit +" is out of range, expected (0-15)");
    }

    /**
     * @return The least significant {@link RoxByte byte} (lsb) of this {@link RoxWord word}
     */
    public RoxByte getLowByte() {
        return RoxByte.fromLiteral(wordValue & 0xFF);
    }

    /**
     * @return The most significant {@link RoxByte byte} (msb) of this {@link RoxWord word}
     */
    public RoxByte getHighByte() {
        return RoxByte.fromLiteral(wordValue >> 8);
    }

    /**
     * @return Return an {@link int} with identical two least significant bytes to this {@link RoxWord word}
     */
    public int getRawValue(){
        return wordValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (o instanceof Integer)
            return (wordValue == RoxWord.fromLiteral((Integer)o).getRawValue());
        if (o instanceof RoxByte)
            return wordValue == RoxWord.from((RoxByte)o).getRawValue();
        else if (getClass() != o.getClass())
            return false;

        return wordValue == ((RoxWord) o).wordValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordValue);
    }

    @Override
    public String toString() {
        return "RoxWord{" + getAsInt() +"}";
    }
}
