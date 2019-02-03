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
        final RoxByte[] extractedData = new RoxByte[(to.getRawValue()+1) - from.getRawValue()];
        for (int i=0; i<extractedData.length; i++){
            extractedData[i] = RoxByte.fromLiteral(memoryArray[from.getRawValue() + i].getRawValue());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMemory that = (SimpleMemory) o;

        System.out.println("A: " + memoryArray.length + ", B: " + this.memoryArray.length);

        return Arrays.equals(memoryArray, that.memoryArray);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(memoryArray);
    }
}
