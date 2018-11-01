package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MultiSourceMemoryTest {
    private Memory memoryBlockA;
    private Memory memoryBlockB;
    private MultiSourceMemory testMemory;

    @Before
    public void setup(){
        memoryBlockA = new SimpleMemory();
        memoryBlockB = new SimpleMemory();
        testMemory = new MultiSourceMemory().maintaining(new SimpleMemory())
                                            .withMapping(10, memoryBlockA)
                                            .withMapping(new int[] {20,21,22,23,24,25}, memoryBlockB);
    }

    @Test
    public void testNoMaintainedMemory(){
        final Memory targetMemory = mock(SimpleMemory.class);
        final Memory logicalMemory = new MultiSourceMemory().withMapping(2, targetMemory);

        logicalMemory.getByte(RoxWord.fromLiteral(2));

        try{
            logicalMemory.getByte(RoxWord.fromLiteral(1));
            fail("Expected an exception, there is no memory mapped to address 1");
        }catch(NullPointerException npE){}

        try{
            logicalMemory.reset();
        }catch(Exception e){
            fail("Reset should be clean if there is no maintained memory: " + e.getClass() + " thrown.");
        }
    }

    @Test
    public void testMaintainedMemorySize(){
        assertEquals(65536, testMemory.getSize());
    }

    @Test
    public void testMemorySize(){
        MultiSourceMemory testMemory = new MultiSourceMemory().withMapping(50, mock(Memory.class));
        assertEquals(50, testMemory.getSize());
    }

    @Test
    public void testCombinedMemorySize(){
        MultiSourceMemory largerMaintained = new MultiSourceMemory().maintaining(new SimpleMemory(51))
                                                                    .withMapping(50, mock(Memory.class));
        MultiSourceMemory largerMapped = new MultiSourceMemory().maintaining(new SimpleMemory(49))
                                                                .withMapping(50, mock(Memory.class));

        assertEquals(51, largerMaintained.getSize());
        assertEquals(50, largerMapped.getSize());
    }

    @Test
    public void testGetByte(){
        memoryBlockA.setByteAt(RoxWord.fromLiteral(10), RoxByte.fromLiteral(99));

        assertEquals(RoxByte.fromLiteral(99), testMemory.getByte(RoxWord.fromLiteral(10)));
    }

    @Test
    public void testPutByte(){
        testMemory.setByteAt(RoxWord.fromLiteral(10), RoxByte.fromLiteral(99));

        assertEquals(RoxByte.fromLiteral(99), memoryBlockA.getByte(RoxWord.fromLiteral(10)));
    }

    @Test
    public void testGetBlock(){
        int[] sampleData = new int[] {1,2,3,4,5};
        memoryBlockB.setBlock(RoxWord.fromLiteral(20), toRoxByteArray(sampleData));

        final int[] memoryBlock = toIntArray(testMemory.getBlock(RoxWord.fromLiteral(20), RoxWord.fromLiteral(25)));

        assertTrue("Expected " + Arrays.toString(sampleData) + ", got " + Arrays.toString(memoryBlock), Arrays.equals(sampleData, memoryBlock));
    }

    @Test
    public void testSetBlock(){
        int[] sampleData = new int[] {1,2,3,4,5};
        testMemory.setBlock(RoxWord.fromLiteral(20), toRoxByteArray(sampleData));

        final int[] memoryBlock = toIntArray(testMemory.getBlock(RoxWord.fromLiteral(20), RoxWord.fromLiteral(25)));

        assertTrue("Expected " + Arrays.toString(sampleData) + ", got " + Arrays.toString(memoryBlock), Arrays.equals(sampleData, memoryBlock));
    }

    @Test
    public void testGetWord(){
        memoryBlockB.setBlock(RoxWord.fromLiteral(20), toRoxByteArray(new int[] {1, 20}));

        assertEquals(RoxWord.fromLiteral(276), testMemory.getWord(RoxWord.fromLiteral(20)));
    }

    @Test
    public void testMultipleDestinationReset(){
        final Memory internalMemory = mock(Memory.class);
        MultiSourceMemory testMemory = new MultiSourceMemory().maintaining(internalMemory);

        final Memory memory1 = mock(Memory.class);
        testMemory = testMemory.withMapping(20, memory1);

        final Memory memory2 = mock(Memory.class);
        testMemory = testMemory.withMapping(30, memory2);
        testMemory = testMemory.withMapping(31, memory2);

        final Memory memory3 = mock(Memory.class);
        testMemory = testMemory.withMapping(41, memory3);
        testMemory = testMemory.withMapping(42, memory3);
        testMemory = testMemory.withMapping(43, memory3);

        testMemory.reset();

        verify(memory1, times(1)).reset();
        verify(memory2, times(1)).reset();
        verify(memory3, times(1)).reset();
        verify(internalMemory, times(1)).reset();
    }

    @Test
    public void testControlledLogicalMapping(){
        final Memory physicalMemory = mock(Memory.class);

        MultiSourceMemory logicalMemory = new MultiSourceMemory().withMappingTo(1000, 1, physicalMemory);

        logicalMemory.getByte(RoxWord.fromLiteral(1000));

        verify(physicalMemory, times(1)).getByte(RoxWord.fromLiteral(1));
    }

    @Test
    public void testSecondArrayTooSmall(){
        final Memory memory = mock(Memory.class);

        try {
            new MultiSourceMemory().withMappingTo(new int[]{5, 6, 7, 8, 9}, new int[]{1, 2, 3, 4}, memory);
            fail("Should not be able to map 5 memory addresses to 4");
        }catch(MemoryMappingException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testSecondArrayTooLarge(){
        final Memory memory = mock(Memory.class);

        try {
            new MultiSourceMemory().withMappingTo(new int[]{5, 6, 7, 8}, new int[]{1, 2, 3, 4, 5}, memory);
            fail("Should not be able to map 5 memory addresses to 4");
        }catch(MemoryMappingException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testMirroredMemory(){
        final Memory memory = mock(Memory.class);

        MultiSourceMemory testMemory = new MultiSourceMemory().withMapping(new int[] {1,2,3,4}, memory)
                                                              .withMappingTo(new int[] {5,6,7,8}, new int[] {1,2,3,4}, memory);

        testMemory.getByte(RoxWord.fromLiteral(5));

        testMemory.getByte(RoxWord.fromLiteral(2));
        testMemory.getByte(RoxWord.fromLiteral(6));

        testMemory.getByte(RoxWord.fromLiteral(3));
        testMemory.getByte(RoxWord.fromLiteral(7));
        testMemory.getByte(RoxWord.fromLiteral(7));

        verify(memory, times(1)).getByte(RoxWord.fromLiteral(1));
        verify(memory, times(2)).getByte(RoxWord.fromLiteral(2));
        verify(memory, times(3)).getByte(RoxWord.fromLiteral(3));
    }

    @Test
    public void testMemoryRanges(){
        final Memory memory1 = mock(Memory.class);
        final Memory memory2 = mock(Memory.class);

        final Memory rangedMultiSourceMemory = new MultiSourceMemory().withMapping(0,0, 10, memory1)
                                                                      .withMapping(10, 10, 10, memory2);

        rangedMultiSourceMemory.getByte(RoxWord.ZERO);
        rangedMultiSourceMemory.getByte(RoxWord.fromLiteral(9));
        rangedMultiSourceMemory.getByte(RoxWord.fromLiteral(10));
        rangedMultiSourceMemory.getByte(RoxWord.fromLiteral(19));
        //XXX What do we expect with 'rangedMultiSourceMemory.getByte(RoxWord.fromLiteral(20));'?

        verify(memory1, times(1)).getByte(RoxWord.ZERO);
        verify(memory1, times(1)).getByte(RoxWord.fromLiteral(9));
        verify(memory2, times(1)).getByte(RoxWord.fromLiteral(10));
        verify(memory2, times(1)).getByte(RoxWord.fromLiteral(19));

    }

    private int[] toIntArray(RoxByte[] byteArray) {
        int i=0;
        final int[] result = new int[byteArray.length];
        for (RoxByte roxByte : byteArray) {
            result[i++] = roxByte.getRawValue();
        }
        return result;
    }

    private RoxByte[] toRoxByteArray(int[] byteArray) {
        int i=0;
        final RoxByte[] result = new RoxByte[byteArray.length];
        for (int intByte : byteArray) {
            result[i++] = RoxByte.fromLiteral(intByte);
        }
        return result;
    }
}
