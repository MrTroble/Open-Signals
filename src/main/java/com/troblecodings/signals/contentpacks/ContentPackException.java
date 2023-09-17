package com.troblecodings.signals.contentpacks;

public class ContentPackException extends RuntimeException {

    private static final long serialVersionUID = -3954830220162464371L;

    public ContentPackException(final String message) {
        super(message);
    }

    public ContentPackException(final Throwable err) {
        super(err);
    }

    public ContentPackException(final String message, final Throwable err) {
        super(message, err);
    }
}