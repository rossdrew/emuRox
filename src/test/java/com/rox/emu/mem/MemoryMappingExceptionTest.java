package com.rox.emu.mem;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Ross W. Drew
 */
public class MemoryMappingExceptionTest {
    @Test
    public void testCreationWithOpCode(){
        MemoryMappingException e = new MemoryMappingException("This is my reason");

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
    }

    @Test
    public void testCausedException(){
        Exception cause = new Exception();
        MemoryMappingException e = new MemoryMappingException("This is my reason", cause);

        assertNotNull(e.getMessage());
        assertFalse(e.getMessage().isEmpty());
        assertEquals(cause, e.getCause());
    }
}
