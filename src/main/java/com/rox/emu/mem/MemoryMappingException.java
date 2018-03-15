package com.rox.emu.mem;

/**
 * Just a {@link RuntimeException} wrapper for memory mapping exceptions
 */
public class MemoryMappingException extends RuntimeException {
    public MemoryMappingException(final String message){
        super(message);
    }

    public MemoryMappingException(final String message, final Exception cause){
        super(message, cause);
    }
}
