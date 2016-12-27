package com.rox.emu;

/**
 * @author Ross Drew
 */
public class UnknownOpCodeException extends RuntimeException{
    private Object opCode = null;

    public UnknownOpCodeException(String message, Object opCode) {
        super(message);
        this.opCode = opCode;
    }

    public Object getOpCode() {
        return opCode;
    }
}
