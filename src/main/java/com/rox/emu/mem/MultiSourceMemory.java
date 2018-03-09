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

    /**
     *
     * @param logicalAddress a logical address to map
     * @param physicalAddress a physical address to map the logical address to
     * @param mappedMemory the "physical" memory to map to
     * @return A new {@link MultiSourceMemory} with the specified mapping
     */
    public MultiSourceMemory withMappingTo(final Integer logicalAddress, final Integer physicalAddress, final Memory mappedMemory){
        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>();
        newMemoryMappings.putAll(memoryMappings);
        newMemoryMappings.put(logicalAddress, new MemoryMapping(logicalAddress, physicalAddress, mappedMemory));

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

    /**
     * Create a copy of this {@link Memory} mapping with the addresses specified, with the logical addresses mapped
     * to the corresponding physical addresses of the provided {@link Memory}
     * @param logicalAddresses array of logical addresses
     * @param physicalAddresses array of physical addresses to map <code>logicalAddresses</code> to, in the same order
     * @param memory the "physical" memory to map to
     * @return A new {@link MultiSourceMemory} with the specified mappings
     */
    public MultiSourceMemory withMappingTo(final int[] logicalAddresses,
                                           final int[] physicalAddresses,
                                           final Memory memory) {
        if (logicalAddresses.length != physicalAddresses.length)
            throw new RuntimeException("The same number of logical and physical addresses are required for multiple mappings.  Received " + logicalAddresses.length + " logical addresses to " + physicalAddresses.length + " physical.");

        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>();
        newMemoryMappings.putAll(memoryMappings);

        for (int a=0; a<logicalAddresses.length; a++){
            MemoryMapping newMapping = new MemoryMapping(logicalAddresses[a], physicalAddresses[a], memory);
            newMemoryMappings.put(logicalAddresses[a], newMapping);
        }

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

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
        for (Map.Entry<Integer, MemoryMapping> mappingEntry : memoryMappings.entrySet()) {
            final Memory memory = mappingEntry.getValue().physicalMemory;
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
