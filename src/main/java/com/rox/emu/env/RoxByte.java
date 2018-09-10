package com.rox.emu.env;

import com.rox.emu.InvalidDataTypeException;

import java.util.Objects;

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

    /** Binary digit place values */
    private static final int[] PLACE_VALUE = {1, 2, 4, 8, 16, 32, 64, 128};

    /**
     * A {@link RoxByte} representing zero
     */
    public static final RoxByte ZERO = new RoxByte(0, ByteFormat.SIGNED_TWOS_COMPLIMENT);

    private RoxByte(int value, ByteFormat format){
        this.byteValue = value;
        this.format = format;
    }

    /**
     * @return a new {@link RoxByte} representing this value converted to it's twos compliment value
     */
    public RoxByte asTwosCompliment(){
        return RoxByte.fromLiteral(((~getRawValue()) + 1) & 0xFF);
    }

    /**
     * @return a new {@link RoxByte} representing this value converted to it's ones compliment value
     */
    public RoxByte asOnesCompliment(){
        return RoxByte.fromLiteral((~getRawValue()) & 0xFF);
    }

    /**
     * Create a {@link RoxByte} with a SIGNED_TWOS_COMPLIMENT value
     * @param value the value required for this byte to have
     * @return A {@link RoxByte}, SIGNED_TWOS_COMPLIMENT, with the specified value
     * @throws InvalidDataTypeException if the given value doesn't fit inside a SIGNED_TWOS_COMPLIMENT byte
     */
    public static RoxByte signedFrom(int value) {
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
    public static RoxByte fromLiteral(int value) {
        return new RoxByte(value & 0xFF, ByteFormat.SIGNED_TWOS_COMPLIMENT);
    }

    /**
     * Create an 8 bit byte from the given {@link byte}
     *
     * @param value an {@link byte} from which to create this byte representation
     * @return a {@link RoxByte} made up from the least significant 8 bits of the given value
     */
    public static RoxByte fromLiteral(byte value) {
        return new RoxByte(value, ByteFormat.SIGNED_TWOS_COMPLIMENT);
    }

    /**
     * Return this single byte value as it's relative Java {@link int} value.
     * This means a single, {@link ByteFormat} SIGNED_TWOS_COMPLIMENT value which is negative will fill out an
     * integer and move the signed bit to the integer msb. i.e.
     *
     * <table cols="3">
     *  <tr>
     *      <th> Value </th>
     *      <th> RoxByte </th>
     *      <td> </td>
     *      <th> Java int </th>
     *  </tr>
     *
     *  <tr>
     *      <TD> 1</TD>
     *      <TD> 00000001 </TD>
     *      <TD> -></TD>
     *      <TD> 00000000000000000000000000000001</TD>
     *  </tr>
     *
     *  <tr>
     *      <TD> 127</TD>
     *      <TD> 01111111</TD>
     *      <TD> -></TD>
     *      <TD> 00000000000000000000000001111111</TD>
     *  </tr>
     *
     *  <tr>
     *      <TD> -1</TD>
     *      <TD> 11111111</TD>
     *      <TD> -></TD>
     *      <TD> 11111111111111111111111111111111</TD>
     *  </tr>
     *
     *  <tr>
     *      <TD> -128</TD>
     *      <TD> 10000000</TD>
     *      <TD> -></TD>
     *      <TD> 11111111111111111111111110000000</TD>
     *  </tr>
     * </table>
     *
     * @return this SIGNED_TWOS_COMPLIMENT byte as an integer
     */
    public int getAsInt() {
        return intFromTwosComplimented(byteValue);
    }

    private int intFromTwosComplimented(int byteValue){
        if (isBitSet(7))
            return -((~(byteValue-1)) & 0xFF);
        else
            return byteValue;
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
        validateBit(bitToSet);
        return new RoxByte(PLACE_VALUE[bitToSet] | this.byteValue, ByteFormat.SIGNED_TWOS_COMPLIMENT);
    }

    /**
     * @param bitToClear bit number (<code>0-7</code>) of the bit to clear in the new {@link RoxByte}
     * @return A new {@link RoxByte} which is this one, with the specified bit cleared
     */
    public RoxByte withoutBit(int bitToClear) {
        validateBit(bitToClear);
        int withoutBit = (~(PLACE_VALUE[bitToClear])) & byteValue;
        return new RoxByte(withoutBit, ByteFormat.SIGNED_TWOS_COMPLIMENT);
    }

    /**
     * @param bitToTest bit number (<code>0-7</code>) of the bit to test
     * @return weather the specified bit is set in this byte
     */
    public boolean isBitSet(int bitToTest) {
        validateBit(bitToTest);
        return (byteValue & PLACE_VALUE[bitToTest]) == PLACE_VALUE[bitToTest];
    }

    private void validateBit(final int bit){
        if ((bit < 0) || (bit > 7))
            throw new ArrayIndexOutOfBoundsException("Bit #"+ bit +" is out of range, expected (0-7)");
    }

    /**
     * @return weather this byte represents a negative number. i.e. it is signed and bit 7 is set
     */
    public boolean isNegative() {
        return isBitSet(7);
    }

    public String toBinaryString(){
        return Integer.toBinaryString(getAsInt());
    }

    public static RoxByte[] fromIntArray(final int[] bytes){
        final RoxByte[] newByteArray = new RoxByte[bytes.length];
        for (int i=0; i<bytes.length; i++){
            newByteArray[i] = RoxByte.fromLiteral(bytes[i]);
        }
        return newByteArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o==null)
            return false;

        if (o instanceof Integer)
            return (byteValue == RoxByte.fromLiteral((Integer)o).byteValue);

        if (getClass() != o.getClass()) return false;
        RoxByte roxByte = (RoxByte) o;
        return byteValue == roxByte.byteValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(byteValue);
    }

    @Override
    public String toString() {
        return "RoxByte{" + getAsInt() + "}";
    }
}
