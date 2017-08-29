package com.rox.emu.env;

import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class RoxWordTest {
    @Test
    public void testEmptyWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.ZERO);
        assertNotNull(myWord);
        assertEquals(0, myWord.getAsInt());
    }

    @Test
    public void testSingleArgumentLowByteWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.literalFrom(10));
        assertNotNull(myWord);
        assertEquals(10, myWord.getAsInt());
    }

    @Test
    public void testLowByteWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.ZERO, RoxByte.literalFrom(10));
        assertNotNull(myWord);
        assertEquals(10, myWord.getAsInt());
    }

    @Test
    public void testHighByteWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.literalFrom(1), RoxByte.ZERO);
        assertNotNull(myWord);
        assertEquals(256, myWord.getAsInt());
    }

    @Test
    public void testTwoByteWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.literalFrom(1), RoxByte.literalFrom(1));
        assertNotNull(myWord);
        assertEquals(257, myWord.getAsInt());
    }

    @Test
    public void testGetLowByte(){
        final RoxWord myWord = RoxWord.from(RoxByte.literalFrom(10), RoxByte.literalFrom(20));
        assertEquals(RoxByte.literalFrom(20), myWord.getLowByte());
    }

    @Test
    public void testGetHighByte(){
        final RoxWord myWord = RoxWord.from(RoxByte.literalFrom(10), RoxByte.literalFrom(20));
        assertEquals(RoxByte.literalFrom(10), myWord.getHighByte());
    }
}
