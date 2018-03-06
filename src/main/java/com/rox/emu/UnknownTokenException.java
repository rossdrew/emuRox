package com.rox.emu;

/**
 * @author Ross W. Drew
 */
public class UnknownTokenException extends RuntimeException {
    private final String token;

    public UnknownTokenException(String message, String token, Exception cause) {
        super(message, cause);
        this.token = token;
    }

    public UnknownTokenException(String message, String token) {
        super(message);
        this.token = token;
    }

    public Object getToken() {
        return token;
    }
}
