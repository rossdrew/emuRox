package com.rox.emu.env;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import spock.lang.Specification;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(JUnitQuickcheck.class)
public class RoxByteTest extends Specification{
    @Test
    public void testEmptyByteCreation(){
        final RoxByte myByte = new RoxByte();
        assertNotNull(myByte);
    }

    @Test
    public void testByteFromInteger() throws Exception {
        final RoxByte myByte = RoxByte.signedFrom(1);
        assertNotNull(myByte);
        assertEquals(RoxByte.ByteFormat.SIGNED, myByte.getFormat());
    }

    @Property(trials = 10)
    public void testValidGetAsInt(@InRange(min = "-128", max = "127") int byteValue) throws Exception {
        final RoxByte myByte = RoxByte.signedFrom(byteValue);
        assertEquals(byteValue, myByte.getAsInt());
        assertEquals(RoxByte.ByteFormat.SIGNED, myByte.getFormat());
    }

    @Property(trials = 10)
    public void testGetAsIntWithTooLow(@InRange(min = "-300", max = "-128") int byteValue){
        try {
            RoxByte.signedFrom(byteValue);
            fail(byteValue + " was expected to be too low to convert to unsigned byte");
        }catch(Exception e) { //TODO make more specific
            assertNotNull(e);
        }
    }

    @Property(trials = 10)
    public void testGetAsIntWithTooHigh(@InRange(min = "128", max = "300") int byteValue){
        try {
            RoxByte.signedFrom(byteValue);
            fail(byteValue + " was expected to be too high to convert to unsigned byte");
        }catch(Exception e) { //TODO make more specific
            assertNotNull(e);
        }
    }


}
