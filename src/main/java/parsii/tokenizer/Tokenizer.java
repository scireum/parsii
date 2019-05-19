/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.tokenizer;

import java.io.Reader;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import parsii.tokenizer.Token.TokenType;

/**
 * Turns a stream of characters ({@link Reader} into a stream of {@link Token}, supporting lookahead.
 * <p>
 * Reads from the given input and parses it into a stream of tokens. By default all token types defined by
 * {@link Token} are supported. Most of the features can be further tweaked by changing the default settings.
 * <p>
 * By default the tokenizer operates as follows:
 * <ul>
 * <li>Consume and ignore any whitespace characters (see {@link Char#isWhitepace()}</li>
 * <li>If the current character starts a line comment, read until the end of the line and ignore all characters
 * consumed.</li>
 * <li>If the current character starts a block comment, read until and end of block comment is detected.</li>
 * <li>If the current character is a digit, parse a INTEGER, if a decimal separator is found, switch over to a DECIMAL
 * (see {@link Char#isDigit()}. Also if the current character is a '-' and the next is a digit, we try to read
 * a number.</li>
 * <li>If the current character is a letter, parse an ID (see {@link Char#isLetter()}. Once this is complete, check if
 * the ID matches one of the supplied keywords, and convert if necessary.</li>
 * <li>If the current character is an opening or closing bracket, a SYMBOL for that single character is returned</li>
 * <li>If the current character is one of the special id starters, all valid ID characters
 * ({@link #isIdentifierChar(Char)} are consumed and returned as SPECIAL_ID</li>
 * <li>All other characters, especially all operators, will be read and returned as one SYMBOL. Therefore <tt>#++*</tt>
 * will be returned as a single symbol.</li>
 * </ul>
 */
public class Tokenizer extends Lookahead<Token> {
    /*
     * Contains the underlying input
     */
    protected LookaheadReader input;
    /*
     * Decimal separator used when detecting decimal numbers
     */
    private char decimalSeparator = '.';
    /*
     * Decimal separator used as output (in the content) when a decimal separator was found
     */
    private char effectiveDecimalSeparator = '.';
    /*
     * Thousand grouping character. Is allowed in any number (at any position) but ignored (not added to the content)
     */
    private char groupingSeparator = '_';
    /*
     * Scientific notation separator (e.g. 3e2 = 3*10**2 = 300)
     */
    private char scientificNotationSeparator = 'e';
    private char alternateScientificNotationSeparator = 'E';
    /*
     * Scientific notation separator used as output (in the content) when a scientific notation separator was found
     */
    private char effectiveScientificNotationSeparator = 'e';
    /*
     * Initiates a line comment
     */
    private String lineComment = "//";
    /*
     * Starts a block comment
     */
    private String blockCommentStart = "/*";
    /*
     * Ends a block comment
     */
    private String blockCommentEnd = "*/";
    /*
     * All supported brackets. For obvious reasons, several brackets like (( are treated as two symbols, rather than
     * operators like ** which will create one symbol
     */
    private char[] brackets = {'(', '[', '{', '}', ']', ')'};
    /*
     * Determines if a single pipe (this: | ) will be treated as bracket. This could can be used like | a - b |
     * However || will be handled as symbol with two characters, as it is often used as "or".
     */
    private boolean treatSinglePipeAsBracket = true;
    /*
     * These characters are used to identify the start of a SPECIAL_ID like "$test"
     */
    private Set<Character> specialIdStarters = new HashSet<>();
    /*
     * These characters are used to identify the end of a SPECIAL_ID like "test:"
     */
    private Set<Character> specialIdTerminators = new HashSet<>();
    /*
     * Contains keywords which will cause IDs to be converted to KEYWORD if the name matches
     */
    private Map<String, String> keywords = new IdentityHashMap<>();
    /*
     * Determines if keywords are case sensitive
     */
    private boolean keywordsCaseSensitive = false;
    /*
     * Contains all characters which are used to delimit a string, and also a second character which is used to
     * escape characters within this string. '\0' means no escaping.
     */
    private Map<Character, Character> stringDelimiters = new IdentityHashMap<>();

    /**
     * Creates a new tokenizer for the given input
     *
     * @param input the input to parse. The reader will be buffered by the implementation so that it can be effectively
     *              read character b character.
     */
    public Tokenizer(Reader input) {
        this.input = new LookaheadReader(input);
        this.input.setProblemCollector(problemCollector);

        // Setup default string handling
        addStringDelimiter('"', '\\');
        addStringDelimiter('\'', '\0');
    }

    @Override
    public void setProblemCollector(List<ParseError> problemCollector) {
        super.setProblemCollector(problemCollector);
        this.input.setProblemCollector(problemCollector);
    }

    @Override
    protected Token endOfInput() {
        return Token.createAndFill(Token.TokenType.EOI, input.current());
    }

    @Override
    protected Token fetch() {
        // Fetch and ignore any whitespace
        while (input.current().isWhitepace()) {
            input.consume();
        }

        // End of input reached? Pass end of input signal on...
        if (input.current().isEndOfInput()) {
            return null;
        }

        // Handle (and ignore) line comments
        if (isAtStartOfLineComment(true)) {
            skipToEndOfLine();
            return fetch();
        }

        // Handle (and ignore) block comments
        if (isAtStartOfBlockComment(true)) {
            skipBlockComment();
            return fetch();
        }

        // A digit signals the start of a number
        if (isAtStartOfNumber()) {
            return fetchNumber();
        }

        // A letter signals the start of an id
        if (isAtStartOfIdentifier()) {
            return fetchId();
        }

        // A " or ' (or whatever string delimiters are used...) start a string constant
        if (stringDelimiters.containsKey(input.current().getValue())) {
            return fetchString();
        }

        // Treat brackets as special symbols: (( will create two consecutive symbols but ** will create a single
        // symbol "**".
        if (isAtBracket(false)) {
            return Token.createAndFill(Token.TokenType.SYMBOL, input.consume());
        }

        // Check if the current character starts a special ID
        if (isAtStartOfSpecialId()) {
            return fetchSpecialId();
        }

        // Read all symbol characters and form a SYMBOL of it
        if (isSymbolCharacter(input.current())) {
            return fetchSymbol();
        }

        problemCollector.add(ParseError.error(input.current(),
                                              String.format("Invalid character in input: '%s'",
                                                            input.current().getStringValue())));
        input.consume();
        return fetch();
    }

    /**
     * Determines if the underlying input is looking at the start of a special id.
     * <p>
     * By default this is one of the given <tt>specialIdStarters</tt>.
     *
     * @return <tt>true</tt> if the current input is the start of a special id, <tt>false</tt> otherwise
     */
    protected boolean isAtStartOfSpecialId() {
        return specialIdStarters.contains(input.current().getValue());
    }

    /**
     * Determines if the underlying input is looking at the start of a number.
     * <p>
     * By default this is either indicated by a digit or by '-' followed by a digit or a '.' followed by a digit.
     *
     * @return <tt>true</tt> if the current input is the start of a numeric constant, <tt>false</tt> otherwise
     */
    @SuppressWarnings("squid:S1067")
    protected boolean isAtStartOfNumber() {
        return input.current().isDigit()
               || input.current().is('-') && input.next().isDigit()
               || input.current().is('-') && input.next().is('.') && input.next(2).isDigit()
               || input.current().is('.') && input.next().isDigit();
    }

    /**
     * Determines if the underlying input is looking at a bracket.
     * <p>
     * By default all supplied <tt>brackets</tt> are checked. If <tt>treatSinglePipeAsBracket</tt> is true, a
     * single '|' is also treated as bracket.
     *
     * @param inSymbol determines if we're already parsing a symbol or just trying to decide what the next token is
     * @return <tt>true</tt> if the current input is an opening or closing bracket
     */
    @SuppressWarnings("squid:S1067")
    protected boolean isAtBracket(boolean inSymbol) {
        return input.current().is(brackets) || !inSymbol
                                               && treatSinglePipeAsBracket
                                               && input.current().is('|')
                                               && !input.next().is('|');
    }

    /**
     * Checks if the next characters, starting from the current, match the given string.
     *
     * @param string  the string to check
     * @param consume determines if the matched string should be consumed immediately
     * @return <tt>true</tt> if the next characters of the input match the given string, <tt>false</tt>
     * otherwise
     */
    protected boolean canConsumeThisString(String string, boolean consume) {
        if (string == null) {
            return false;
        }
        for (int i = 0; i < string.length(); i++) {
            if (!input.next(i).is(string.charAt(i))) {
                return false;
            }
        }
        if (consume) {
            input.consume(string.length());
        }
        return true;
    }

    /**
     * Checks if the underlying input is looking at a start of line comment.
     * <p>
     * If a line comment is detected, any characters indicating this are consumed by this method if
     * <tt>consume</tt> is <tt>true</tt>.
     *
     * @param consume determines if the matched comment start should be consumed immediately
     * @return <tt>true</tt> if the next character(s) of the input start a line comment, <tt>false</tt> otherwise
     */
    protected boolean isAtStartOfLineComment(boolean consume) {
        if (lineComment != null) {
            return canConsumeThisString(lineComment, consume);
        } else {
            return false;
        }
    }

    /**
     * Read everything upon (and including) the next line break
     */
    protected void skipToEndOfLine() {
        while (!input.current().isEndOfInput() && !input.current().isNewLine()) {
            input.consume();
        }
    }

    /**
     * Checks if the underlying input is looking at a start of block comment
     * <p>
     * If a block comment is detected, any characters indicating this are consumed by this method if <tt>consume</tt>
     * is <tt>true</tt> .
     *
     * @param consume determines if the block comment starter is to be consumed if found or not
     * @return <tt>true</tt> if the next character(s) of the input start a block comment, <tt>false</tt> otherwise
     */
    protected boolean isAtStartOfBlockComment(boolean consume) {
        return canConsumeThisString(blockCommentStart, consume);
    }

    /**
     * Checks if the underlying input is looking at a end of block comment
     * <p>
     * If an end of block comment is detected, any characters indicating this are consumed by this method
     *
     * @return <tt>true</tt> if the next character(s) of the input end a block comment, <tt>false</tt> otherwise
     */
    protected boolean isAtEndOfBlockComment() {
        return canConsumeThisString(blockCommentEnd, true);
    }

    /**
     * Checks if we're looking at an end of block comment
     */
    protected void skipBlockComment() {
        while (!input.current().isEndOfInput()) {
            if (isAtEndOfBlockComment()) {
                return;
            }
            input.consume();
        }
        problemCollector.add(ParseError.error(input.current(), "Premature end of block comment"));
    }

    /**
     * Reads and returns a string constant.
     *
     * @return the parsed string constant a Token
     */
    protected Token fetchString() {
        char separator = input.current().getValue();
        char escapeChar = stringDelimiters.get(input.current().getValue());
        Token result = Token.create(Token.TokenType.STRING, input.current());
        result.addToTrigger(input.consume());
        while (!input.current().isNewLine() && !input.current().is(separator) && !input.current().isEndOfInput()) {
            if (escapeChar != '\0' && input.current().is(escapeChar)) {
                result.addToSource(input.consume());
                if (!handleStringEscape(separator, escapeChar, result)) {
                    problemCollector.add(ParseError.error(input.next(),
                                                          String.format("Cannot use '%s' as escaped character",
                                                                        input.next().getStringValue())));
                }
            } else {
                result.addToContent(input.consume());
            }
        }
        if (input.current().is(separator)) {
            result.addToSource(input.consume());
        } else {
            problemCollector.add(ParseError.error(input.current(), "Premature end of string constant"));
        }
        return result;
    }

    /**
     * Evaluates an string escape like \n
     * <p>
     * The escape character is already consumed. Therefore the input points at the character to escape. This method
     * must consume all escaped characters.
     *
     * @param separator   the delimiter of this string constant
     * @param escapeChar  the escape character used
     * @param stringToken the resulting string constant
     * @return <tt>true</tt> if an escape was possible, <tt>false</tt> otherwise
     */
    protected boolean handleStringEscape(char separator, char escapeChar, Token stringToken) {
        if (input.current().is(separator)) {
            stringToken.addToContent(separator);
            stringToken.addToSource(input.consume());
            return true;
        } else if (input.current().is(escapeChar)) {
            stringToken.silentAddToContent(escapeChar);
            stringToken.addToSource(input.consume());
            return true;
        } else if (input.current().is('n')) {
            stringToken.silentAddToContent('\n');
            stringToken.addToSource(input.consume());
            return true;
        } else if (input.current().is('r')) {
            stringToken.silentAddToContent('\r');
            stringToken.addToSource(input.consume());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines if the underlying input is looking at a valid character to start an identifier
     * <p>
     * By default, only letters can start identifiers
     *
     * @return <tt>true</tt> if the underlying input is looking at a valid identifier starter, <tt>false</tt> otherwise
     */
    protected boolean isAtStartOfIdentifier() {
        return input.current().isLetter();
    }

    /**
     * Reads and returns an identifier
     *
     * @return the parsed identifier as Token
     */
    protected Token fetchId() {
        Token result = Token.create(Token.TokenType.ID, input.current());
        result.addToContent(input.consume());
        while (isIdentifierChar(input.current())) {
            result.addToContent(input.consume());
        }
        if (!input.current().isEndOfInput() && specialIdTerminators.contains(input.current().getValue())) {
            Token specialId = Token.create(Token.TokenType.SPECIAL_ID, result);
            specialId.setTrigger(input.current().getStringValue());
            specialId.setContent(result.getContents());
            specialId.setSource(result.getContents());
            specialId.addToSource(input.current());
            input.consume();
            return handleKeywords(specialId);
        }
        return handleKeywords(result);
    }

    /**
     * Checks if the given identifier is a keyword and returns an appropriate Token
     *
     * @param idToken the identifier to check
     * @return a keyword Token if the given identifier was a keyword, the original Token otherwise
     */
    protected Token handleKeywords(Token idToken) {
        String keyword = keywords.get(keywordsCaseSensitive ?
                                      idToken.getContents().intern() :
                                      idToken.getContents().toLowerCase().intern());
        if (keyword != null) {
            Token keywordToken = Token.create(Token.TokenType.KEYWORD, idToken);
            keywordToken.setTrigger(keyword);
            keywordToken.setContent(idToken.getContents());
            keywordToken.setSource(idToken.getSource());

            return keywordToken;
        }

        return idToken;
    }

    /**
     * Determines if the given Char is a valid identifier part.
     * <p>
     * By default, letters, digits and '_' are valid identifier parts.
     *
     * @param current the character to check
     * @return <tt>true</tt> if the given Char is a valid identifier part, <tt>false</tt> otherwise
     */
    protected boolean isIdentifierChar(Char current) {
        return current.isDigit() || current.isLetter() || current.is('_');
    }

    /**
     * Reads and returns a special id.
     *
     * @return the parsed special id as Token
     */
    protected Token fetchSpecialId() {
        Token result = Token.create(Token.TokenType.SPECIAL_ID, input.current());
        result.addToTrigger(input.consume());
        while (isIdentifierChar(input.current())) {
            result.addToContent(input.consume());
        }
        return handleKeywords(result);
    }

    /**
     * Reads and returns a symbol.
     * <p>
     * A symbol are one or two characters, which don't match any other token type. In most cases, this will be
     * operators like + or *.
     *
     * @return the parsed symbol as Token
     */
    @SuppressWarnings("squid:S1067")
    protected Token fetchSymbol() {
        Token result = Token.create(Token.TokenType.SYMBOL, input.current());
        result.addToTrigger(input.consume());
        if (result.isSymbol("*") && input.current().is('*')
            || result.isSymbol("&") && input.current().is('&')
            || result.isSymbol("|") && input.current().is('|')
            || result.isSymbol() && input.current().is('=')) {
            result.addToTrigger(input.consume());
        }
        return result;
    }

    /**
     * Determines if the given Char is a symbol character.
     * <p>
     * By default these are all non-control characters, which don't match any other class (letter, digit, whitepsace)
     *
     * @param ch the character to check
     * @return <tt>true</tt> if the given character is a valid symbol character, <tt>false</tt> otherwise
     */
    @SuppressWarnings("squid:S1067")
    protected boolean isSymbolCharacter(Char ch) {
        if (ch.isEndOfInput() || ch.isDigit() || ch.isLetter() || ch.isWhitepace()) {
            return false;
        }

        char c = ch.getValue();
        if (Character.isISOControl(c)) {
            return false;
        }

        return !(isAtBracket(true)
                 || isAtStartOfBlockComment(false)
                 || isAtStartOfLineComment(false)
                 || isAtStartOfNumber()
                 || isAtStartOfIdentifier()
                 || stringDelimiters.containsKey(ch.getValue()));
    }

    /**
     * Reads and returns a number.
     *
     * @return the parsed number as Token
     */
    protected Token fetchNumber() {
        Token result = Token.create(Token.TokenType.INTEGER, input.current());
        result.addToContent(input.consume());
        while (input.current().isDigit()
            || input.current().is(decimalSeparator)
            || (input.current().is(groupingSeparator) && input.next().isDigit())
            || ((input.current().is(scientificNotationSeparator)
               || input.current().is(alternateScientificNotationSeparator))
               && (input.next().isDigit() || input.next().is('+') || input.next().is('-')))) {
            if (input.current().is(groupingSeparator)) {
                result.addToSource(input.consume());
            } else if (input.current().is(decimalSeparator)) {
                if (result.is(Token.TokenType.DECIMAL) || result.is(TokenType.SCIENTIFIC_DECIMAL)) {
                    problemCollector.add(ParseError.error(input.current(), "Unexpected decimal separators"));
                } else {
                    Token decimalToken = Token.create(Token.TokenType.DECIMAL, result);
                    decimalToken.setContent(result.getContents() + effectiveDecimalSeparator);
                    decimalToken.setSource(result.getSource());
                    result = decimalToken;
                }
                result.addToSource(input.consume());
            } else if (input.current().is(scientificNotationSeparator)
                || input.current().is(alternateScientificNotationSeparator)) {
                if (result.is(TokenType.SCIENTIFIC_DECIMAL)) {
                    problemCollector.add(ParseError.error(input.current(), "Unexpected scientific notation separators"));
                } else {
                    Token scientificDecimalToken = Token.create(TokenType.SCIENTIFIC_DECIMAL, result);
                    scientificDecimalToken.setContent(result.getContents() + effectiveScientificNotationSeparator);
                    scientificDecimalToken.setSource(result.getSource() + effectiveScientificNotationSeparator);
                    result = scientificDecimalToken;
                    input.consume();
                    if (input.current().is('+') || input.current().is('-')){
                        result.addToContent(input.consume());
                    }
                }
            } else {
                result.addToContent(input.consume());
            }
        }

        return result;
    }

    /**
     * Determines if keywords are case sensitive.
     * <p>
     * By default, keywords aren't case sensitive. Therefore True and true are the same keyword.
     *
     * @return <tt>true</tt> if keywords are case sensitive, <tt>false</tt> otherwise
     */
    public boolean isKeywordsCaseSensitive() {
        return keywordsCaseSensitive;
    }

    /**
     * Sets the case sensitiveness of keywords.
     * <p>
     * This must be setup before any call to {@link #addKeyword(String)} as this will determine internal data
     * structures
     *
     * @param keywordsCaseSensitive <tt>true</tt> if keywords should be treated as case sensitive, <tt>false</tt>
     *                              otherwise (default)
     */
    public void setKeywordsCaseSensitive(boolean keywordsCaseSensitive) {
        this.keywordsCaseSensitive = keywordsCaseSensitive;
    }

    /**
     * Adds a keyword which is now being recognized by the tokenizer
     * <p>
     * Detection will be case insensitive. Only ID tokens (identifiers) are checked against the given keywords,
     * therefore a keyword must be a valid identifier.
     *
     * @param keyword the keyword to be added to the list of known keywords.
     */
    public void addKeyword(String keyword) {
        keywords.put(keywordsCaseSensitive ? keyword.intern() : keyword.toLowerCase().intern(), keyword);
    }

    /**
     * Adds character as a special id starter. The given character must be followed by a valid id to be recognized
     * as SPECIAL_ID.
     *
     * @param character the character to be added as special id starter
     */
    public void addSpecialIdStarter(char character) {
        specialIdStarters.add(character);
    }

    /**
     * Adds character as a special id terminator. The given character can be placed at the end of an id
     * to be recognized as SPECIAL_ID.
     *
     * @param character the character to be added as special id terminator
     */
    public void addSpecialIdTerminator(char character) {
        specialIdTerminators.add(character);
    }

    /**
     * Removes all previously registered string delimiters.
     * <p>
     * By default " and ' are registered as string delimiters, where string enclosed by " can have characters
     * escaped by \
     */
    public void clearStringDelimiters() {
        stringDelimiters.clear();
    }

    /**
     * Adds a new string delimiter character along with the character used to escape string within it.
     *
     * @param stringDelimiter the delimiter used to start and end string constants
     * @param escapeCharacter the character used to start an escape sequence or \0 to indicate that escaping is
     *                        not supported
     */
    public void addStringDelimiter(char stringDelimiter, char escapeCharacter) {
        stringDelimiters.put(stringDelimiter, escapeCharacter);
    }

    /**
     * Boilerplate method for adding a string delimiter which does not support escape sequences.
     *
     * @param stringDelimiter the delimiter used to start and end string constants
     */
    public void addUnescapedStringDelimiter(char stringDelimiter) {
        stringDelimiters.put(stringDelimiter, '\0');
    }

    /**
     * Returns the decimal separator used in decimal numbers
     * <p>
     * The default separator used is '.'
     *
     * @return the character which is detected as separator between the integer and the decimal part of a number
     */
    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * Sets the character which is recognized as decimal separator.
     *
     * @param decimalSeparator the character to be recognized as decimal separator
     */
    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    /**
     * Returns the decimal separator used in the content of DECIMAL tokens.
     * <p>
     * The default separator used is '.'. When adapting this for language dependent inputs (e.g. using ',' as
     * decimal separator) this value should probably not be changed, as it is used in the output (content) of the
     * Tokens and has no effect what kind of numbers are being accepted.
     *
     * @return the decimal separator used in tokens
     */
    public char getEffectiveDecimalSeparator() {
        return effectiveDecimalSeparator;
    }

    /**
     * Sets the decimal separator used in the content of DECIMAL tokens. This can differ from the character set via
     * {@link #setDecimalSeparator(char)} which is used to recognize decimal numbers. Therefore language dependent
     * input can be parsed with a constant output being language independent.
     *
     * @param effectiveDecimalSeparator the character used as decimal separator in the content of decimal tokens
     */
    public void setEffectiveDecimalSeparator(char effectiveDecimalSeparator) {
        this.effectiveDecimalSeparator = effectiveDecimalSeparator;
    }

    /**
     * Returns the grouping separator which can be used in numbers for group digits (e.g. in thousands).
     * <p>
     * This character will be accepted in numbers, but ignored (not added to the content). The default value is '_'.
     *
     * @return the grouping separator accepting in numbers
     */
    public char getGroupingSeparator() {
        return groupingSeparator;
    }

    /**
     * Sets the grouping separator accepting in numbers.
     *
     * @param groupingSeparator the character which can be used to group digits in numbers
     */
    public void setGroupingSeparator(char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
    }

    /**
     * Returns the string which starts a line comment.
     * <p>
     * The default value is '/''/'
     *
     * @return the string sequence starting a line comment
     */
    public String getLineComment() {
        return lineComment;
    }

    /**
     * Sets the string which stats a line comment.
     *
     * @param lineComment the string used to detect a line comment
     */
    public void setLineComment(String lineComment) {
        this.lineComment = lineComment;
    }

    /**
     * Returns the string which starts a block comment.
     * <p>
     * The default value is '/''*'
     *
     * @return the string sequence starting a block comment
     */
    public String getBlockCommentStart() {
        return blockCommentStart;
    }

    /**
     * Sets the string which stats a block comment.
     *
     * @param blockCommentStart the string used to detect a block comment
     */
    public void setBlockCommentStart(String blockCommentStart) {
        this.blockCommentStart = blockCommentStart;
    }

    /**
     * Returns the string which ends a block comment.
     * <p>
     * The default value is '*''/'
     *
     * @return the string sequence ending a block comment
     */
    public String getBlockCommentEnd() {
        return blockCommentEnd;
    }

    /**
     * Sets the string which ends a block comment.
     *
     * @param blockCommentEnd the string used to detect the end of a block comment
     */
    public void setBlockCommentEnd(String blockCommentEnd) {
        this.blockCommentEnd = blockCommentEnd;
    }

    @Override
    public String toString() {
        // We check the internal buffer first to that no further parsing is triggered by calling toString()
        // as it is frequently invoked by the debugger which causes nasty side-effects otherwise
        if (itemBuffer.isEmpty()) {
            return "No Token fetched...";
        }
        if (itemBuffer.size() < 2) {
            return "Current: " + current();
        }
        return "Current: " + current().toString() + ", Next: " + next().toString();
    }

    /**
     * Boilerplate method for {@code current().isNotEnd()}
     *
     * @return <tt>true</tt> if the current token is not an "end of input" token, <tt>false</tt> otherwise.
     */
    public boolean more() {
        return current().isNotEnd();
    }

    /**
     * Boilerplate method for {@code current().isEnd()}
     *
     * @return <tt>true</tt> if the current token is an "end of input" token, <tt>false</tt> otherwise.
     */
    public boolean atEnd() {
        return current().isEnd();
    }

    /**
     * Adds a parse error to the internal problem collector.
     * <p>
     * It is preferred to collect as much errors as possible and then fail with an exception instead of failing
     * at the first problem. Often syntax errors can be worked out by the parser and we can report a set of
     * errors at once.
     *
     * @param pos        the position of the error. Note that {@link Token} implements {@link Position}. Therefore the
     *                   current token is often a good choice for this parameter.
     * @param message    the message to describe the error. Can contain formatting parameters like %s or %d as defined
     *                   by {@link String#format(String, Object...)}
     * @param parameters Contains the parameters used to format the given message
     */
    public void addError(Position pos, String message, Object... parameters) {
        getProblemCollector().add(ParseError.error(pos, String.format(message, parameters)));
    }

    /**
     * Adds a warning to the internal problem collector.
     * <p>
     * A warning indicates an anomaly which might lead to an error but still, the parser can continue to complete its
     * work.
     *
     * @param pos        the position of the warning. Note that {@link Token} implements {@link Position}.
     *                   Therefore the current token is often a good choice for this parameter.
     * @param message    the message to describe the earning. Can contain formatting parameters like %s or %d as
     *                   defined by {@link String#format(String, Object...)}
     * @param parameters Contains the parameters used to format the given message
     */
    public void addWarning(Position pos, String message, Object... parameters) {
        getProblemCollector().add(ParseError.warning(pos, String.format(message, parameters)));
    }

    /**
     * Consumes the current token, expecting it to be as <tt>SYMBOL</tt> with the given content
     *
     * @param symbol the expected trigger of the current token
     */
    public void consumeExpectedSymbol(String symbol) {
        if (current().matches(Token.TokenType.SYMBOL, symbol)) {
            consume();
        } else {
            addError(current(), "Unexpected token: '%s'. Expected: '%s'", current().getSource(), symbol);
        }
    }

    /**
     * Consumes the current token, expecting it to be as <tt>KEYWORD</tt> with the given content
     *
     * @param keyword the expected content of the current token
     */
    public void consumeExpectedKeyword(String keyword) {
        if (current().matches(Token.TokenType.KEYWORD, keyword)) {
            consume();
        } else {
            addError(current(), "Unexpected token: '%s'. Expected: '%s'", current().getSource(), keyword);
        }
    }

    /**
     * Throws a {@link ParseException} if an error or warning occurred while parsing the input
     *
     * @throws ParseException if an error or warning occurred while parsing.
     */
    public void throwOnErrorOrWarning() throws ParseException {
        if (!getProblemCollector().isEmpty()) {
            throw ParseException.create(getProblemCollector());
        }
    }

    /**
     * Throws a {@link ParseException} if an error occurred while parsing the input.
     * <p>
     * All warnings which occurred will be ignored.
     *
     * @throws ParseException if an error occurred while parsing.
     */
    public void throwOnError() throws ParseException {
        for (ParseError e : getProblemCollector()) {
            if (e.getSeverity() == ParseError.Severity.ERROR) {
                throw ParseException.create(getProblemCollector());
            }
        }
    }
}
