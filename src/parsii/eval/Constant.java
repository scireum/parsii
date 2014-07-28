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
 * Represents a constant numeric expression.
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public class Constant extends Expression {
    private BigDecimal value;

    /**
     * Used as dummy expression by the parser if an error occurs while parsing.
     */
    public static final Constant EMPTY = new Constant(null);

    public Constant(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal evaluate() {
        return value;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
