package com.rox.emu.mem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiSourceMemoryTest {
    private Memory memoryBlockA;
    private Memory memoryBlockB;
    private Memory testMemory;

    @Before
    public void setup(){
        memoryBlockA = new SimpleMemory();
        memoryBlockB = new SimpleMemory();
        testMemory = new MultiSourceMemory().with(10, memoryBlockA)
                                            .with(20, memoryBlockB)
                                            .with(21, memoryBlockB)
                                            .with(22, memoryBlockB)
                                            .with(23, memoryBlockB)
                                            .with(24, memoryBlockB)
                                            .with(25, memoryBlockB);
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
}
