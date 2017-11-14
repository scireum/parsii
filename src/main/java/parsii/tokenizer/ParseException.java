/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.tokenizer;

import java.util.List;

/**
 * Used to signal that processing an input failed.
 * <p>
 * By first collecting as many {@link ParseError} instances as possible, this permits to provide good insights
 * of what is wrong with the input provided by the user.
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = -5618855459424320517L;

    private final transient List<ParseError> errors;

    private ParseException(String message, List<ParseError> errors) {
        super(message);
        this.errors = errors;
    }

    /**
     * Creates a new exception based on the list of errors.
     *
     * @param errors the errors which occurred while processing the user input
     * @return a new ParseException which can be thrown
     */
    public static ParseException create(List<ParseError> errors) {
        if (errors.size() == 1) {
            return new ParseException(errors.get(0).getMessage(), errors);
        } else if (errors.size() > 1) {
            return new ParseException(String.format("%d errors occured. First: %s",
                                                    errors.size(),
                                                    errors.get(0).getMessage()), errors);
        } else {
            return new ParseException("An unknown error occured", errors);
        }
    }

    /**
     * Provides a list of all errors and warnings which occurred
     *
     * @return all errors and warnings which occurred while processing the user input
     */
    public List<ParseError> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ParseError error : errors) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(error);
        }

        return sb.toString();
    }
}
