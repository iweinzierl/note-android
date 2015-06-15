package de.inselhome.noteapp.exception;

public class NoNetworkException extends RuntimeException {

    public NoNetworkException(String detailMessage) {
        super(detailMessage);
    }
}
