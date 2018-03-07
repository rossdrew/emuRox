package com.rox.emu;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class UnknownTokenExceptionTest {
    @Test
    public void testCreationWithToken(){
        UnknownTokenException e = new UnknownTokenException("This is my reason", "UNKNOWN_TOKEN");

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
        assertNotNull(e.getToken());
        assertEquals("UNKNOWN_TOKEN", e.getToken());
    }
}
