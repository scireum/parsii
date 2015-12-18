/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.tokenizer;

/**
 * Represents a single character read from a {@link LookaheadReader}.
 * <p>
 * Provides the value as well as an exact position of the character in the stream. Also some test methods are
 * provided to determine the character class of the internal value.
 *
 * @see LookaheadReader
 */
public class Char implements Position {
    private char value;
    private int line;
    private int pos;

    Char(char value, int line, int pos) {
        this.value = value;
        this.line = line;
        this.pos = pos;
    }

    /**
     * Returns the value of this char.
     *
     * @return the internal value read from the stream
     */
    public char getValue() {
        return value;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getPos() {
        return pos;
    }

    /**
     * Determines if the value is a digit (0..9)
     *
     * @return <tt>true</tt> if the internal value is a digit, <tt>false</tt> otherwise
     */
    public boolean isDigit() {
        return Character.isDigit(value);
    }

    /**
     * Determines if the value is a letter (a..z, A..Z)
     *
     * @return <tt>true</tt> if the internal value is a letter, <tt>false</tt> otherwise
     */
    public boolean isLetter() {
        return Character.isLetter(value);
    }

    /**
     * Determines if the value is a whitespace character like a blank, tab or line break
     *
     * @return <tt>true</tt> if the internal value is a whitespace character, <tt>false</tt> otherwise
     */
    public boolean isWhitepace() {
        return Character.isWhitespace(value) && !isEndOfInput();
    }

    /**
     * Determines if the value is a line break
     *
     * @return <tt>true</tt> if the internal value is a line break, <tt>false</tt> otherwise
     */
    public boolean isNewLine() {
        return value == '\n';
    }

    /**
     * Determines if this instance represents the end of input indicator
     *
     * @return <tt>true</tt> if this instance represents the end of the underlying input,
     * <tt>false</tt> otherwise
     */
    public boolean isEndOfInput() {
        return value == '\0';
    }

    @Override
    public String toString() {
        if (isEndOfInput()) {
            return "<End Of Input>";
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * Checks if the internal value is one of the given characters
     *
     * @param tests the characters to check against
     * @return <tt>true</tt> if the value equals to one of the give characters, <tt>false</tt> otherwise
     */
    public boolean is(char... tests) {
        for (char test : tests) {
            if (test == value && test != '\0') {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the internal value as string.
     *
     * @return the internal character as string or "" if this is the end of input indicator
     */
    public String getStringValue() {
        if (isEndOfInput()) {
            return "";
        }
        return String.valueOf(value);
    }
}
