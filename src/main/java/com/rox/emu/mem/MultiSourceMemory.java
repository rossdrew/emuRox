package com.rox.emu.mem;

import java.util.*;

/**
 * A partially logical block of memory in which certain blocks can be assigned (memory mapped) to other memory objects
 */
public class MultiSourceMemory implements Memory {

    /**
     * Representation of a memory mapping, consisting of a destination memory, a logical memory location that can be
     * logically accessed and a physical memory location that will eventually by "physically" accessed.
     */
    private class MemoryMapping {
        public final int logicalAddress;
        public final int physicalAddress;
        public final Memory physicalMemory;

        private MemoryMapping(final int logicalAddress,
                              final int physicalAddress,
                              final Memory physicalMemory){
            this.logicalAddress=logicalAddress;
            this.physicalAddress=physicalAddress;
            this.physicalMemory=physicalMemory;
        }
    }

    private final Map<Integer, MemoryMapping> memoryMappings;
    private final Memory defaultMemory;

    public MultiSourceMemory(){
        memoryMappings = new HashMap<>();
        defaultMemory = null;
    }

    private MultiSourceMemory(final Memory defaultMemory, final Map<Integer, MemoryMapping> memoryMappings){
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
        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>();
        newMemoryMappings.putAll(memoryMappings);
        newMemoryMappings.put(address, new MemoryMapping(address,address,mappedMemory));

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

    /**
     * Create a copy of this {@link Memory} mapping with the addresses specified, mapped to the provided {@link Memory}
     * @param addresses to map
     * @param mappedMemory on which to map the address
     * @return a new {@link MultiSourceMemory} with the new mapping
     */
    public MultiSourceMemory withMapping(final int[] addresses, final Memory mappedMemory){
        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>();
        newMemoryMappings.putAll(memoryMappings);
        for (Integer address : addresses) {
            newMemoryMappings.put(address, new MemoryMapping(address, address, mappedMemory));
        }

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

//    public MultiSourceMemory withMappingTo(final int[] logicalAddress,
//                                           final int[] physicalAddress,
//                                           final Memory memory) {
//        assert logicalAddress.length == physicalAddress.length : "Logical and physical address count must match";
//        //TODO
//        return null;
//    }

    private MemoryMapping getMemoryMappedTo(final Integer address){
        return memoryMappings.getOrDefault(address, new MemoryMapping(address,address, defaultMemory));
    }

    @Override
    public void setByteAt(int location, int byteValue) {
        final MemoryMapping mappedMemory = getMemoryMappedTo(location);
        mappedMemory.physicalMemory.setByteAt(mappedMemory.physicalAddress, byteValue);
    }

    @Override
    public void setBlock(int startLocation, int[] byteValues) {
        //In case a block crosses multiple unique memory mapped blocks
        for (int i=0; i < byteValues.length; i++){
            int address = startLocation + i;
            final MemoryMapping mappedMemory = getMemoryMappedTo(address);
            mappedMemory.physicalMemory.setByteAt(mappedMemory.physicalAddress, byteValues[i]);
        }
    }

    @Override
    public int getByte(int location) {
        final MemoryMapping mappedMemory = getMemoryMappedTo(location);
        return mappedMemory.physicalMemory.getByte(mappedMemory.physicalAddress);
    }

    @Override
    public int getWord(int location) {
        int byteA = getByte(location);
        int byteB = getByte(location + 1);

        return byteA << 8 | byteB;
    }

    @Override
    public int[] getBlock(int from, int to) {
        int blockSize = to - from;
        int[] block = new int[blockSize];

        //In case a block crosses multiple unique memory mapped blocks
        for (int i=0; i<blockSize; i++){
            int address = from + i;
            block[i] = getByte(address);
        }
        return block;
    }

    @Override
    public void reset() {
        //For each unique memory object, reset it
        List<Memory> reset = new ArrayList<>();
        for (Integer mappedAddress : memoryMappings.keySet()) {
            final Memory memory = memoryMappings.get(mappedAddress).physicalMemory;
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
        //XXX Should the size be the largest addressable byte or should it be the addressable byte count?!
        int maintainedMemorySize = (defaultMemory != null) ? defaultMemory.getSize() : 0;
        int maxMappedAddress = memoryMappings.keySet().stream().max(Comparator.comparing(i -> i)).get();
        maintainedMemorySize = Math.max(maintainedMemorySize, maxMappedAddress);
        return maintainedMemorySize;
    }
}
