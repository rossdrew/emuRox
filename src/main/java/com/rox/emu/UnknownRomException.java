package com.rox.emu;

/**
 * Exception for when ROM format is invalid or unknown
 *
 * @author Ross Drew
 */
public class UnknownRomException extends RuntimeException{
    public UnknownRomException(String message){
        super(message);
    }
}
