package com.rox.emu;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnknownTokenExceptionTest {
    @Test
    public void testCreationWithTokenAndException(){
        UnknownTokenException e = new UnknownTokenException("This is my reason", "UNKNOWN_TOKEN", new RuntimeException());

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
        assertNotNull(e.getToken());
        assertNotNull(e.getCause());
        assertEquals("UNKNOWN_TOKEN", e.getToken());
    }

    @Test
    public void testCreationWithToken(){
        UnknownTokenException e = new UnknownTokenException("This is my reason", "UNKNOWN_TOKEN");

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
        assertNotNull(e.getToken());
        assertNull(e.getCause());
        assertEquals("UNKNOWN_TOKEN", e.getToken());
    }
}
