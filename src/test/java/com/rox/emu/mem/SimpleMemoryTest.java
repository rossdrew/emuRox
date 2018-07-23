package com.rox.emu.mem;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;
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

        final RoxByte[] expectedMemory = new RoxByte[10];
        IntStream.range(0,10).forEach(i -> expectedMemory[i] = RoxByte.ZERO);

        assertTrue(Arrays.equals(sizedMemory.getBlock(RoxWord.ZERO, RoxWord.fromLiteral(10)), expectedMemory));

        sizedMemory.setByteAt(RoxWord.ZERO, RoxByte.fromLiteral(1));
        sizedMemory.setByteAt(RoxWord.fromLiteral(9), RoxByte.fromLiteral(10));

        assertEquals(10, sizedMemory.getSize());
        try {
            sizedMemory.setByteAt(RoxWord.fromLiteral(10), RoxByte.ZERO);
            fail("Should not be able to access memory outside the size of addressable memory");
        }catch(ArrayIndexOutOfBoundsException e){}
    }

    @Test
    public void testGetAndSetByte(){
        memory.setByteAt(RoxWord.ZERO, RoxByte.fromLiteral(99));
        memory.setByteAt(RoxWord.fromLiteral(0x10), RoxByte.fromLiteral(101));
        memory.setByteAt(RoxWord.fromLiteral(0b00000001), RoxByte.fromLiteral(1));

        assertEquals(RoxByte.fromLiteral(99), memory.getByte(RoxWord.ZERO));
        assertEquals(RoxByte.fromLiteral(101), memory.getByte(RoxWord.fromLiteral(16)));
        assertEquals(RoxByte.fromLiteral(1), memory.getByte(RoxWord.fromLiteral(1)));
    }

    @Test
    public void testGetWord(){
        memory.setByteAt(RoxWord.ZERO, RoxByte.fromLiteral(1));
        memory.setByteAt(RoxWord.fromLiteral(0x01), RoxByte.fromLiteral(1));

        assertEquals(RoxWord.fromLiteral(257), memory.getWord(RoxWord.ZERO));
        assertEquals(RoxWord.fromLiteral(256), memory.getWord(RoxWord.fromLiteral(0x01)));
        assertEquals(RoxWord.fromLiteral(0), memory.getWord(RoxWord.fromLiteral(0x02)));
    }

    @Test
    public void testGetBlock(){
        memory.setBlock(RoxWord.ZERO, RoxByte.fromIntArray(new int [] {0,1,2,3,4,5,6,7,8,9}));

        int offset = 5;
        RoxByte[] retrievedBlock = memory.getBlock(RoxWord.fromLiteral(offset), RoxWord.fromLiteral(9));
        for (int i=0; i<retrievedBlock.length; i++){
            assertEquals(RoxByte.fromLiteral(i + offset), retrievedBlock[i]);
        }
    }

    @Test
    public void testSetMemory(){
        RoxByte[] values = RoxByte.fromIntArray(new int[] {1,22,33,44,55});
        memory.setBlock(RoxWord.ZERO, values);

        assertEquals(65536, memory.getSize());

        assertEquals(RoxByte.fromLiteral(1), memory.getByte(RoxWord.ZERO));
        assertEquals(RoxByte.fromLiteral(22), memory.getByte(RoxWord.fromLiteral(1)));
        assertEquals(RoxByte.fromLiteral(33), memory.getByte(RoxWord.fromLiteral(2)));
        assertEquals(RoxByte.fromLiteral(44), memory.getByte(RoxWord.fromLiteral(3)));
        assertEquals(RoxByte.fromLiteral(55), memory.getByte(RoxWord.fromLiteral(4)));
    }

    @Test
    public void testClearMemory(){
        memory.setBlock(RoxWord.ZERO, RoxByte.fromIntArray(new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}));
        memory.reset();
        for (int i=0; i<30; i++){
            assertEquals(RoxByte.ZERO, memory.getByte(RoxWord.fromLiteral(i)));
        }
    }

}
