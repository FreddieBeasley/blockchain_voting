package exceptions;

public class MalformedJSONBlockchainException extends Exception{
    public MalformedJSONBlockchainException(String message, Throwable cause) {
        super(message);
    }

    public MalformedJSONBlockchainException(String message) {
        super(message);
    }
}

