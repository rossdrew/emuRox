package com.rox.emu;

/**
 * An exception thrown when trying to deal with a data type that is invalid
 */
public class InvalidDataTypeException extends RuntimeException {
    public InvalidDataTypeException(String message){
        super(message);
    }

}
