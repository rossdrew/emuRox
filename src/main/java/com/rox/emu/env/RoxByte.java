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
         *     <li><code>0b11111110</code> &rarr; <code>-2</code></li>
         *     <li><code>0b00000001</code> &rarr; &nbsp; <code>1</code></li>
         *     <li><code>0b10000001</code> &rarr; <code>-127</code></li>
         *     <li><code>0b01111110</code> &rarr; &nbsp; <code>126</code></li>
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

    private boolean bitInRange(int bit){
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
     * Create an 8 bit byte from the least significant 8 bits in the given {@link int}
     *
     * @param value an {@link int} from which to extract the bits to make this byte
     * @return a {@link RoxByte} made up from the least significant 8 bits of the given value
     */
    public static RoxByte literalFrom(int value) {
        return new RoxByte(value & 0xFF, ByteFormat.SIGNED_TWOS_COMPLIMENT);
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
     * Get the raw, unformatted value of this {@link Byte}
     *
     * @return 8 bits of the byte as an {@link int} as they are in memory
     */
    public int getRawValue(){
        return byteValue & 0xFF;
    }

    /**
     * @return the format this byte is considered to be, at the moment, only SIGNED_TWOS_COMPLIMENT
     */
    public ByteFormat getFormat(){
        return format;
    }

    /**
     * @param bitToSet bit number (<code>0-7</code>) of the bit to set in the new {@link RoxByte}
     * @return A new {@link RoxByte} which is this one, with the specified bit set
     */
    public RoxByte withBit(int bitToSet) {
        if (!bitInRange(bitToSet))
            throw new ArrayIndexOutOfBoundsException("Bit #"+ bitToSet +" is out of range, expected (0-7)");
        return new RoxByte(PLACE_VALUE[bitToSet] | this.byteValue, ByteFormat.SIGNED_TWOS_COMPLIMENT);
    }

    /**
     * @param bitToClear bit number (<code>0-7</code>) of the bit to clear in the new {@link RoxByte}
     * @return A new {@link RoxByte} which is this one, with the specified bit cleared
     */
    public RoxByte withoutBit(int bitToClear) {
        if (!bitInRange(bitToClear))
            throw new ArrayIndexOutOfBoundsException("Bit #"+ bitToClear +" is out of range, expected (0-7)");

        int withoutBit = (~(PLACE_VALUE[bitToClear])) & byteValue;
        return new RoxByte(withoutBit, ByteFormat.SIGNED_TWOS_COMPLIMENT);
    }

    /**
     * @param bitToTest bit number (<code>0-7</code>) of the bit to test
     * @return weather the specified bit is set in this byte
     */
    public boolean isBitSet(int bitToTest) {
        if (!bitInRange(bitToTest))
            throw new ArrayIndexOutOfBoundsException("Bit #"+ bitToTest +" is out of range, expected (0-7)");
        return (byteValue & PLACE_VALUE[bitToTest]) == PLACE_VALUE[bitToTest];
    }
}