package com.rox.emu.env;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.InvalidDataTypeException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import spock.lang.Specification;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class RoxByteTest extends Specification{
        @Test
    public void testEquality(){
        assertTrue(RoxWord.ZERO.equals(RoxWord.ZERO));
        Assert.assertEquals(RoxWord.ZERO, RoxWord.ZERO);
        Assert.assertEquals(RoxWord.ZERO, 0);
        Assert.assertEquals(RoxWord.ZERO.hashCode(), RoxWord.ZERO.hashCode());

        assertTrue(RoxWord.fromLiteral(1).equals(1));
        Assert.assertEquals(RoxWord.fromLiteral(1), 1);
        Assert.assertEquals(RoxWord.fromLiteral(1), RoxWord.fromLiteral(1));
        Assert.assertEquals(RoxWord.fromLiteral(1).hashCode(), RoxWord.fromLiteral(1).hashCode());

        assertTrue(RoxWord.fromLiteral(0b11111110).equals(0b11111110));
        Assert.assertEquals(RoxWord.fromLiteral(0b11111110), 0b11111110);
        Assert.assertEquals(RoxWord.fromLiteral(99), RoxByte.fromLiteral(99));
        Assert.assertEquals(RoxWord.fromLiteral(99).hashCode(), RoxByte.fromLiteral(99).hashCode());
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

        assertFalse(RoxWord.fromLiteral(0b11111110).equals(0b11111100));
        assertNotEquals(RoxWord.fromLiteral(0b11111110), 0b11111100);
        assertNotEquals(RoxWord.fromLiteral(99), RoxByte.fromLiteral(98));
        assertNotEquals(RoxWord.fromLiteral(99).hashCode(), RoxByte.fromLiteral(98).hashCode());

        assertFalse(RoxWord.fromLiteral(23).equals("Test 1"));
        assertNotEquals(RoxWord.fromLiteral(23), "Test 2");
        assertNotEquals(RoxWord.fromLiteral(23).hashCode(), "Test 3".hashCode());
    }


    @Test
    public void testToTwosComplimentSimpleCase(){
        final RoxByte myByte = RoxByte.signedFrom(1);
        final RoxByte twosCompliment = myByte.asTwosCompliment();
        assertEquals(-1, twosCompliment.getAsInt());
        assertEquals(0b11111111, twosCompliment.getRawValue());
    }

    @Test
    public void testToTwosComplimentStandardCase(){
        final RoxByte myByte = RoxByte.signedFrom(10);
        final RoxByte twosCompliment = myByte.asTwosCompliment();
        assertEquals(-10, twosCompliment.getAsInt());
        assertEquals(0b11110110, twosCompliment.getRawValue());
    }

    @Test
    public void testToOnesComplimentSimpleCase(){
        final RoxByte myByte = RoxByte.signedFrom(1);
        final RoxByte onesCompliment = myByte.asOnesCompliment();
        assertEquals(-2, onesCompliment.getAsInt());
        assertEquals(0b11111110, onesCompliment.getRawValue());
    }

    @Test
    public void testEmptyByteCreation(){
        final RoxByte myByte = RoxByte.ZERO;
        assertNotNull(myByte);
        assertEquals(0, myByte.getAsInt());
    }

    @Test
    public void testSignedFromInteger() throws InvalidDataTypeException {
        final RoxByte myByte = RoxByte.signedFrom(1);
        assertNotNull(myByte);
        assertEquals(RoxByte.ByteFormat.SIGNED_TWOS_COMPLIMENT, myByte.getFormat());
        assertEquals(0b00000001, myByte.getRawValue());
    }

    @Test
    public void testLiteralFromInteger() throws InvalidDataTypeException {
        final RoxByte myByte = RoxByte.fromLiteral(1);
        assertNotNull(myByte);
        assertEquals(RoxByte.ByteFormat.SIGNED_TWOS_COMPLIMENT, myByte.getFormat());
        assertEquals(0b00000001, myByte.getRawValue());
    }

    @Test
    public void testLiteralFromByte() throws InvalidDataTypeException {
        byte fromByte = 0b00000001;
        final RoxByte myByte = RoxByte.fromLiteral(fromByte);
        assertNotNull(myByte);
        assertEquals(RoxByte.ByteFormat.SIGNED_TWOS_COMPLIMENT, myByte.getFormat());
        assertEquals(0b00000001, myByte.getRawValue());
    }

    @Test
    public void testByteFromRaw() throws InvalidDataTypeException {
        final RoxByte myByte = RoxByte.fromLiteral(0b11111111);
        assertNotNull(myByte);
        assertEquals(RoxByte.ByteFormat.SIGNED_TWOS_COMPLIMENT, myByte.getFormat());
        assertEquals(0b11111111, myByte.getRawValue());
    }

    @Property(trials = 30)
    public void testSignedFromWithInvalid(@When(satisfies = "#_ < -128 || #_ > 127") int byteValue){
        try {
            RoxByte.signedFrom(byteValue);
            fail(byteValue + " was expected to be too low to convert to unsigned byte");
        }catch(InvalidDataTypeException e) {
            assertNotNull(e);
        }
    }

    @Property(trials = 10)
    public void testSignedFromWithTooHigh(@InRange(min = "128", max = "300") int byteValue){
        try {
            RoxByte.signedFrom(byteValue);
            fail(byteValue + " was expected to be too high to convert to unsigned byte");
        }catch(InvalidDataTypeException e) {
            assertNotNull(e);
        }
    }

    @Property(trials = 10)
    public void testToStringIsAccurate(@InRange(min = "0", max = "255") int byteValue){
        final RoxByte value = RoxByte.fromLiteral(byteValue);
        assertTrue(value.toString() + " expected to contain " + byteValue, value.toString().contains(""+value.getAsInt()));
    }

    @Test
    public void testSetBit(){
        final RoxByte myByte = RoxByte.ZERO;

        assertEquals(1, myByte.withBit(0).getAsInt());
        assertEquals(2, myByte.withBit(1).getAsInt());
        assertEquals(4, myByte.withBit(2).getAsInt());
        assertEquals(8, myByte.withBit(3).getAsInt());
        assertEquals(16, myByte.withBit(4).getAsInt());
        assertEquals(32, myByte.withBit(5).getAsInt());
        assertEquals(64, myByte.withBit(6).getAsInt());
        assertEquals(-128, myByte.withBit(7).getAsInt());
    }

    @Test
    public void testWithoutBit(){
        final RoxByte myByte = RoxByte.fromLiteral(0b11111111);

        assertEquals(0b11111110, myByte.withoutBit(0).getRawValue());
        assertEquals(0b11111101, myByte.withoutBit(1).getRawValue());
        assertEquals(0b11111011, myByte.withoutBit(2).getRawValue());
        assertEquals(0b11110111, myByte.withoutBit(3).getRawValue());
        assertEquals(0b11101111, myByte.withoutBit(4).getRawValue());
        assertEquals(0b11011111, myByte.withoutBit(5).getRawValue());
        assertEquals(0b10111111, myByte.withoutBit(6).getRawValue());
        assertEquals(0b01111111, myByte.withoutBit(7).getRawValue());
    }

    @Property(trials = 5)
    public void testWithBitInvalidChoice(@When(satisfies = "#_ < 0 || #_ > 7") int bit){
        final RoxByte myByte = RoxByte.ZERO;

        try {
            myByte.withBit(bit);
            fail("There is no bit " + bit + ", this should throw an error");
        }catch(ArrayIndexOutOfBoundsException e){
            assertNotNull(e);
        }
    }

    @Property(trials = 5)
    public void testWithoutBitInvalidChoice(@When(satisfies = "#_ < 0 || #_ > 7") int bit){
        final RoxByte myByte = RoxByte.ZERO;

        try {
            myByte.withoutBit(bit);
            fail("There is no bit " + bit + ", this should throw an error");
        }catch(ArrayIndexOutOfBoundsException e){
            assertNotNull(e);
        }
    }

    @Test
    public void testIsBitSet(){
        final RoxByte loadedByte = RoxByte.signedFrom(-1);
        final RoxByte emptyByte = RoxByte.ZERO;

        for (int i=0; i<8; i++){
            assertTrue(loadedByte.isBitSet(i));
            assertFalse(emptyByte.isBitSet(i));
        }
    }

    @Property(trials = 10)
    public void testEquals(@InRange(min = "0", max = "255") int byteValue){
        final RoxByte valA = RoxByte.fromLiteral(byteValue);
        final RoxByte valB = RoxByte.fromLiteral(byteValue);

        assertTrue(valA.equals(valB));
    }

    @Property(trials = 100)
    public void testIsNegative(@InRange(min = "0", max = "127") int positiveValue,
                               @InRange(min = "128", max = "255") int negativeValue){
        final RoxByte positiveByte = RoxByte.fromLiteral(positiveValue);
        final RoxByte negativeByte = RoxByte.fromLiteral(negativeValue);

        assertTrue(negativeByte.isNegative());
        assertFalse(positiveByte.isNegative());
    }

    @Property(trials = 10)
    public void testNotEquals(@InRange(min = "0", max = "127") int byteValueA,
                              @InRange(min = "128", max = "255") int byteValueB){
        final RoxByte valA = RoxByte.fromLiteral(byteValueA);
        final RoxByte valB = RoxByte.fromLiteral(byteValueB);

        assertFalse(valA + " == " + valB + " and was expected not to", valA.equals(valB));
    }

    @Property(trials = 10)
    public void testEqualsEdgesCases(@InRange(min = "0", max = "255") int byteValue){
        final RoxByte valA = RoxByte.fromLiteral(byteValue);
        final RoxByte valB = RoxByte.fromLiteral(byteValue);

        assertTrue(valA.equals(valA));
        assertTrue(valA.equals(valB));

        assertFalse(valA.equals(null));
        assertFalse(valA.equals("This does not match"));
    }

    @Property(trials = 10)
    public void testHashcode(@InRange(min = "0", max = "255") int byteValue){
        final RoxByte valA = RoxByte.fromLiteral(byteValue);
        final RoxByte valB = RoxByte.fromLiteral(byteValue);

        assertTrue(valA.hashCode() == valB.hashCode());
    }

    @Property(trials = 5)
    public void testIsBitSetInvalidChoice(@When(satisfies = "#_ < 0 || #_ > 7") int bit){
        final RoxByte myByte = RoxByte.ZERO;

        try {
            myByte.isBitSet(bit);
            fail("There is no bit " + bit + ", this should throw an error");
        }catch(ArrayIndexOutOfBoundsException e){
            assertNotNull(e);
        }
    }
}
