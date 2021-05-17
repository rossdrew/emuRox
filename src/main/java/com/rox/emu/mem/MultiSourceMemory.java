package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A partially logical block of memory in which certain blocks can be assigned (memory mapped) to other memory objects
 */
public class MultiSourceMemory implements Memory {

    /**
     * Representation of a memory mapping, consisting of a destination memory, a logical memory location that can be
     * logically accessed and a physical memory location that will eventually be "physically" accessed.
     */
    private class MemoryMapping {
        final int logicalAddress;
        final int physicalAddress;
        final Memory physicalMemory;

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
        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>(memoryMappings);
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
        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>(memoryMappings);

        for (Integer address : addresses) {
            newMemoryMappings.put(address, new MemoryMapping(address, address, mappedMemory));
        }

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

    /**
     * Create a copy of this {@link Memory} mapping with the address range specified, mapped to the provided {@link Memory}
     *
     * @param fromAddress the logical start location to map this memory to
     * @param toAddress the physical start location to map this memory to
     * @param size the size of the range from the start
     * @param mappedMemory the physical memory to map
     * @return a new {@link MultiSourceMemory} with the new mapping
     */
    public MultiSourceMemory withMapping(final int fromAddress,
                                         final int toAddress,
                                         final int size,
                                         final Memory mappedMemory){
        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>(memoryMappings);

        for (int from=fromAddress, to=toAddress; from<(fromAddress+size); from++, to++){
            newMemoryMappings.put(from, new MemoryMapping(from, to, mappedMemory));
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
        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>(memoryMappings);

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
            throw new MemoryMappingException("The same number of logical and physical addresses are required for multiple mappings.  Received " + logicalAddresses.length + " logical addresses to " + physicalAddresses.length + " physical.");

        final Map<Integer, MemoryMapping> newMemoryMappings = new HashMap<>(memoryMappings);

        for (int logicalAddress=0; logicalAddress<logicalAddresses.length; logicalAddress++){
            MemoryMapping newMapping = new MemoryMapping(logicalAddresses[logicalAddress], physicalAddresses[logicalAddress], memory);
            newMemoryMappings.put(logicalAddresses[logicalAddress], newMapping);
        }

        return new MultiSourceMemory(defaultMemory, newMemoryMappings);
    }

    private MemoryMapping getMemoryMappedTo(final Integer address){
        return memoryMappings.getOrDefault(address, new MemoryMapping(address,address, defaultMemory));
    }

    @Override
    public void setByteAt(RoxWord location, RoxByte byteValue) {
        final MemoryMapping mappedMemory = getMemoryMappedTo(location.getRawValue());
        mappedMemory.physicalMemory.setByteAt(RoxWord.fromLiteral(mappedMemory.physicalAddress), byteValue);
    }

    @Override
    public void setBlock(RoxWord startLocation, RoxByte[] byteValues) {
        //In case a block crosses multiple unique memory mapped blocks
        int address = startLocation.getRawValue();
        for (int i=0; i < byteValues.length; i++, address++){
            final MemoryMapping mappedMemory = getMemoryMappedTo(address);
            mappedMemory.physicalMemory.setByteAt(RoxWord.fromLiteral(mappedMemory.physicalAddress), byteValues[i]);
        }
    }

    @Override
    public RoxByte getByte(RoxWord location) {
        final MemoryMapping mappedMemory = getMemoryMappedTo(location.getRawValue());
        return mappedMemory.physicalMemory.getByte(RoxWord.fromLiteral(mappedMemory.physicalAddress));
    }

    @Override
    public RoxWord getWord(RoxWord location) {
        RoxByte byteA = getByte(location);
        RoxByte byteB = getByte(RoxWord.fromLiteral(location.getRawValue() + 1));

        return RoxWord.from(byteA, byteB);
    }

    @Override
    public RoxByte[] getBlock(RoxWord from, RoxWord to) {
        int blockSize = to.getRawValue() - from.getRawValue();
        RoxByte[] block = new RoxByte[blockSize];

        //In case a block crosses multiple unique memory mapped blocks
        for (int i=0; i<blockSize; i++){
            RoxWord address = RoxWord.fromLiteral(from.getRawValue() + i);
            block[i] = getByte(address);
        }
        return block;
    }

    @Override
    public void reset() {
        //For each unique memory object, reset it
        memoryMappings.entrySet().stream()
                .map(entry -> entry.getValue().physicalMemory)
                .distinct()
                .forEach(Memory::reset);

        if (defaultMemory != null)
            defaultMemory.reset();
    }

    @Override
    public int getSize() {
        //XXX Should the size be the largest addressable byte or should it be the addressable byte count?!
        int maintainedMemorySize = (defaultMemory != null) ? defaultMemory.getSize() : 0;
        int maxMappedAddress = memoryMappings.keySet().stream().max(Comparator.comparing(i -> i)).orElse(0);
        maintainedMemorySize = Math.max(maintainedMemorySize, maxMappedAddress);
        return maintainedMemorySize;
    }
}