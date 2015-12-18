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
 * Represents a binary function.
 * <p>
 * A binary function has two arguments which are always evaluated in order to compute the final result.
 */
public abstract class BinaryFunction implements Function {

    @Override
    public int getNumberOfArguments() {
        return 2;
    }

    @Override
    public double eval(List<Expression> args) {
        double a = args.get(0).evaluate();
        if (Double.isNaN(a)) {
            return a;
        }
        double b = args.get(1).evaluate();
        if (Double.isNaN(b)) {
            return b;
        }
        return eval(a, b);
    }

    /**
     * Performs the computation of the binary function
     *
     * @param a the first argument of the function
     * @param b the second argument of the function
     * @return the result of calling the function with a and b
     */
    protected abstract double eval(double a, double b);

    @Override
    public boolean isNaturalFunction() {
        return true;
    }
}
