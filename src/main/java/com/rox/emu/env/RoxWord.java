package com.rox.emu.env;

import java.util.Objects;

/**
 * A representation of a word, i.e. the combination of two {@link RoxByte}s.
 *
 * {@see RoxByte}
 */
public final class RoxWord {
    private final int wordValue;

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

    public int getAsInt() {
        return wordValue;
    }

    public RoxByte getLowByte() {
        return RoxByte.fromLiteral(wordValue & 0xFF);
    }

    public RoxByte getHighByte() {
        return RoxByte.fromLiteral(wordValue >> 8);
    }

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
