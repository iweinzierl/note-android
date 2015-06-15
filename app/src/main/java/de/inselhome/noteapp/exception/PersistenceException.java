package de.inselhome.noteapp.exception;

public class PersistenceException extends Exception {

    public PersistenceException(String detailMessage) {
        super(detailMessage);
    }

    public PersistenceException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }
}
