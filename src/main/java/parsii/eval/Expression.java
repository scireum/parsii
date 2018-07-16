/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

/**
 * Represents the result of a parsed expression.
 * <p>
 * Can be evaluated to return a double value. If an error occurs {@code Double.NaN} will be returned.
 */
public interface Expression {

    /**
     * Evaluates the expression to a double number.
     *
     * @return the double value as a result of evaluating this expression. Returns NaN if an error occurs
     */
    double evaluate();

    /**
     * Returns a simplified version of this expression.
     *
     * @return a simplified version of this expression or <tt>this</tt> if the expression cannot be simplified
     */
    default Expression simplify() {
        return this;
    }

    /**
     * Determines the this expression is constant
     *
     * @return <tt>true</tt> if the result of evaluate will never change and does not depend on external state like
     * variables
     */
    default boolean isConstant() {
        return false;
    }
}
