/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

import java.math.BigDecimal;
import java.util.List;

/**
 * Defines a function which can be referenced and evaluated from within expressions.
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public interface Function {

    /**
     * Returns the number of expected arguments.
     * <p>If the function is called with a different number of arguments, an error will be created</p>
     * <p>In order to support functions with a variable number of arguments, a negative number can be returned.
     * This will essentially disable the check.</p>
     *
     * @return the number of arguments expected by this function or a negative number to indicate that this
     * function accepts a variable number of arguments
     */
    int getNumberOfArguments();

    /**
     * Executes the function with the given arguments.
     * <p>
     * The arguments need to be evaluated first. This is not done externally to permit functions to perform lazy
     * evaluations.
     * </p>
     *
     * @param args the arguments for this function. The length of the given list will exactly match
     *             <tt>getNumberOfArguments</tt>
     * @return the result of the function evaluated with the given arguments
     */
    BigDecimal eval(List<Expression> args);

    /**
     * A natural function returns the same output for the same input.
     * <p>
     * All classical mathematical functions are "natural". A function which reads user input is not natural, as
     * the function might return different results depending on the users input
     * </p>
     *
     * @return <tt>true</tt> if the function returns the same output for the same input, <tt>false</tt> otherwise
     */
    boolean isNaturalFunction();
}
