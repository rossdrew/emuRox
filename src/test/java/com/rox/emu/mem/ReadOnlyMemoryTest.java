package com.rox.emu.mem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class ReadOnlyMemoryTest {
    private Memory memory;

    @Before
    public void setUp(){
        memory = new ReadOnlyMemory(new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19});
    }

    @Test
    public void testExplicitlySizedMemory(){
        try {
            memory.setByteAt(10, 0);
            fail("Should not be able to access memory outside the size of addressable memory");
        }catch(RuntimeException e){}
    }

    //TODO test getting bytes
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
    //TODO test reset

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
