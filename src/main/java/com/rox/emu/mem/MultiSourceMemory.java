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
        defaultMemory = null;
    }

    private MultiSourceMemory(final Memory defaultMemory, final Map<Integer, Memory> memoryMappings){
        this.memoryMappings = memoryMappings;
        this.defaultMemory = defaultMemory;
    }

    /**
     * @param internalMemory {@link Memory} which to link all addresses to
     * @return a {@link MultiSourceMemory} whose addresses are all mapped to the given {@link Memory}
     */
    public MultiSourceMemory maintaining(final Memory internalMemory){
        return new MultiSourceMemory(internalMemory, this.memoryMappings);
    }

    /**
     * Create a copy of this {@link Memory} mapping with the address specified, mapped to the provided {@link Memory}
     *
     * @param address to map
     * @param mappedMemory on which to map the address
     * @return a new {@link MultiSourceMemory} with the new mapping
     */
    public MultiSourceMemory withMapping(final Integer address, final Memory mappedMemory){
        final Map<Integer, Memory> newMemoryMappings = new HashMap<>();
        newMemoryMappings.putAll(memoryMappings);
        newMemoryMappings.put(address, mappedMemory);

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

    /**
     * Create a copy of this {@link Memory} mapping with the addresses specified, mapped to the provided {@link Memory}
     * @param addresses to map
     * @param mappedMemory on which to map the address
     * @return a new {@link MultiSourceMemory} with the new mapping
     */
    public MultiSourceMemory withMapping(final int[] addresses, final Memory mappedMemory){
        final Map<Integer, Memory> newMemoryMappings = new HashMap<>();
        newMemoryMappings.putAll(memoryMappings);
        for (Integer address : addresses) {
            newMemoryMappings.put(address, mappedMemory);
        }

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

    private Memory getMemoryMappedTo(final Integer address){
        return memoryMappings.getOrDefault(address, defaultMemory);
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

        if (defaultMemory != null)
            defaultMemory.reset();
    }

    @Override
    public int getSize() {
        return defaultMemory.getSize(); //XXX
    }
}
