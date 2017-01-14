package com.rox.emu;

/**
 * Simple array representing memory, implementing the memory interface
 *
 * @author Ross Drew
 */
public class SimpleMemory implements Memory{
    private final int[] memoryArray;

    public SimpleMemory(int size){
        memoryArray = new int[size];
    }

    @Override
    public void setByteAt(int location, int byteValue) {
        memoryArray[location] = byteValue & 0xFF;
    }

    @Override
    public void setMemory(int startLocation, int[] byteValues) {
        System.arraycopy(byteValues, 0, memoryArray, startLocation, byteValues.length);
    }

    @Override
    public int getByte(int location) {
        return memoryArray[location];
    }
}
