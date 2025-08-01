package exceptions;

public class MalformedJSONBlockchainException extends Exception{
    public MalformedJSONBlockchainException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedJSONBlockchainException(String message) {
        super(message);
    }
}

