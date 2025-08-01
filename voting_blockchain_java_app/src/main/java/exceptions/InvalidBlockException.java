package exceptions;

public class InvalidBlockException extends Exception{
    public InvalidBlockException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBlockException(String message) {
        super(message);
    }
}

