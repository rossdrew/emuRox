package com.rox.emu.mem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class SimpleMemoryTest {
    private Memory memory;

    @Before
    public void setUp(){
        memory = new SimpleMemory();
    }

    @Test
    public void testExplicitlySizedMemory(){
        final Memory sizedMemory = new SimpleMemory(10);

        assert Arrays.equals(sizedMemory.getBlock(0, 10), new int[] {0,0,0,0,0,0,0,0,0,0});

        sizedMemory.setByteAt(0, 1);
        sizedMemory.setByteAt(9, 10);

        assertEquals(10, sizedMemory.getSize());
        try {
            sizedMemory.setByteAt(10, 0);
            fail("Should not be able to access memory outside the size of addressable memory");
        }catch(ArrayIndexOutOfBoundsException e){}
    }

    @Test
    public void testGetAndSetByte(){
        memory.setByteAt(0, 99);
        memory.setByteAt(0x10, 101);
        memory.setByteAt(0b00000001, 1);

        assertEquals(99, memory.getByte(0));
        assertEquals(101, memory.getByte(16));
        assertEquals(1, memory.getByte(1));
    }

    @Test
    public void testGetWord(){
        memory.setByteAt(0, 0x01);
        memory.setByteAt(1, 0x01);

        assertEquals(257, memory.getWord(0));
        assertEquals(256, memory.getWord(1));
        assertEquals(0, memory.getWord(2));
    }

    @Test
    public void testGetBlock(){
        memory.setBlock(0, new int [] {0,1,2,3,4,5,6,7,8,9});

        int offset = 5;
        int [] retrievedBlock = memory.getBlock(offset,9);
        for (int i=0; i<retrievedBlock.length; i++){
            assertEquals(i+offset, retrievedBlock[i]);
        }
    }

    @Test
    public void testSetMemory(){
        int[] values = new int[] {1,22,33,44,55};
        memory.setBlock(0, values);

        assertEquals(65536, memory.getSize());

        assertEquals(1, memory.getByte(0));
        assertEquals(22, memory.getByte(1));
        assertEquals(33, memory.getByte(2));
        assertEquals(44, memory.getByte(3));
        assertEquals(55, memory.getByte(4));
    }

    @Test
    public void testClearMemory(){
        memory.setBlock(0, new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20});
        memory.reset();
        for (int i=0; i<30; i++){
            assertEquals(0, memory.getByte(i));
        }
    }

}
