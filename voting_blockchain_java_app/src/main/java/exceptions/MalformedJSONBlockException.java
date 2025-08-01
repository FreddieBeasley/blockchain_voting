package exceptions;

public class MalformedJSONBlockException extends Exception{
    public MalformedJSONBlockException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedJSONBlockException(String message) {
        super(message);
    }
}

