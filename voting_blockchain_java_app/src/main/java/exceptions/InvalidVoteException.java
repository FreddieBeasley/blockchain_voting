package exceptions;

public class InvalidVoteException extends Exception{
    public InvalidVoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidVoteException(String message) {
        super(message);
    }
}

