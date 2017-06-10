package com.rox.emu.mem;

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
    void setByteAt(int location, int byteValue);

    /**
     * @param startLocation to place byte array
     * @param byteValues byte array to place from <code>startLocation</code> to <code>startLocation + byteValues.size</code>
     */
    void setBlock(int startLocation, int[] byteValues);

    /**
     * @param location to query
     * @return the byte value at the given location
     */
    int getByte(int location);

    /**
     * Return two bytes, as a word
     *
     * @param location start location of the word
     * @return the word consisting of two bytes combines into a word, little endian
     */
    int getWord(int location);

    /**
     * Return a block of bytes
     *
     * @param from the required blocks starting memory address
     * @param to the required blocks ending memory address
     * @return the entire block [<code>from</code> ... <code>to</code>] as an array
     */
    int[] getBlock(int from, int to);

    /**
     * Reset all memory to 0
     */
    void reset();
}
