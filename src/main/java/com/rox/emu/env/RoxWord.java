package com.rox.emu.env;

/**
 * A representation of a word, i.e. the combination of two {@link RoxByte}s.
 *
 * {@see RoxByte}
 */
public final class RoxWord {
    private final int wordValue;

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
    public static RoxWord literalFrom(final int literalValue) {
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

}
