/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

import java.math.BigDecimal;

/**
 * Represents the result of a parsed expression.
 * <p>
 * Can be evaluated to return a BigDecimal value. If an error occurs <code>null</code> will be returned.
 * </p>
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public abstract class Expression {

    /**
     * Evaluates the expression to a BigDecimal number.
     *
     * @return the BigDecimal value as a result of evaluating this expression. Returns <code>null</code> if an error occurs
     */
    public abstract BigDecimal evaluate();

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
     *         variables
     */
    public boolean isConstant() {
        return false;
    }
}
