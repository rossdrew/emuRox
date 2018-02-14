package com.rox.emu.mem;

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

        logicalMemory.getByte(2);

        try{
            logicalMemory.getByte(1);
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
        memoryBlockA.setByteAt(10, 99);

        assertEquals(99, testMemory.getByte(10));
    }

    @Test
    public void testPutByte(){
        testMemory.setByteAt(10, 99);

        assertEquals(99, memoryBlockA.getByte(10));
    }

    @Test
    public void testGetBlock(){
        int[] sampleData = new int[] {1,2,3,4,5};
        memoryBlockB.setBlock(20, sampleData);

        assertTrue("Expected " + Arrays.toString(sampleData) + ", got " + Arrays.toString(testMemory.getBlock(20, 25)), Arrays.equals( sampleData,  testMemory.getBlock(20, 25)));
    }

    @Test
    public void testSetBlock(){
        int[] sampleData = new int[] {1,2,3,4,5};
        testMemory.setBlock(20, sampleData);

        assertTrue("Expected " + Arrays.toString(sampleData) + ", got " + Arrays.toString(memoryBlockB.getBlock(20, 25)), Arrays.equals( sampleData,  memoryBlockB.getBlock(20, 25)));
    }

    @Test
    public void testGetWord(){
        memoryBlockB.setBlock(20, new int[] {1, 20});

        assertEquals(276, testMemory.getWord(20));
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

        logicalMemory.getByte(1000);

        verify(physicalMemory, times(1)).getByte(1);
    }

    @Test
    public void testMappingArraysAreOfEqualSize1(){
        final Memory memory = mock(Memory.class);

        try {
            new MultiSourceMemory().withMappingTo(new int[]{5, 6, 7, 8, 9}, new int[]{1, 2, 3, 4}, memory);
            fail("Should not be able to map 5 memory addresses to 4");
        }catch(RuntimeException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testMappingArraysAreOfEqualSize2(){
        final Memory memory = mock(Memory.class);

        try {
            new MultiSourceMemory().withMappingTo(new int[]{5, 6, 7, 8}, new int[]{1, 2, 3, 4, 5}, memory);
            fail("Should not be able to map 5 memory addresses to 4");
        }catch(RuntimeException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testMirroredMemory(){
        final Memory memory = mock(Memory.class);

        MultiSourceMemory testMemory = new MultiSourceMemory().withMapping(new int[] {1,2,3,4}, memory)
                                                              .withMappingTo(new int[] {5,6,7,8}, new int[] {1,2,3,4}, memory);

        testMemory.getByte(5);

        testMemory.getByte(2);
        testMemory.getByte(6);

        testMemory.getByte(3);
        testMemory.getByte(7);
        testMemory.getByte(7);

        verify(memory, times(1)).getByte(1);
        verify(memory, times(2)).getByte(2);
        verify(memory, times(3)).getByte(3);
    }
}
