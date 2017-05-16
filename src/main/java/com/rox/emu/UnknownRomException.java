package com.rox.emu;

public class UnknownRomException extends RuntimeException{
    public UnknownRomException(String message){
        super(message);
    }
}
