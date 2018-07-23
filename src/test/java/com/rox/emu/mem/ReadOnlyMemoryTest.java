package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
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
        assertEquals(RoxByte.fromLiteral(0x20), byteMemory.getByte(RoxWord.fromLiteral(1)));

        try{
            byteMemory.setByteAt(RoxWord.ZERO, RoxByte.fromLiteral(42));
            fail("Writing to read only memory should throw an exception.");
        }catch(RuntimeException e){}
    }

    @Test
    public void testExplicitlySizedMemory(){
        assertEquals(20, memory.getSize());
        try {
            memory.setByteAt(RoxWord.fromLiteral(10), RoxByte.fromLiteral(0));
            fail("Should not be able to access memory outside the size of addressable memory");
        }catch(RuntimeException e){}
    }

    @Test
    public void testRead(){
        assertEquals(RoxByte.fromLiteral(5), memory.getByte(RoxWord.fromLiteral(5)));
    }

    @Test
    public void testReadBlock(){
        int[] expected = new int[] {0,1,2,3};
        int[] actual = toIntArray(memory.getBlock(RoxWord.ZERO, RoxWord.fromLiteral(4)));
        assertTrue("Expected " + Arrays.toString(expected) + ", got " +  Arrays.toString(actual), Arrays.equals(expected, actual));
    }

    @Test
    public void testReadBlockWithZeroSize(){
        int[] expected = new int[] {};
        int[] actual = toIntArray(memory.getBlock(RoxWord.ZERO, RoxWord.ZERO));
        assertTrue("Expected " + Arrays.toString(expected) + ", got " +  Arrays.toString(actual), Arrays.equals(expected, actual));
    }

    @Test
    public void testReadBlockWithNegativeSize(){
        try {
            memory.getBlock(RoxWord.fromLiteral(4), RoxWord.ZERO);
            fail("Requesting a negatively sized memory block should throw an IllegalArgumentException");
        }catch(IllegalArgumentException e){}
    }

    @Test
    public void testReset(){
        int[] actual = toIntArray(memory.getBlock(RoxWord.ZERO, RoxWord.fromLiteral(20)));
        memory.reset();
        assertTrue("Expected " + Arrays.toString(memoryValues) + ", got " + Arrays.toString(actual),Arrays.equals(memoryValues, actual));
    }

    @Test
    public void testReadWord(){
        assertEquals(RoxWord.from(RoxByte.fromLiteral(1), RoxByte.fromLiteral(2)), memory.getWord(RoxWord.fromLiteral(1)));
    }

    @Test
    public void testWrite(){
        try {
            memory.setByteAt(RoxWord.ZERO, RoxByte.ZERO);
            fail("Writing to read only memory should throw an exception.");
        }catch (RuntimeException e){}
    }

    @Test
    public void testWriteBlock(){
        try {
            memory.setBlock(RoxWord.ZERO, toRoxByteArray(new int[] {0,1,2,3,4,5}));
            fail("Writing to read only memory should throw an exception.");
        }catch (RuntimeException e){}
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
