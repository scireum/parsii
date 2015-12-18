/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

import java.util.List;

/**
 * Represents an unary function.
 * <p>
 * An unary function has one arguments which is always evaluated in order to compute the final result.
 */
public abstract class UnaryFunction implements Function {

    @Override
    public int getNumberOfArguments() {
        return 1;
    }

    @Override
    public double eval(List<Expression> args) {
        double a = args.get(0).evaluate();
        if (Double.isNaN(a)) {
            return a;
        }
        return eval(a);
    }

    /**
     * Performs the computation of the unary function
     *
     * @param a the argument of the function
     * @return the result of calling the function with a as argument
     */
    protected abstract double eval(double a);

    @Override
    public boolean isNaturalFunction() {
        return true;
    }
}
