package resources.exceptions;

public class InvalidBlockchainException extends Exception{
    public InvalidBlockchainException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBlockchainException(String message) {
        super(message);
    }
}

