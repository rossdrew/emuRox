package com.rox.emu;

/**
 * @author rossdrew
 */
public interface Memory {
    void setByte(int location, int byteValue);
    void setMemory(int startLocation, int[] byteValues);
    int getByte(int location);
}
