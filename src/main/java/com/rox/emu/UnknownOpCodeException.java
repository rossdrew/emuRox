package com.rox.emu;

/**
 * Exception for when an op-code is unknown 
 *  
 * @author Ross Drew
 */
public class UnknownOpCodeException extends RuntimeException {
    private final String opCode;

    public UnknownOpCodeException(String message, Object opCode, Exception cause) {
        super(message, cause);
        this.opCode = opCode.toString();
    }

    public UnknownOpCodeException(String message, Object opCode) {
        super(message);
        this.opCode = opCode.toString();
    }

    public Object getOpCode() {
        return opCode;
    }
}
