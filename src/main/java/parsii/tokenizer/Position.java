/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.tokenizer;

/**
 * Describes a position in a file or a stream based on lines and the character position within the line.
 */
@SuppressWarnings("squid:S1214")
public interface Position {

    /**
     * Represents an unknown position for warnings and errors which cannot be associated with a defined position.
     */
    Position UNKNOWN = new Position() {

        @Override
        public int getLine() {
            return 0;
        }

        @Override
        public int getPos() {
            return 0;
        }
    };

    /**
     * Returns the line number of this position.
     *
     * @return the one-based line number of this position
     */
    int getLine();

    /**
     * Returns the character position within the line of this position
     *
     * @return the one-based character position of this
     */
    int getPos();

}
