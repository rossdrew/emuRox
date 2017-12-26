package com.rox.emu.mem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
    }

    @Test
    public void testSimpleGetByte(){
        memoryBlockA.setByteAt(10, 99);

        assertEquals(99, testMemory.getByte(10));
    }

    @Test
    public void testSimplePutByte(){
        testMemory.setByteAt(10, 99);

        assertEquals(99, memoryBlockA.getByte(10));
    }

    @Test
    public void testSimpleGetBlock(){
        int[] sampleData = new int[] {1,2,3,4,5};
        memoryBlockB.setBlock(20, sampleData);

        assertTrue("Expected " + Arrays.toString(sampleData) + ", got " + Arrays.toString(testMemory.getBlock(20, 25)), Arrays.equals( sampleData,  testMemory.getBlock(20, 25)));
    }

    @Test
    public void testSimpleSetBlock(){
        int[] sampleData = new int[] {1,2,3,4,5};
        testMemory.setBlock(20, sampleData);

        assertTrue("Expected " + Arrays.toString(sampleData) + ", got " + Arrays.toString(memoryBlockB.getBlock(20, 25)), Arrays.equals( sampleData,  memoryBlockB.getBlock(20, 25)));
    }

    @Test
    public void testMultipleDestinationReset(){
        final Memory memory1 = mock(Memory.class);
        testMemory = testMemory.withMapping(20, memory1);

        final Memory memory2 = mock(Memory.class);
        testMemory = testMemory.withMapping(30, memory2);
        testMemory = testMemory.withMapping(40, memory2);

        final Memory memory3 = mock(Memory.class);
        testMemory = testMemory.withMapping(50, memory3);
        testMemory = testMemory.withMapping(60, memory3);
        testMemory = testMemory.withMapping(70, memory3);

        testMemory.reset();

        verify(memory1, times(1)).reset();
        verify(memory2, times(1)).reset();
        verify(memory3, times(1)).reset();
    }
}