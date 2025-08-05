package app.resources.exceptions;

public class LoadException extends Exception{
    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadException(String message) {
        super(message);
    }
}

