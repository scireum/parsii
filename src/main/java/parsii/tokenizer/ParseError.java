/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.tokenizer;

/**
 * Represents an error or a warning which occurred when parsing an input.
 * <p>
 * Used by {@link ParseException} to collect as many errors as possible before failing (throwing).
 */
public class ParseError {

    private Position pos;
    private String message;
    private final Severity severity;

    /**
     * Specifies whether an error (unrecoverable problem) or a warning occurred.
     */
    public enum Severity {
        WARNING, ERROR
    }

    /*
     * Use one of the factory methods to create a new instance
     */
    protected ParseError(Position pos, String message, Severity severity) {
        this.pos = pos;
        this.message = message;
        this.severity = severity;
    }

    /**
     * Creates a new warning for the given position with the given message.
     * <p>
     * If no position is available {@link Position#UNKNOWN} can be used
     *
     * @param pos the position where the warning occurred
     * @param msg the message explaining the warning
     * @return a new ParseError containing the warning
     */
    public static ParseError warning(Position pos, String msg) {
        String message = msg;
        if (pos.getLine() > 0) {
            message = String.format("%3d:%2d: %s", pos.getLine(), pos.getPos(), msg);
        }
        return new ParseError(pos, message, Severity.WARNING);
    }

    /**
     * Creates a new error for the given position with the given message.
     * <p>
     * If no position is available {@link Position#UNKNOWN} can be used
     *
     * @param pos the position where the error occurred
     * @param msg the message explaining the error
     * @return a new ParseError containing the error
     */
    public static ParseError error(Position pos, String msg) {
        String message = msg;
        if (pos.getLine() > 0) {
            message = String.format("%3d:%2d: %s", pos.getLine(), pos.getPos(), msg);
        }
        return new ParseError(pos, message, Severity.ERROR);
    }

    /**
     * Provides the position where the error or warning occurred.
     *
     * @return the position of this error or warning
     */
    public Position getPosition() {
        return pos;
    }

    /**
     * Provides the message explaining the error or warning.
     *
     * @return the message of this error or warning
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the severity, which indicates if this is an error or a warning.
     *
     * @return the severity of this.
     */
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return String.format("%s %s", severity, message);
    }
}
