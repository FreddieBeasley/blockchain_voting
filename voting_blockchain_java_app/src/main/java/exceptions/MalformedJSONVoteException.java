package exceptions;

public class MalformedJSONVoteException extends Exception{
    public MalformedJSONVoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedJSONVoteException(String message) {
        super(message);
    }
}

