package app.resources.exceptions;

public class OverflowException extends Exception{
    public OverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public OverflowException(String message) {
        super(message);
    }
}

