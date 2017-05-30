package com.rox.emu.env;

/**
 * A representation of a byte that can be in different formats, so far only SIGNED.
 */
public class RoxByte {
    /**
     * Specifies the format of the information held in a {@link RoxByte} instance, options:-
     *
     * <dl>
     *     <dt>SIGNED</dt>
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
        SIGNED
    }

    private final int byteValue;
    private final ByteFormat format;

    private RoxByte(int value, ByteFormat format){
        this.byteValue = value;
        this.format = format;
    }

    public RoxByte(){
        this(0, ByteFormat.SIGNED);
    }

    public static RoxByte signedFrom(int value) throws Exception {
        if (value > 127 || value < -128)
            throw new Exception("Cannot convert " + value + " to unsigned byte.  Expected range (-128 -> 127)");

        return new RoxByte(value, ByteFormat.SIGNED);
    }

    public int getAsInt() {
        return byteValue;
    }

    public ByteFormat getFormat(){
        return format;
    }
}
