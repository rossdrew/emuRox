package com.rox.emu.mem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A partially logical block of memory in which certain blocks can be assigned (memory mapped) to other memory objects
 */
public class MultiSourceMemory implements Memory {

    private final Map<Integer, Memory> memoryMappings;
    private final Memory defaultMemory;

    public MultiSourceMemory(){
        memoryMappings = new HashMap<>();
        defaultMemory = new SimpleMemory();
    }

    private MultiSourceMemory(final Memory defaultMemory, final Map<Integer, Memory> memoryMappings){
        this.memoryMappings = memoryMappings;
        this.defaultMemory = defaultMemory;
    }

    public MultiSourceMemory with(final Integer address, final Memory mappedMemory){
        final Map<Integer, Memory> newMemoryMappings = new HashMap<>();
        newMemoryMappings.putAll(memoryMappings);
        newMemoryMappings.put(address, mappedMemory);

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

    private Memory getMemoryMappedTo(final Integer address){
        if (memoryMappings.containsKey(address)){
            return memoryMappings.get(address);
        }else{
            return defaultMemory;
        }
    }

    @Override
    public void setByteAt(int location, int byteValue) {
        getMemoryMappedTo(location).setByteAt(location, byteValue);
    }

    @Override
    public void setBlock(int startLocation, int[] byteValues) {
        //In case a block crosses multiple unique memory mapped blocks
        for (int i=0; i < byteValues.length; i++){
            int address = startLocation + i;
            getMemoryMappedTo(address).setByteAt(address, byteValues[i]);
        }
    }

    @Override
    public int getByte(int location) {
        return getMemoryMappedTo(location).getByte(location);
    }

    @Override
    public int getWord(int location) {
        int byteA = getMemoryMappedTo(location).getByte(location);
        int byteB = getMemoryMappedTo(location + 1).getByte(location + 1);

        return byteA << 8 | byteB;
    }

    @Override
    public int[] getBlock(int from, int to) {
        int blockSize = to - from;
        int[] block = new int[blockSize];

        //In case a block crosses multiple unique memory mapped blocks
        for (int i=0; i<blockSize; i++){
            int address = from + i;
            block[i] = getMemoryMappedTo(address).getByte(address);
        }
        return block;
    }

    @Override
    public void reset() {
        //For each unique memory object, reset it
        List<Memory> reset = new ArrayList<>();
        for (Integer mappedAddress : memoryMappings.keySet()) {
            final Memory memory = memoryMappings.get(mappedAddress);
            if (!reset.contains(memory)){
                memory.reset();
                reset.add(memory);
            }
        }

        defaultMemory.reset();
    }
}
