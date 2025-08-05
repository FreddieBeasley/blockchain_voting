package app.resources.exceptions;

public class InvalidPublicKeyException extends Exception{
    public InvalidPublicKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPublicKeyException(String message) {
        super(message);
    }
}

