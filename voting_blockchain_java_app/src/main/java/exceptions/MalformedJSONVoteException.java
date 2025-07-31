package exceptions;

public class MalformedJSONVoteException extends Exception{
    public MalformedJSONVoteException(String message, Throwable cause) {
        super(message);
    }

    public MalformedJSONVoteException(String message) {
        super(message);
    }
}

