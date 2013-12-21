package parsii.eval;

/**
 * Represents a reference to a variable.
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public class VariableReference extends Expression {

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
