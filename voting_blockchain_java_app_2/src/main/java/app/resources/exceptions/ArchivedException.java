package app.resources.exceptions;

public class ArchivedException extends Exception{
    public ArchivedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArchivedException(String message) {
        super(message);
    }
}

