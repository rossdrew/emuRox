package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;

import java.util.Arrays;

/**
 * A block of read only memory, writes will throw a {@link MemoryMappingException} and resetting will have no effect.
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
    public void setByteAt(RoxWord location, RoxByte byteValue) {
        throw new MemoryMappingException("Cannot write to read only memory");
    }

    @Override
    public void setBlock(RoxWord startLocation, RoxByte[] byteValues) {
        throw new MemoryMappingException("Cannot write to read only memory");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoxByte getByte(RoxWord location) {
        return memoryArray[location.getRawValue()];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoxWord getWord(RoxWord location) {
        return RoxWord.from(memoryArray[location.getRawValue()], memoryArray[location.getRawValue()+1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoxByte[] getBlock(RoxWord from, RoxWord to) {
        return Arrays.copyOfRange(memoryArray, from.getRawValue(), to.getRawValue());
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
