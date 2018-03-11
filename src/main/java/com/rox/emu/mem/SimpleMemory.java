package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple array representing memory, implementing the memory interface
 *
 * @author Ross Drew
 */
public class SimpleMemory implements Memory {
    private static final Logger log = LoggerFactory.getLogger(SimpleMemory.class);

    private final RoxByte[] memoryArray;

    public SimpleMemory(){
        memoryArray = new RoxByte[0x10000];
        reset();
    }

    public SimpleMemory(int size){
        memoryArray = new RoxByte[size];
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setByteAt(int location, int byteValue) {
        if (log.isTraceEnabled()) log.trace("mem[{}] << {}", location, byteValue);
        memoryArray[location] = RoxByte.fromLiteral(byteValue & 0xFF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlock(int startLocation, int[] byteValues) {
        if (log.isTraceEnabled()) log.trace("mem[{}] << {} bytes", startLocation, byteValues.length);
        for (int i=0; i<byteValues.length; i++){
            memoryArray[startLocation + i] = RoxByte.fromLiteral(byteValues[i]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getByte(int location) {
        if (log.isTraceEnabled()) log.trace("mem[{}] >> {}", location, memoryArray[location]);

        return memoryArray[location].getRawValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWord(int location) {
        int word = (memoryArray[location].getRawValue() << 8 | memoryArray[location+1].getRawValue());
        if (log.isTraceEnabled()) log.trace("mem[{}] >> {}", location, word);
        return word;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getBlock(int from, int to) {
        int[] extractedData = new int[to-from];
        for (int i=0; i<extractedData.length; i++){
            extractedData[i] = memoryArray[from + i].getRawValue();
        }
        return extractedData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        for (int i=0; i<memoryArray.length; i++)
            memoryArray[i] = RoxByte.ZERO;
    }

    @Override
    public int getSize() {
        return memoryArray.length;
    }
}
