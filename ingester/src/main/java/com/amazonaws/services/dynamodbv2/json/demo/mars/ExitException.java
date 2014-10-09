package com.amazonaws.services.dynamodbv2.json.demo.mars;

/**
 * A fatal exception for the {@link ImageIngester}.
 */
public class ExitException extends Exception {
    /**
     * Serial Version.
     */
    private static final long serialVersionUID = 67891134344302080L;

    /**
     * Constructs an {@link ExitException} with the supplied error message.
     *
     * @param message
     *            The error message for the exception.
     */
    public ExitException(final String message) {
        super(message);
    }

    /**
     * Constructs an {@link ExitException} with the supplied error message and cause {@link Throwable}.
     *
     * @param message
     *            The error message for the exception
     * @param cause
     *            The {@link Throwable} that caused the exception
     */
    public ExitException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
