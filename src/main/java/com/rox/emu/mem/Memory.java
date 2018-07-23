package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;

/**
 * An interface to any "memory" store in which data can
 * be saved and retrieved from specific locations.
 *
 * @author Ross Drew
 */
public interface Memory {
    /**
     * @param location where to place the given byte
     * @param byteValue to place in that location
     */
    void setByteAt(RoxWord location, RoxByte byteValue);

    /**
     * @param startLocation to place byte array
     * @param byteValues byte array to place from <code>startLocation</code> to <code>startLocation + byteValues.size</code>
     */
    void setBlock(RoxWord startLocation, RoxByte[] byteValues);

    /**
     * @param location to query
     * @return the byte value at the given location
     */
    RoxByte getByte(RoxWord location);

    /**
     * Return two bytes, as a word
     *
     * @param location start location of the word
     * @return the word consisting of two bytes combines into a word, little endian
     */
    RoxWord getWord(RoxWord location);

    /**
     * Return a block of bytes
     *
     * @param from the required blocks starting memory address
     * @param to the required blocks ending memory address
     * @return the entire block [<code>from</code> ... <code>to</code>] as an array
     */
    RoxByte[] getBlock(RoxWord from, RoxWord to);

    /**
     * Reset all memory to 0
     */
    void reset();

    /**
     * @return the number of blocks in this memory
     */
    int getSize();
}
