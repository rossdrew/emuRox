package com.rox.emu.env;

/**
 * A representation of a word, i.e. the combination of two {@link RoxByte}s.
 *
 * {@see RoxByte}
 *
 * @param rawValue the 16 bits of this word. Only the least significant 16 bits are kept.
 */
public record RoxWord(int rawValue) {
    public static final RoxWord ZERO = new RoxWord(0);

    public RoxWord {
        rawValue &= 0xFFFF;
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
        return new RoxWord(literalValue);
    }

    /**
     * Take the {@link RoxWord word} and move it's sign bit to the most significant
     * bit of an {@link int} representation of it
     *
     * @return This {@link RoxWord word} as it's {@link int} representation.
     */
    public int getAsInt() {
        return intFromTwosComplimented(rawValue);
    }

    private int intFromTwosComplimented(int rawValue){
        if (isBitSet(15))
            return -((-rawValue) & 0xFFFF);
        else
            return rawValue;
    }

    /**
     * @param bitToTest bit number (<code>0-15</code>) of the bit to test
     * @return weather the specified bit is set in this word
     */
    public boolean isBitSet(int bitToTest) {
        validateBit(bitToTest);
        return (rawValue & (1 << bitToTest)) != 0;
    }

    private void validateBit(final int bit){
        if ((bit < 0) || (bit > 15))
            throw new ArrayIndexOutOfBoundsException("Bit #"+ bit +" is out of range, expected (0-15)");
    }

    /**
     * @return The least significant {@link RoxByte byte} (lsb) of this {@link RoxWord word}
     */
    public RoxByte getLowByte() {
        return RoxByte.fromLiteral(rawValue & 0xFF);
    }

    /**
     * @return The most significant {@link RoxByte byte} (msb) of this {@link RoxWord word}
     */
    public RoxByte getHighByte() {
        return RoxByte.fromLiteral(rawValue >> 8);
    }

    /**
     * @return Return an {@link int} with identical two least significant bytes to this {@link RoxWord word}
     */
    public int getRawValue(){
        return rawValue;
    }

    @Override
    public String toString() {
        return "RoxWord{" + getAsInt() +"}";
    }
}
