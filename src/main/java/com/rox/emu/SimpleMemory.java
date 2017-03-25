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
        System.out.println("STORE mem[" + location + "] --> " + byteValue);
        memoryArray[location] = byteValue & 0xFF;
    }

    @Override
    public void setMemory(int startLocation, int[] byteValues) {
        System.out.println("STORE mem[" + startLocation + "] --> " + byteValues.length + " bytes");
        System.arraycopy(byteValues, 0, memoryArray, startLocation, byteValues.length);
    }

    @Override
    public int getByte(int location) {
        System.out.println("FETCH mem[" + location +"] --> " + memoryArray[location]);
        return memoryArray[location];
    }

    @Override
    public int getWord(int location) {
        int word = (memoryArray[location] << 8 | memoryArray[location+1]);
        System.out.println("FETCH mem[" + location +"] --> " + word);
        return word;
    }
}
