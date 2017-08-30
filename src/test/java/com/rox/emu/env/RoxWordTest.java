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

    @Test
    public void testValidLiteralFrom(){
        //Any value above 0xFFFF will just be treated as 'v &= 0xFFFF'
        for (int i=0x0; i<0x10010; i++) {
            final RoxWord word = RoxWord.literalFrom(i);
            final RoxByte expectedLoByte = RoxByte.literalFrom(i & 0xFF);
            final RoxByte expectedHiByte = RoxByte.literalFrom((i >> 8) & 0xFF);

            assertEquals(expectedLoByte, word.getLowByte());
            assertEquals(expectedHiByte, word.getHighByte());
        }
    }
}
