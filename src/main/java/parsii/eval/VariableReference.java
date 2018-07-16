/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

/**
 * Represents a reference to a variable.
 */
public class VariableReference implements Expression {

    private Variable var;

    /**
     * Creates a new reference to the given variable.
     *
     * @param var the variable to access when this expression is evaluated
     */
    public VariableReference(Variable var) {
        this.var = var;
    }

    @Override
    public double evaluate() {
        return var.getValue();
    }

    @Override
    public String toString() {
        return var.getName();
    }

    @Override
    public boolean isConstant() {
        return var.isConstant();
    }

    @Override
    public Expression simplify() {
        if (isConstant()) {
            return new Constant(evaluate());
        }
        return this;
    }
}
