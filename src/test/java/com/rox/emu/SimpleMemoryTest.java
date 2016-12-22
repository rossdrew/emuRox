package com.rox.emu;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class SimpleMemoryTest {
    private Memory memory;

    @Before
    public void setup(){
        memory = new SimpleMemory(65534);
    }

    @Test
    public void testGetAndSetByte(){
        memory.setByte(0, 99);
        memory.setByte(0x10, 101);
        memory.setByte(0b00000001, 1);

        assertEquals(99, memory.getByte(0));
        assertEquals(101, memory.getByte(16));
        assertEquals(1, memory.getByte(1));
    }

    @Test
    public void testSetMemory(){
        int[] values = new int[] {1,22,33,44,55};
        memory.setMemory(0, values);

        assertEquals(1, memory.getByte(0));
        assertEquals(22, memory.getByte(1));
        assertEquals(33, memory.getByte(2));
        assertEquals(44, memory.getByte(3));
        assertEquals(55, memory.getByte(4));
    }

}
