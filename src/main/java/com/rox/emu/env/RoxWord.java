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

    public static RoxWord from(final RoxByte highByte,
                               final RoxByte lowByte){
        return new RoxWord(highByte.getRawValue() << 8 | lowByte.getRawValue());
    }

    public static RoxWord from(final RoxByte lowByte){
        return new RoxWord(lowByte.getRawValue());
    }

    public static RoxWord literalFrom(final int literalValue) {
        return new RoxWord(literalValue & 0xFFFF);
    }

    public int getAsInt() {
        return wordValue;
    }

    public RoxByte getLowByte() {
        return RoxByte.literalFrom(wordValue & 0xFF);
    }

    public RoxByte getHighByte() {
        return RoxByte.literalFrom(wordValue >> 8);
    }

}
