package resources.exceptions;

public class MalformedJSONException extends Exception{
    public MalformedJSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedJSONException(String message) {
        super(message);
    }
}

