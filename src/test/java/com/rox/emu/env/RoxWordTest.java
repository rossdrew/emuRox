package com.rox.emu.env;

import junit.framework.TestCase;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoxWordTest {
    @Test
    public void comparisons(){
        assertTrue(RoxWord.fromLiteral(1).equals(1));
        TestCase.assertEquals(RoxWord.fromLiteral(1), 1);
        TestCase.assertEquals(RoxWord.fromLiteral(1), RoxWord.fromLiteral(1));

        assertTrue(RoxWord.fromLiteral(0b1111111111111110).equals(0b1111111111111110));
        TestCase.assertEquals(RoxWord.fromLiteral(0b1111111111111110), 0b1111111111111110);

        assertEquals(RoxWord.fromLiteral(99), RoxByte.fromLiteral(99));
    }

    @Test
    public void testEmptyWordCreation(){
        final RoxWord myWord = RoxWord.ZERO;
        assertNotNull(myWord);
        assertEquals(0, myWord.getAsInt());
    }

    @Test
    public void testSingleArgumentLowByteWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.fromLiteral(10));
        assertNotNull(myWord);
        assertEquals(10, myWord.getAsInt());
    }

    @Test
    public void testLowByteWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.ZERO, RoxByte.fromLiteral(10));
        assertNotNull(myWord);
        assertEquals(10, myWord.getAsInt());
    }

    @Test
    public void testHighByteWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.fromLiteral(1), RoxByte.ZERO);
        assertNotNull(myWord);
        assertEquals(256, myWord.getAsInt());
    }

    @Test
    public void testTwoByteWordCreation(){
        final RoxWord myWord = RoxWord.from(RoxByte.fromLiteral(1), RoxByte.fromLiteral(1));
        assertNotNull(myWord);
        assertEquals(257, myWord.getAsInt());
    }

    @Test
    public void testGetLowByte(){
        final RoxWord myWord = RoxWord.from(RoxByte.fromLiteral(10), RoxByte.fromLiteral(20));
        assertEquals(RoxByte.fromLiteral(20), myWord.getLowByte());
    }

    @Test
    public void testGetHighByte(){
        final RoxWord myWord = RoxWord.from(RoxByte.fromLiteral(10), RoxByte.fromLiteral(20));
        assertEquals(RoxByte.fromLiteral(10), myWord.getHighByte());
    }

    @Test
    public void testValidLiteralFrom(){
        //Any value above 0xFFFF will just be treated as 'v &= 0xFFFF'
        for (int i=0x0; i<0x10010; i++) {
            final RoxWord word = RoxWord.fromLiteral(i);
            final RoxByte expectedLoByte = RoxByte.fromLiteral(i & 0xFF);
            final RoxByte expectedHiByte = RoxByte.fromLiteral((i >> 8) & 0xFF);

            assertEquals(expectedLoByte, word.getLowByte());
            assertEquals(expectedHiByte, word.getHighByte());
        }
    }
}
