package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;

import java.util.Arrays;

/**
 * A block of read only memory, writes will throw a {@link RuntimeException} and resetting will have no effect.
 */
public class ReadOnlyMemory implements Memory {
    private final RoxByte[] memoryArray;

    public ReadOnlyMemory(final int[] contents){
        memoryArray = new RoxByte[contents.length];
        for (int memoryIndex = 0; memoryIndex < memoryArray.length; memoryIndex++)
            memoryArray[memoryIndex] = RoxByte.fromLiteral(contents[memoryIndex]);
    }

    public ReadOnlyMemory(final byte[] contents){
        memoryArray = new RoxByte[contents.length];
        for (int memoryIndex = 0; memoryIndex < memoryArray.length; memoryIndex++)
            memoryArray[memoryIndex] = RoxByte.fromLiteral(contents[memoryIndex]);
    }

    @Override
    public void setByteAt(int location, int byteValue) {
        throw new RuntimeException("Cannot write to read only memory");
    }

    @Override
    public void setBlock(int startLocation, int[] byteValues) {
        throw new RuntimeException("Cannot write to read only memory");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getByte(int location) {
        return memoryArray[location].getRawValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWord(int location) {
        int word = (memoryArray[location].getRawValue() << 8 | memoryArray[location+1].getRawValue());
        return word;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getBlock(int from, int to) {
        final RoxByte[] roxBytes = Arrays.copyOfRange(memoryArray, from, to);
        int[] extractedData = new int[roxBytes.length];
        for (int i=0; i<extractedData.length; i++){
            extractedData[i] = roxBytes[i].getRawValue();
        }
        return extractedData;
    }

    @Override
    public void reset() {
        //Does nothing as this is read only memory
    }

    @Override
    public int getSize() {
        return memoryArray.length;
    }
}
