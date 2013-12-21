package parsii.tokenizer;

/**
 * Describes a position in a file or a stream based on lines and the character position within the line.
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public interface Position {
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

    /**
     * Represents an unknown position for warnings and errors which cannot be associated with a defined position.
     */
    public static final Position UNKNOWN = new Position() {

        @Override
        public int getLine() {
            return 0;
        }

        @Override
        public int getPos() {
            return 0;
        }
    };
}
