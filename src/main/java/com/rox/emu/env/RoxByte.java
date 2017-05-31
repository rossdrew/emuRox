package com.rox.emu.env;

import com.rox.emu.InvalidDataTypeException;

/**
 * A representation of a byte that can be in different formats, so far only SIGNED_TWOS_COMPLIMENT.
 */
public final class RoxByte {
    /**
     * Specifies the format of the information held in a {@link RoxByte} instance, options:-
     *
     * <dl>
     *     <dt>SIGNED_TWOS_COMPLIMENT</dt>
     *     <dd>will only accept/return values in the range [-128...127] so 0b11111110 will represent -2</dd>
     *
     *     <dt>UNSIGNED</dt>
     *     <dd>will only accept/return values in the range [0...255] so 0b11111110 will represent 254</dd>
     * </dl>
     *
     * Both will only work with values in the binary range [0000 0000...1111 1111]
     */
    public enum ByteFormat {
        /**
         * Numbers so formatted will only accept/return values in the range <code>[-128...127]</code><br/>
         * <br/>
         * This corresponds with bit 7 being treated as a sign in a <em>two's compliment</em> number e.g.
         * <ul>
         *     <li><code>0b11111110</code> -> <code>-2</code></li>
         *     <li><code>0b00000001</code> -> &nbsp; <code>1</code></li>
         *     <li><code>0b10000001</code> -> &nbsp; <code>-127</code></li>
         *     <li><code>0b01111110</code> -> &nbsp; <code>126</code></li>
         * </ul>
         */
        SIGNED_TWOS_COMPLIMENT
    }

    private final int byteValue;
    private final ByteFormat format;

    private static int[] PLACE_VALUE = {1,2,4,8,16,32,64,128};

    /**
     * A {@link RoxByte} representing zero
     */
    public static RoxByte ZERO = new RoxByte(0, ByteFormat.SIGNED_TWOS_COMPLIMENT);

    private RoxByte(int value, ByteFormat format){
        this.byteValue = value;
        this.format = format;
    }

    private int fromTwosComplimented(int byteValue){
        return -(((~(byteValue-1))) & 0xFF);
    }

    private boolean inRange(int bit){
        return ((bit >= 0) && (bit <= 7));
    }

    /**
     * Create a {@link RoxByte} with a SIGNED_TWOS_COMPLIMENT value
     * @param value the value required for this byte to have
     * @return A {@link RoxByte}, SIGNED_TWOS_COMPLIMENT, with the specified value
     * @throws InvalidDataTypeException if the given value doesn't fit inside a SIGNED_TWOS_COMPLIMENT byte
     */
    public static RoxByte signedFrom(int value) throws InvalidDataTypeException {
        if (value > 127 || value < -128)
            throw new InvalidDataTypeException("Cannot convert " + value + " to unsigned byte.  Expected range (-128 -> 127)");

        return new RoxByte(value, ByteFormat.SIGNED_TWOS_COMPLIMENT);
    }

    /**
     * @return this SIGNED_TWOS_COMPLIMENT byte as an integer
     */
    public int getAsInt() {
        switch (format){
            default:
            case SIGNED_TWOS_COMPLIMENT:
                if (isBitSet(7))
                    return fromTwosComplimented(byteValue);
                else
                    return byteValue;
        }
    }

    /**
     * @return the format this byte is considered to be, at the moment, only SIGNED_TWOS_COMPLIMENT
     */
    public ByteFormat getFormat(){
        return format;
    }

    /**
     * @param bitToSet bit number (0-7) of the bit to set in the new {@link RoxByte}
     * @return A new {@link RoxByte} which is this one, with the specified bit set
     */
    public RoxByte withBit(int bitToSet) {
        if (!inRange(bitToSet))
            throw new ArrayIndexOutOfBoundsException("Bit #"+ bitToSet +" is out of range, expected (0-7)");
        return new RoxByte(PLACE_VALUE[bitToSet], ByteFormat.SIGNED_TWOS_COMPLIMENT);
    }

    /**
     * @param bitToTest bit number (0-7) of the bit to test
     * @return weather the specified bit is set in this byte
     */
    public boolean isBitSet(int bitToTest) {
        if (!inRange(bitToTest))
            throw new ArrayIndexOutOfBoundsException("Bit #"+ bitToTest +" is out of range, expected (0-7)");
        return (byteValue & PLACE_VALUE[bitToTest]) == PLACE_VALUE[bitToTest];
    }
}
