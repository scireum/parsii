/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

/**
 * Represents a variable which binds a value to a name.
 * <p>
 * A variable is resolved or created using a {@link Scope}. This ensures that the same name always resolves to the
 * same variable. In contrast to using a Map, reading and writing a variable can be much faster, as it only needs
 * to be resolved once. Reading and writing it, is basically as cheap as a field access.
 * <p>
 * A variable can be made constant, which will fail all further attempts to change it.
 */
public class Variable {

    private double value = 0d;
    private String name;
    private boolean constant = false;

    /**
     * Creates a new variable.
     * <p>
     * Variables should only be created by their surrounding {@link Scope} so that all following look-ups
     * yield the same variable.
     *
     * @param name the name of the variable
     */
    protected Variable(String name) {
        this.name = name;
    }

    /**
     * Sets the value if the variable.
     *
     * @param value the new value of the variable
     * @throws IllegalStateException if the variable is constant
     */
    public void setValue(double value) {
        if (constant) {
            throw new IllegalStateException(String.format("%s is constant!", name));
        }
        this.value = value;
    }

    /**
     * Sets the given value and marks it as constant.
     *
     * @param value the new (and final) value of this variable
     */
    public void makeConstant(double value) {
        setValue(value);
        this.constant = true;
    }

    /**
     * Returns the value previously set.
     *
     * @return the value previously set or 0 if the variable is not written yet
     */
    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }

    /**
     * Returns the name of the variable.
     *
     * @return the name of this variable
     */
    public String getName() {
        return name;
    }

    /**
     * Determines if this variable is constant.
     *
     * @return <tt>true</tt> if this variable cannot be modified anymore, <tt>false</tt> otherwise
     */
    public boolean isConstant() {
        return constant;
    }

    /**
     * Sets the value and returns <tt>this</tt>.
     *
     * @param value the new value of this variable
     * @return <tt>this</tt> for fluent method calls
     */
    public Variable withValue(double value) {
        setValue(value);
        return this;
    }
}
