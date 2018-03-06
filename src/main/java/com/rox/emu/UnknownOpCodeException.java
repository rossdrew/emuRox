package com.rox.emu;

/**
 * Exception for when an op-code is unknown 
 *  
 * @author Ross Drew
 */
public class UnknownOpCodeException extends RuntimeException {
    private final Object opCode;

    public UnknownOpCodeException(String message, Object opCode, Exception cause) {
        super(message, cause);
        this.opCode = opCode;
    }

    public UnknownOpCodeException(String message, Object opCode) {
        super(message);
        this.opCode = opCode;
    }

    public Object getOpCode() {
        return opCode;
    }
}
