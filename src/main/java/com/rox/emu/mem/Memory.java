package com.rox.emu.mem;

/**
 * An interface to any "memory" store in which data can
 * be saved and retrieved from specific locations.
 *
 * @author Ross Drew
 */
public interface Memory {
    void setByteAt(int location, int byteValue);
    void setMemory(int startLocation, int[] byteValues);
    int getByte(int location);
    int getWord(int location);
    int[] getBlock(int from, int to);
    void reset();
}