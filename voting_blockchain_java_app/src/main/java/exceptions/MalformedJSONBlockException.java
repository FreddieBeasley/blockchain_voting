package exceptions;

public class MalformedJSONBlockException extends Exception{
    public MalformedJSONBlockException(String message, Throwable cause) {
        super(message);
    }

    public MalformedJSONBlockException(String message) {
        super(message);
    }
}

