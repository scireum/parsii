/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

import static java.math.BigDecimal.ZERO;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.corba.se.spi.extension.ZeroPortPolicy;

import parsii.tokenizer.ParseError;
import parsii.tokenizer.ParseException;
import parsii.tokenizer.Token;
import parsii.tokenizer.Tokenizer;

/**
 * Parses a given mathematical expression into an abstract syntax tree which can be evaluated.
 * <p>
 * Takes a string input as String or Reader which will be translated into an {@link Expression}. If one or more errors
 * occur, a {@link ParseException} will be thrown. The parser tries to continue as long a possible to provide good
 * insight into the errors within the expression.
 * </p>
 * <p>
 * This is a recursive descending parser which has a method per non-terminal.
 * </p>
 * <p>
 * Using this parser is as easy as:
 * <code>
 * Scope scope = Scope.create();
 * Variable a = scope.getVariable("a");
 * Expression expr = Parser.parse("3 + a * 4");
 * a.setValue(4);
 * System.out.println(expr.evaluate());
 * a.setValue(5);
 * System.out.println(expr.evaluate());
 * </code>
 * </p>
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public class Parser {
    private final Scope scope;
    private List<ParseError> errors = new ArrayList<ParseError>();
    private Tokenizer tokenizer;
    private static Map<String, Function> functionTable;

    /**
     * Registers a new function which can be referenced from within an expression.
     * <p>
     * A function must be registered before an expression is parsed in order to be visible.
     * </p>
     *
     * @param name     the name of the function. If a function with the same name is already available, it will be
     *                 overridden
     * @param function the function which is invoked as an expression is evaluated
     */
    public static void registerFunction(String name, Function function) {
        functionTable.put(name, function);
    }

    /*
     * Setup well known functions
     */
    static {
        functionTable = new TreeMap<String, Function>();

        registerFunction("sin", Functions.SIN);
        registerFunction("cos", Functions.COS);
        registerFunction("tan", Functions.TAN);
        registerFunction("sinh", Functions.SINH);
        registerFunction("cosh", Functions.COSH);
        registerFunction("tanh", Functions.TANH);
        registerFunction("asin", Functions.ASIN);
        registerFunction("acos", Functions.ACOS);
        registerFunction("atan", Functions.ATAN);
        registerFunction("atan2", Functions.ATAN2);
        registerFunction("deg", Functions.DEG);
        registerFunction("rad", Functions.RAD);
        registerFunction("abs", Functions.ABS);
        registerFunction("round", Functions.ROUND);
        registerFunction("ceil", Functions.CEIL);
        registerFunction("floor", Functions.FLOOR);
        registerFunction("exp", Functions.EXP);
        registerFunction("ln", Functions.LN);
        registerFunction("log", Functions.LOG);
        registerFunction("sqrt", Functions.SQRT);
        registerFunction("min", Functions.MIN);
        registerFunction("max", Functions.MAX);
        registerFunction("rnd", Functions.RND);
        registerFunction("sign", Functions.SIGN);
        registerFunction("if", Functions.IF);
    }

    /**
     * Parses the given input into an expression.
     *
     * @param input the expression to be parsed
     * @throws ParseException if the expression contains one or more errors
     */
    public static Expression parse(String input) throws ParseException {
        return new Parser(new StringReader(input), Scope.create()).parse();
    }

    /**
     * Parses the given input into an expression.
     *
     * @param input the expression to be parsed
     * @throws ParseException if the expression contains one or more errors
     */
    public static Expression parse(Reader input) throws ParseException {
        return new Parser(input, Scope.create()).parse();
    }

    /**
     * Parses the given input into an expression.
     * <p>Referenced variables will be resolved using the given Scope</p>
     *
     * @param input the expression to be parsed
     * @param scope the scope used to resolve variables
     * @throws ParseException if the expression contains one or more errors
     */
    public static Expression parse(String input, Scope scope) throws ParseException {
        return new Parser(new StringReader(input), scope).parse();
    }

    /**
     * Parses the given input into an expression.
     * <p>Referenced variables will be resolved using the given Scope</p>
     *
     * @param input the expression to be parsed
     * @param scope the scope used to resolve variables
     * @throws ParseException if the expression contains one or more errors
     */
    public static Expression parse(Reader input, Scope scope) throws ParseException {
        return new Parser(input, scope).parse();
    }


    /*
     * Use one of the static methods to parse an expression
     */
    private Parser(Reader input, Scope scope) {
        this.scope = scope;
        tokenizer = new Tokenizer(input);
        tokenizer.setProblemCollector(errors);
    }

    /**
     * Parses the expression in <tt>input</tt>
     *
     * @return the parsed expression
     * @throws ParseException if the expression contains one or more errors
     */
    protected Expression parse() throws ParseException {
        Expression result = expression().simplify();
        if (errors.size() > 0) {
            throw ParseException.create(errors);
        }
        return result;
    }

    /**
     * Parser rule for parsing an expression.
     * <p>
     * This is the root rule. An expression is a <tt>relationalExpression</tt> which might be followed by a logical
     * operator (&& or ||) and another <tt>expression</tt>.
     * </p>
     *
     * @return an expression parsed from the given input
     */
    protected Expression expression() {
        Expression left = relationalExpression();
        if (tokenizer.current().isSymbol("&&")) {
            tokenizer.consume();
            Expression right = expression();
            return reOrder(left, right, BinaryOperation.Op.AND);
        } else if (tokenizer.current().isSymbol("||")) {
            tokenizer.consume();
            Expression right = expression();
            return reOrder(left, right, BinaryOperation.Op.OR);
        }
        return left;
    }

    /**
     * Parser rule for parsing a relational expression.
     * <p>
     * A relational expression is a <tt>term</tt> which might be followed by a relational operator
     * (&lt;,&lt;=,...,&gt;) and another <tt>relationalExpression</tt>.
     * </p>
     *
     * @return a relational expression parsed from the given input
     */
    protected Expression relationalExpression() {
        Expression left = term();
        if (tokenizer.current().isSymbol("<")) {
            tokenizer.consume();
            Expression right = relationalExpression();
            return reOrder(left, right, BinaryOperation.Op.LT);
        }
        if (tokenizer.current().isSymbol("<=")) {
            tokenizer.consume();
            Expression right = relationalExpression();
            return reOrder(left, right, BinaryOperation.Op.LT_EQ);
        }
        if (tokenizer.current().isSymbol("=")) {
            tokenizer.consume();
            Expression right = relationalExpression();
            return reOrder(left, right, BinaryOperation.Op.EQ);
        }
        if (tokenizer.current().isSymbol(">=")) {
            tokenizer.consume();
            Expression right = relationalExpression();
            return reOrder(left, right, BinaryOperation.Op.GT_EQ);
        }
        if (tokenizer.current().isSymbol(">")) {
            tokenizer.consume();
            Expression right = relationalExpression();
            return reOrder(left, right, BinaryOperation.Op.GT);
        }
        if (tokenizer.current().isSymbol("!=")) {
            tokenizer.consume();
            Expression right = relationalExpression();
            return reOrder(left, right, BinaryOperation.Op.NEQ);
        }
        return left;
    }

    /**
     * Parser rule for parsing a term.
     * <p>
     * A term is a <tt>product</tt> which might be followed by + or - as operator and another <tt>term</tt>.
     * </p>
     *
     * @return a term parsed from the given input
     */
    protected Expression term() {
        Expression left = product();
        if (tokenizer.current().isSymbol("+")) {
            tokenizer.consume();
            Expression right = term();
            return reOrder(left, right, BinaryOperation.Op.ADD);
        }
        if (tokenizer.current().isSymbol("-")) {
            tokenizer.consume();
            Expression right = term();
            return reOrder(left, right, BinaryOperation.Op.SUBTRACT);
        }
        if (tokenizer.current().isNumber()) {
            if (tokenizer.current().getContents().startsWith("-")) {
                Expression right = term();
                return reOrder(left, right, BinaryOperation.Op.ADD);
            }
        }

        return left;
    }

    /**
     * Parser rule for parsing a product.
     * <p>
     * A product is a <tt>power</tt> which might be followed by *, / or % as operator and another <tt>product</tt>.
     * </p>
     *
     * @return a product parsed from the given input
     */
    protected Expression product() {
        Expression left = power();
        if (tokenizer.current().isSymbol("*")) {
            tokenizer.consume();
            Expression right = product();
            return reOrder(left, right, BinaryOperation.Op.MULTIPLY);
        }
        if (tokenizer.current().isSymbol("/")) {
            tokenizer.consume();
            Expression right = product();
            return reOrder(left, right, BinaryOperation.Op.DIVIDE);
        }
        if (tokenizer.current().isSymbol("%")) {
            tokenizer.consume();
            Expression right = product();
            return reOrder(left, right, BinaryOperation.Op.MODULO);
        }
        return left;
    }

    /**
     * Reorders the operands of the given operation in order to generate a "left handed" AST which performs evaluations
     * in natural order (from left to right).
     */
    private Expression reOrder(Expression left, Expression right, BinaryOperation.Op op) {
        if (right instanceof BinaryOperation) {
            BinaryOperation rightOp = (BinaryOperation) right;
            if (!rightOp.isSealed() && rightOp.getOp().getPriority() == op.getPriority()) {
                replaceLeft(rightOp, left, op);
                return right;
            }
        }
        return new BinaryOperation(op, left, right);
    }

    private void replaceLeft(BinaryOperation target, Expression newLeft, BinaryOperation.Op op) {
        if (target.getLeft() instanceof BinaryOperation) {
            BinaryOperation leftOp = (BinaryOperation) target.getLeft();
            if (!leftOp.isSealed() && leftOp.getOp().getPriority() == op.getPriority()) {
                replaceLeft(leftOp, newLeft, op);
                return;
            }
        }
        target.setLeft(new BinaryOperation(op, newLeft, target.getLeft()));
    }

    /**
     * Parser rule for parsing a power.
     * <p>
     * A power is an <tt>atom</tt> which might be followed by ^ or ** as operator and another <tt>power</tt>.
     * </p>
     *
     * @return a power parsed from the given input
     */
    protected Expression power() {
        Expression left = atom();
        if (tokenizer.current().isSymbol("^") || tokenizer.current().isSymbol("**")) {
            tokenizer.consume();
            Expression right = power();
            return reOrder(left, right, BinaryOperation.Op.POWER);
        }
        return left;
    }

    /**
     * Parser rule for parsing an atom.
     * <p>
     * An atom is either a numeric constant, an <tt>expression</tt> in brackets, an <tt>expression</tt> surrounded by
     * | to signal the absolute function, an identifier to signal a variable reference or an identifier followed by a
     * bracket to signal a function call.
     * </p>
     *
     * @return an atom parsed from the given input
     */
    protected Expression atom() {
        if (tokenizer.current().isSymbol("-")) {
            tokenizer.consume();
            BinaryOperation result = new BinaryOperation(BinaryOperation.Op.SUBTRACT, new Constant(ZERO), atom());
            result.seal();
            return result;
        }
        if (tokenizer.current().isSymbol("(")) {
            tokenizer.consume();
            Expression result = expression();
            if (result instanceof BinaryOperation) {
                ((BinaryOperation) result).seal();
            }
            expect(Token.TokenType.SYMBOL, ")");
            return result;
        }
        if (tokenizer.current().isSymbol("|")) {
            tokenizer.consume();
            FunctionCall call = new FunctionCall();
            call.addParameter(expression());
            call.setFunction(Functions.ABS);
            expect(Token.TokenType.SYMBOL, "|");
            return call;
        }
        if (tokenizer.current().isIdentifier()) {
            if (tokenizer.next().isSymbol("(")) {
                return functionCall();
            }
            return new VariableReference(scope.getVariable(tokenizer.consume().getContents()));
        }
        if (tokenizer.current().isNumber()) {
            BigDecimal value = new BigDecimal (tokenizer.consume().getContents());
            if (tokenizer.current().is(Token.TokenType.ID)) {
                String quantifier = tokenizer.current().getContents().intern();
                if ("n" == quantifier) {
                    value = value.movePointLeft(9);
                    tokenizer.consume();
                } else if ("u" == quantifier) {
                    value = value.movePointLeft(6);
                    tokenizer.consume();
                } else if ("m" == quantifier) {
                    value = value.movePointLeft(3);
                    tokenizer.consume();
                } else if ("K" == quantifier || "k" == quantifier) {
                    value = value.movePointRight(3);
                    tokenizer.consume();
                } else if ("M" == quantifier) {
                    value = value.movePointRight(6);
                    tokenizer.consume();
                } else if ("G" == quantifier) {
                    value = value.movePointRight(9);
                    tokenizer.consume();
                }
            }
            return new Constant(value);
        }
        Token token = tokenizer.consume();
        errors.add(ParseError.error(token,
                String.format("Unexpected token: '%s'. Expected an expression.",
                        token.getSource())));
        return Constant.EMPTY;
    }

    /**
     * Parses a function call.
     *
     * @return the function call as Expression
     */
    protected Expression functionCall() {
        FunctionCall call = new FunctionCall();
        Token funToken = tokenizer.consume();
        Function fun = functionTable.get(funToken.getContents());
        if (fun == null) {
            errors.add(ParseError.error(funToken, String.format("Unknown function: '%s'", funToken.getContents())));
        }
        call.setFunction(fun);
        tokenizer.consume(); // (
        while (!tokenizer.current().isSymbol(")") && tokenizer.current().isNotEnd()) {
            if (!call.getParameters().isEmpty()) {
                expect(Token.TokenType.SYMBOL, ",");
            }
            call.addParameter(expression());
        }
        expect(Token.TokenType.SYMBOL, ")");
        if (fun == null) {
            return Constant.EMPTY;
        }
        if (call.getParameters().size() != fun.getNumberOfArguments() && fun.getNumberOfArguments() >= 0) {
            errors.add(ParseError.error(funToken,
                    String.format(
                            "Number of arguments for function '%s' do not match. Expected: %d, Found: %d",
                            funToken.getContents(),
                            fun.getNumberOfArguments(),
                            call.getParameters().size())));
            return Constant.EMPTY;
        }
        return call;
    }

    /**
     * Signals that the given token is expected.
     * <p>
     * If the current input is pointing at the specified token, it will be consumed. If not, an error will be added
     * to the error list and the input remains unchanged.
     * </p>
     *
     * @param type    the type of the expected token
     * @param trigger the trigger of the expected token
     */
    protected void expect(Token.TokenType type, String trigger) {
        if (tokenizer.current().matches(type, trigger)) {
            tokenizer.consume();
        } else {
            errors.add(ParseError.error(tokenizer.current(),
                    String.format("Unexpected token '%s'. Expected: '%s'",
                            tokenizer.current().getSource(),
                            trigger)));
        }
    }

}
