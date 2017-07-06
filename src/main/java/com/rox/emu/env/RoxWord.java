package com.rox.emu.env;

/**
 * A representation of a word, i.e. the combination of two {@link RoxByte}s.
 *
 * {@see RoxByte}
 */
public class RoxWord {
    private final int wordValue;

    private RoxWord(int wordValue) {
        this.wordValue = wordValue;
    }

    public static RoxWord from(final RoxByte highByte,
                               final RoxByte lowByte){
        return new RoxWord(highByte.getRawValue() << 8 | lowByte.getRawValue());
    }

    public static RoxWord from(final RoxByte lowByte){
        return new RoxWord(lowByte.getRawValue());
    }

    public int getAsInt() {
        return wordValue;
    }
}
