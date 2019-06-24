package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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
    public void setByteAt(RoxWord location, RoxByte byteValue) {
        log.trace("mem[{}] << {}", location, byteValue);
        memoryArray[location.getRawValue()] = byteValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlock(RoxWord startLocation, RoxByte[] byteValues) {
        log.trace("mem[{}] << {} bytes", startLocation, byteValues.length);
        System.arraycopy(byteValues, 0, memoryArray, startLocation.getRawValue(), byteValues.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoxByte getByte(RoxWord location) {
        log.trace("mem[{}] >> {}", location, memoryArray[location.getRawValue()]);

        return memoryArray[location.getRawValue()];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoxWord getWord(RoxWord location) {
        RoxWord word = RoxWord.from(memoryArray[location.getRawValue()], memoryArray[location.getRawValue()+1]);
        log.trace("mem[{}] >> {}", location, word.getRawValue());
        return word;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoxByte[] getBlock(RoxWord from, RoxWord to) {
        RoxByte[] extractedData = new RoxByte[to.getRawValue()-from.getRawValue()];
        System.arraycopy(memoryArray, from.getRawValue(), extractedData, 0, extractedData.length);
        return extractedData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        Arrays.fill(memoryArray, RoxByte.ZERO);
    }

    @Override
    public int getSize() {
        return memoryArray.length;
    }
}
