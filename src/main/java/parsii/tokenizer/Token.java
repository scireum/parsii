/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.tokenizer;

/**
 * Represents a token of text read from a {@link Tokenizer}.
 * <p>
 * A token consists of a position, a type and up to three string values. The first is the <tt>trigger</tt>. Along with
 * the type this uniquely identifies what kind of token we're looking at. For example the input <tt>"Hello"</tt>, the
 * id <tt>Hello</tt> and a special id like <tt>#Hello</tt> all have the same content (Hello). However, they will
 * have different types (<tt>STRING</tt>, <tt>ID</tt>, <tt>SPECIAL_ID</tt>). However, the two special ids
 * <tt>$Hello</tt> and <tt>#Hello</tt> will both have the same token type and content. Those will differ in their
 * <tt>trigger</tt> which will be <tt>#</tt> and <tt>$</tt> respectively.
 * <p>
 * As already shown, the <tt>content</tt> contains the effective content of the token which should be used for
 * further processing. Finally the <tt>source</tt> contains the complete text which was consumed while reading the
 * token. If we look at a string constant <tt>"Hello"</tt> the content will be <tt>Hello</tt> and the source will
 * be <tt>"Hello"</tt>.
 * <p>
 * The basic token types supported are:
 * <ul>
 * <li>ID: Represents an name or reference like a function name or variable.</li>
 * <li>SPECIAL_ID: Represents an id which starts with a special character like $ or # and continues with an id</li>
 * <li>STRING: Represents a string constant</li>
 * <li>DECIMAL: Represents a decimal constant</li>
 * <li>SCIENTIFIC_DECIMAL: Represents a decimal constant with a scientific notation</li>
 * <li>INTEGER: Represents an integer constant</li>
 * <li>SYMBOL: Represents any combination of "special" chars like + - ** etc. This will also be all bracket</li>
 * <li>KEYWORD: Represents an ID which was identified as a keyword</li>
 * <li>EOI: Signals the end of input</li>
 * </ul>
 */
public class Token implements Position {

    /**
     * Contains the different token types supported by this class.
     */
    public enum TokenType {
        ID, SPECIAL_ID, STRING, DECIMAL, SCIENTIFIC_DECIMAL, INTEGER, SYMBOL, KEYWORD, EOI
    }

    private TokenType type;
    private String trigger = "";
    private String internTrigger = null;
    private String contents = "";
    private String source = "";

    private int line;
    protected int pos;

    /*
     * Use one of the static factory methods
     */
    private Token() {
    }

    /**
     * Creates a new token with the given type, using the given position as location info.
     *
     * @param type the type if this token. Can be further specified by supplying a trigger.
     * @param pos  the location of this token
     * @return a new token which can be filled with content and trigger infos
     */
    public static Token create(TokenType type, Position pos) {
        Token result = new Token();
        result.type = type;
        result.line = pos.getLine();
        result.pos = pos.getPos();

        return result;
    }

    /**
     * Creates a new token with the given type, using the Char a initial trigger and content.
     *
     * @param type the type if this token. The supplied Char will be used as initial part of the trigger to further
     *             specify the token
     * @param ch   first character of the content and trigger of this token. Also specifies the position of the token.
     * @return a new token which is initialized with the given Char
     */
    public static Token createAndFill(TokenType type, Char ch) {
        Token result = new Token();
        result.type = type;
        result.line = ch.getLine();
        result.pos = ch.getPos();
        result.contents = ch.getStringValue();
        result.trigger = ch.getStringValue();
        result.source = ch.toString();
        return result;
    }

    /**
     * Adds the given Char to the trigger (and the source) but not to the content
     *
     * @param ch the character to add to the trigger and source
     * @return <tt>this</tt> to support fluent method calls
     */
    public Token addToTrigger(Char ch) {
        trigger += ch.getValue();
        internTrigger = null;
        source += ch.getValue();
        return this;
    }

    /**
     * Adds the given Char to the source of this token, but neither to the trigger nor to the content.
     *
     * @param ch the character to add to the source
     * @return <tt>this</tt> to support fluent method calls
     */
    public Token addToSource(Char ch) {
        source += ch.getValue();
        return this;
    }

    /**
     * Adds the given Char to the content (and the source) but not to the trigger
     *
     * @param ch the character to add to the content and source
     * @return <tt>this</tt> to support fluent method calls
     */
    public Token addToContent(Char ch) {
        return addToContent(ch.getValue());
    }

    /**
     * Adds the given character to the content (and the source) but not to the trigger
     *
     * @param ch the character to add to the content and source
     * @return <tt>this</tt> to support fluent method calls
     */
    public Token addToContent(char ch) {
        contents += ch;
        source += ch;
        return this;
    }

    /**
     * Adds a character to the content without adding it to the source.
     *
     * @param ch the character to add to the content
     * @return <tt>this</tt> to support fluent method calls
     */
    public Token silentAddToContent(char ch) {
        contents += ch;
        return this;
    }

    /**
     * Returns the string or character which further specifies this token.
     *
     * @return a first character or characters which where used to determine the token type
     */
    public String getTrigger() {
        if (internTrigger == null) {
            internTrigger = trigger.intern();
        }
        return internTrigger;
    }

    /**
     * Returns the basic classification of this token
     *
     * @return the type of this toke
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the effective content of this token
     *
     * @return the content of this token
     */
    public String getContents() {
        return contents;
    }

    /**
     * Returns the complete source string consumed while parsing this token
     *
     * @return all characters consumed while parsing this token
     */
    public String getSource() {
        return source;
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
     * Externally sets the trigger used for this token.
     * <p>
     * This will neither change the content nor the source of this token.
     *
     * @param trigger the new trigger of this token
     */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
        this.internTrigger = null;
    }

    /**
     * Externally sets the content used for this token.
     * <p>
     * This will neither change the trigger nor the source of this token.
     *
     * @param content the new content of this token
     */
    public void setContent(String content) {
        this.contents = content;
    }

    /**
     * Externally sets the source used for this token.
     * <p>
     * This will neither change the trigger nor the content of this token.
     *
     * @param source the new source of this token
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Determines if this is an end of input token
     *
     * @return <tt>true</tt> if this is an end of input token (with EOI as type), <tt>false</tt> otherwise
     */
    public boolean isEnd() {
        return type == TokenType.EOI;
    }

    /**
     * Opposite of {@link #isEnd()}.
     *
     * @return <tt>false</tt> if this is an end of input token (with EOI as type), <tt>true</tt> otherwise
     */
    public boolean isNotEnd() {
        return type != TokenType.EOI;
    }

    /**
     * Determines if this token has the given type and trigger.
     *
     * @param type    the expected type
     * @param trigger the expected trigger
     * @return <tt>true</tt> if this token matches the given type and trigger, <tt>false</tt> otherwise
     */
    @SuppressWarnings("squid:S1698")
    public boolean matches(TokenType type, String trigger) {
        if (!is(type)) {
            return false;
        }
        if (trigger == null) {
            throw new IllegalArgumentException("trigger must not be null");
        }

        return getTrigger() == trigger.intern();
    }

    /**
     * Determines if this token was triggered by one of the given triggers.
     *
     * @param triggers a list of possible triggers to compare to
     * @return <tt>true</tt> if this token was triggered by one of the given triggers, <tt>false</tt> otherwise
     */
    @SuppressWarnings("squid:S1698")
    public boolean wasTriggeredBy(String... triggers) {
        if (triggers.length == 0) {
            return false;
        }
        for (String aTrigger : triggers) {
            if (aTrigger != null && aTrigger.intern() == getTrigger()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the given content matches the content of this token.
     *
     * @param content the content to check for
     * @return <tt>true</tt> if the content of this token equals the given content (ignoring case),
     * <tt>false</tt> otherwise
     */
    public boolean hasContent(String content) {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }
        return content.equalsIgnoreCase(getContents());
    }

    /**
     * Determines if the token has the given type
     *
     * @param type the expected type
     * @return <tt>true</tt> if this token has the given type, <tt>false</tt> otherwise
     */
    public boolean is(TokenType type) {
        return this.type == type;
    }

    /**
     * Determines if this token is a symbol.
     * <p>
     * If a list of <tt>symbols</tt> is given, this method checks that the trigger matches one of them.
     *
     * @param symbols the symbols to check for. If the list es empty, only the token type is checked.
     * @return <tt>true</tt> if this token is a symbol and matches one of the given <tt>symbols</tt> if the list
     * is not empty.
     */
    public boolean isSymbol(String... symbols) {
        if (symbols.length == 0) {
            return is(TokenType.SYMBOL);
        }
        for (String symbol : symbols) {
            if (matches(TokenType.SYMBOL, symbol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if this token is a keyword.
     * <p>
     * If a list of <tt>symbols</tt> is given, this method checks that the trigger matches one of them.
     *
     * @param keywords the keywords to check for. If the list es empty, only the token type is checked.
     * @return <tt>true</tt> if this token is a keyword and matches one of the given <tt>keywords</tt> if the list
     * is not empty.
     */
    public boolean isKeyword(String... keywords) {
        if (keywords.length == 0) {
            return is(TokenType.KEYWORD);
        }
        for (String keyword : keywords) {
            if (matches(TokenType.KEYWORD, keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if this token is an identifier.
     * <p>
     * If a list of <tt>values</tt> is given, this method checks that the content matches one of them.
     *
     * @param values the values to check for. If the list es empty, only the token type is checked.
     * @return <tt>true</tt> if this token is an identifier and matches one of the given <tt>values</tt> if the list
     * is not empty.
     */
    public boolean isIdentifier(String... values) {
        if (values.length == 0) {
            return is(TokenType.ID);
        }
        for (String value : values) {
            if (matches(TokenType.ID, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if this token is a special identifier.
     * <p>
     * If a list of <tt>triggers</tt> is given, this method checks that the trigger matches one of them.
     *
     * @param triggers the triggers to check for. If the list es empty, only the token type is checked.
     * @return <tt>true</tt> if this token is a special identifier and matches one of the given <tt>triggers</tt>
     * if the list is not empty.
     */
    public boolean isSpecialIdentifier(String... triggers) {
        if (triggers.length == 0) {
            return is(TokenType.SPECIAL_ID);
        }
        for (String possibleTrigger : triggers) {
            if (matches(TokenType.SPECIAL_ID, possibleTrigger)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if this token is a special identifier with the given trigger.
     * <p>
     * If a list of <tt>contents</tt> is given, this method checks that the content matches one of them.
     *
     * @param trigger  the trigger of the special id
     * @param contents the content to check for. If the list es empty, only the token type and the trigger is checked.
     * @return <tt>true</tt> if this token is a special identifier with the given trigger.
     * If <tt>contents</tt> is not empty, the content must also match one of the elements.
     */
    public boolean isSpecialIdentifierWithContent(String trigger, String... contents) {
        if (!matches(TokenType.SPECIAL_ID, trigger)) {
            return false;
        }
        if (contents.length == 0) {
            return true;
        }
        for (String content : contents) {
            if (content != null && content.equals(getContents())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if this token is an integer number.
     *
     * @return <tt>true</tt> if this token is an integer number, <tt>false</tt> otherwise
     */
    public boolean isInteger() {
        return is(TokenType.INTEGER);
    }

    /**
     * Determines if this token is a decimal number.
     *
     * @return <tt>true</tt> if this token is a decimal number, <tt>false</tt> otherwise
     */
    public boolean isDecimal() {
        return is(TokenType.DECIMAL);
    }

    /**
     * Determines if this token is a scientific decimal number (e.g. 3.2e5).
     *
     * @return <tt>true</tt> if this token is a scientific decimal number, <tt>false</tt> otherwise
     */
    public boolean isScientificDecimal() {
        return is(TokenType.SCIENTIFIC_DECIMAL);
    }

    /**
     * Determines if this token is an integer or decimal number.
     *
     * @return <tt>true</tt> if this token is an integer or decimal number, <tt>false</tt> otherwise
     */
    public boolean isNumber() {
        return isInteger() || isDecimal() || isScientificDecimal();
    }

    /**
     * Determines if this token is a string constant
     *
     * @return <tt>true</tt> if this token is a string constant, <tt>false</tt> otherwise
     */
    public boolean isString() {
        return is(TokenType.STRING);
    }

    @Override
    public String toString() {
        return getType().toString() + ":" + getSource() + " (" + line + ":" + pos + ")";
    }
}
