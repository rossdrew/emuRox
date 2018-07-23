package com.rox.emu.env;

import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class RoxWordTest {
    @Test
    public void testEquality(){
        assertTrue(RoxWord.ZERO.equals(RoxWord.ZERO));
        assertEquals(RoxWord.ZERO, RoxWord.ZERO);
        assertEquals(RoxWord.ZERO, 0);
        assertEquals(RoxWord.ZERO.hashCode(), RoxWord.ZERO.hashCode());

        assertTrue(RoxWord.fromLiteral(1).equals(1));
        assertEquals(RoxWord.fromLiteral(1), 1);
        assertEquals(RoxWord.fromLiteral(1), RoxWord.fromLiteral(1));
        assertEquals(RoxWord.fromLiteral(1).hashCode(), RoxWord.fromLiteral(1).hashCode());

        assertTrue(RoxWord.fromLiteral(0b1111111111111110).equals(0b1111111111111110));
        assertEquals(RoxWord.fromLiteral(0b1111111111111110), 0b1111111111111110);
        assertEquals(RoxWord.fromLiteral(99), RoxByte.fromLiteral(99));
        assertEquals(RoxWord.fromLiteral(99).hashCode(), RoxByte.fromLiteral(99).hashCode());
    }

    @Test
    public void testInequality(){
        assertFalse(RoxWord.ZERO.equals(RoxWord.fromLiteral(10)));
        assertNotEquals(RoxWord.ZERO, RoxWord.fromLiteral(1));
        assertNotEquals(RoxWord.ZERO, 1);;

        assertFalse(RoxWord.fromLiteral(1).equals(2));
        assertNotEquals(RoxWord.fromLiteral(1), 2);
        assertNotEquals(RoxWord.fromLiteral(1), RoxWord.fromLiteral(2));
        assertNotEquals(RoxWord.fromLiteral(1).hashCode(), RoxWord.fromLiteral(2).hashCode());

        assertFalse(RoxWord.fromLiteral(0b1111111111111110).equals(0b1111111111111100));
        assertNotEquals(RoxWord.fromLiteral(0b1111111111111110), 0b1111111111111100);
        assertNotEquals(RoxWord.fromLiteral(99), RoxByte.fromLiteral(98));
        assertNotEquals(RoxWord.fromLiteral(99).hashCode(), RoxByte.fromLiteral(98).hashCode());

        assertFalse(RoxWord.fromLiteral(23).equals("Test 1"));
        assertNotEquals(RoxWord.fromLiteral(23), "Test 2");
        assertNotEquals(RoxWord.fromLiteral(23).hashCode(), "Test 3".hashCode());

        assertNotEquals(RoxWord.fromLiteral(63), null);
        assertFalse(RoxWord.fromLiteral(71).equals(null));
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
