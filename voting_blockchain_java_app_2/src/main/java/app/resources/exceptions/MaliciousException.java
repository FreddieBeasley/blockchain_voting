package app.resources.exceptions;

public class MaliciousException extends Exception{
    public MaliciousException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaliciousException(String message) {
        super(message);
    }
}

