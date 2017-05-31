package com.rox.emu.env;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.rox.emu.InvalidDataTypeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import spock.lang.Specification;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class RoxByteTest extends Specification{
    @Test
    public void testEmptyByteCreation(){
        final RoxByte myByte = RoxByte.ZERO;
        assertNotNull(myByte);
        assertEquals(0, myByte.getAsInt());
    }

    @Test
    public void testByteFromInteger() throws InvalidDataTypeException {
        final RoxByte myByte = RoxByte.signedFrom(1);
        assertNotNull(myByte);
        assertEquals(RoxByte.ByteFormat.SIGNED_TWOS_COMPLIMENT, myByte.getFormat());
    }

    @Property(trials = 30)
    public void testGetAsIntWithInvalid(@When(satisfies = "#_ < -128 || #_ > 127") int byteValue){
        try {
            RoxByte.signedFrom(byteValue);
            fail(byteValue + " was expected to be too low to convert to unsigned byte");
        }catch(InvalidDataTypeException e) {
            assertNotNull(e);
        }
    }

    @Property(trials = 10)
    public void testGetAsIntWithTooHigh(@InRange(min = "128", max = "300") int byteValue){
        try {
            RoxByte.signedFrom(byteValue);
            fail(byteValue + " was expected to be too high to convert to unsigned byte");
        }catch(InvalidDataTypeException e) {
            assertNotNull(e);
        }
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

    @Property(trials = 5)
    public void testSetBitInvalidChoice(@When(satisfies = "#_ < 0 || #_ > 7") int bit){
        final RoxByte myByte = RoxByte.ZERO;

        try {
            myByte.withBit(bit);
            fail("There is no bit " + bit + ", this should throw an error");
        }catch(AssertionError e){
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

    @Property(trials = 5)
    public void testIsBitSetInvalidChoice(@When(satisfies = "#_ < 0 || #_ > 7") int bit){
        final RoxByte myByte = RoxByte.ZERO;

        try {
            myByte.isBitSet(bit);
            fail("There is no bit " + bit + ", this should throw an error");
        }catch(AssertionError e){
            assertNotNull(e);
        }
    }


}
