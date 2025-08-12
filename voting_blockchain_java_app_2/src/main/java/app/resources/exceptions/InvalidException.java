package app.resources.exceptions;

public class InvalidException extends Exception{
    public InvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidException(String message) {
        super(message);
    }
}

