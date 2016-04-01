/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

import java.io.Serializable;

/**
 * Represents the result of a parsed expression.
 * <p>
 * Can be evaluated to return a double value. If an error occurs {@code Double.NaN} will be returned.
 */
public abstract class Expression implements Serializable {

    private static final long serialVersionUID = -6078443078086081582L;

    /**
     * Evaluates the expression to a double number.
     *
     * @return the double value as a result of evaluating this expression. Returns NaN if an error occurs
     */
    public abstract double evaluate();

    /**
     * Returns a simplified version of this expression.
     *
     * @return a simplified version of this expression or <tt>this</tt> if the expression cannot be simplified
     */
    public Expression simplify() {
        return this;
    }

    /**
     * Determines the this expression is constant
     *
     * @return <tt>true</tt> if the result of evaluate will never change and does not depend on external state like
     * variables
     */
    public boolean isConstant() {
        return false;
    }
}
