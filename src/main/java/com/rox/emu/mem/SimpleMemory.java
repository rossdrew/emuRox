package com.rox.emu.mem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple array representing memory, implementing the memory interface
 *
 * @author Ross Drew
 */
public class SimpleMemory implements Memory {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    
    private final int[] memoryArray;

    public SimpleMemory(){
        memoryArray = new int[0x10000];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setByteAt(int location, int byteValue) {
        LOG.trace("STORE mem[" + location + "] --> " + byteValue);
        memoryArray[location] = byteValue & 0xFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlock(int startLocation, int[] byteValues) {
        LOG.trace("STORE mem[" + startLocation + "] --> " + byteValues.length + " bytes");
        System.arraycopy(byteValues, 0, memoryArray, startLocation, byteValues.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getByte(int location) {
        LOG.trace("FETCH mem[" + location +"] --> " + memoryArray[location]);
        return memoryArray[location];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWord(int location) {
        int word = (memoryArray[location] << 8 | memoryArray[location+1]);
        LOG.trace("FETCH mem[" + location +"] --> " + word);
        return word;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getBlock(int from, int to) {
        int[] extractedData = new int[to-from];
        System.arraycopy(memoryArray, from, extractedData, 0, extractedData.length);
        return extractedData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        for (int i : memoryArray) {
            memoryArray[i] = 0;
        }
    }
}
