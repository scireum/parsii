/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * An efficient reader of character streams, reading character by character and supporting lookaheads.
 * <p>
 * Helps to read characters from a {@link Reader} one after another. Using <tt>next</tt>, upcoming characters can
 * be inspected without consuming (removing) the current one.
 */
public class LookaheadReader extends Lookahead<Char> {

    private Reader input;
    private int line = 1;
    private int pos = 0;

    /**
     * Creates a new LookaheadReader for the given Reader.
     * <p>
     * Internally a {@link BufferedReader} is used to efficiently read single characters. The given reader will not
     * be closed by this class.
     *
     * @param input the reader to draw the input from
     */
    public LookaheadReader(Reader input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        this.input = new BufferedReader(input);
    }

    @Override
    protected Char endOfInput() {
        return new Char('\0', line, pos);
    }

    @Override
    protected Char fetch() {
        try {
            int character = input.read();
            if (character == -1) {
                return null;
            }
            if (character == '\n') {
                line++;
                pos = 0;
            }
            pos++;
            return new Char((char) character, line, pos);
        } catch (IOException e) {
            problemCollector.add(ParseError.error(new Char('\0', line, pos), e.getMessage()));
            return null;
        }
    }

    @Override
    public String toString() {
        if (itemBuffer.isEmpty()) {
            return line + ":" + pos + ": Buffer empty";
        }
        if (itemBuffer.size() < 2) {
            return line + ":" + pos + ": " + current();
        }
        return line + ":" + pos + ": " + current() + ", " + next();
    }
}
