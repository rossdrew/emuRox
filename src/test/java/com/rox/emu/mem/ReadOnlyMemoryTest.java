package com.rox.emu.mem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class ReadOnlyMemoryTest {
    int[] memoryValues = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
    private Memory memory;

    @Before
    public void setUp(){
        memory = new ReadOnlyMemory(memoryValues);
    }

    @Test
    public void testByteCreatedMemory(){
        final Memory byteMemory = new ReadOnlyMemory(new byte[] {0x10, 0x20, 0x30, 0x40, 0x50});
        assertEquals(0x20, byteMemory.getByte(1));

        try{
            byteMemory.setByteAt(0, 42);
            fail("Writing to read only memory should throw an exception.");
        }catch(RuntimeException e){}
    }

    @Test
    public void testExplicitlySizedMemory(){
        assertEquals(20, memory.getSize());
        try {
            memory.setByteAt(10, 0);
            fail("Should not be able to access memory outside the size of addressable memory");
        }catch(RuntimeException e){}
    }

    @Test
    public void testRead(){
        assertEquals(5, memory.getByte(5));
    }

    @Test
    public void testReadBlock(){
        int[] expected = new int[] {0,1,2,3};
        int[] actual = memory.getBlock(0, 4);
        assertTrue("Expected " + Arrays.toString(expected) + ", got " +  Arrays.toString(actual), Arrays.equals(expected, actual));
    }

    @Test
    public void testReadBlockWithZeroSize(){
        int[] expected = new int[] {};
        int[] actual = memory.getBlock(0, 0);
        assertTrue("Expected " + Arrays.toString(expected) + ", got " +  Arrays.toString(actual), Arrays.equals(expected, actual));
    }

    @Test
    public void testReadBlockWithNegativeSize(){
        int[] expected = new int[] {};
        int[] actual = memory.getBlock(4, 0);
        assertTrue("Expected " + Arrays.toString(expected) + ", got " +  Arrays.toString(actual), Arrays.equals(expected, actual));
    }

    @Test
    public void testReset(){
        int[] actual = memory.getBlock(0, 20);
        assertTrue("Expected " + Arrays.toString(memoryValues) + ", got " + Arrays.toString(actual),Arrays.equals(memoryValues, actual));
    }

    @Test
    public void testReadWord(){
        assertEquals(((1<<8) | 2), memory.getWord(1));
    }

    @Test
    public void testWrite(){
        try {
            memory.setByteAt(0, 0);
            fail("Writing to read only memory should throw an exception.");
        }catch (RuntimeException e){}
    }

    @Test
    public void testWriteBlock(){
        try {
            memory.setBlock(0, new int[] {0,1,2,3,4,5});
            fail("Writing to read only memory should throw an exception.");
        }catch (RuntimeException e){}
    }
}
