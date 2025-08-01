package exceptions;

public class MalformedJSONPeerException extends Exception{
    public MalformedJSONPeerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedJSONPeerException(String message) {
        super(message);
    }
}

