/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains a mapping of names to variables.
 * <p>
 * Both the user as well as the {@link Parser} use a Scope to resolve a name into a {@link Variable}. In
 * contrast to a simple Map, this approach provides two advantages: It's usually faster, as the variable
 * only needs to be resolved once. Modifying it and especially reading it when evaluating an expression is as
 * cheap as a simple field access. The second advantage is that scopes can be chained. So variables can be either
 * shared by two expression or kept separate, if required.
 */
public class Scope {
    private Scope parent;
    private boolean autocreateVariables = true;
    private Map<String, Variable> context = new ConcurrentHashMap<>();

    private static Scope root;

    /**
     * Creates a new empty scope.
     * <p>
     * The scope will not be completely empty, as {@link Math#PI} (pi) and {@link Math#E} (E) are always
     * defined as constants.
     * <p>
     * If an not yet known variable is accessed, it will be created and initialized with 0.
     */
    public Scope() {
        this(false);
    }

    private Scope(boolean skipParent) {
        if (!skipParent) {
            this.parent = getRootScope();
        }
    }

    /*
     * Creates the internal root scope which contains eternal constants ;-)
     */
    private static Scope getRootScope() {
        if (root == null) {
            synchronized (Scope.class) {
                root = new Scope(true);
                root.create("pi").makeConstant(Math.PI);
                root.create("euler").makeConstant(Math.E);
            }
        }

        return root;
    }

    /**
     * Determines if strict lookup should be used or not.
     * <p>
     * A scope with strict lookup will not create unknown variables upon their first access but rather throw an error.
     * <p>
     * By default, scopes are not strict and will automatically create variables when first reuqested.
     *
     * @param strictLookup <tt>true</tt> if the scope should be switched to strict lookup, <tt>false</tt>  otherwise
     * @return the instance itself for fluent method calls
     */
    public Scope withStrictLookup(boolean strictLookup) {
        this.autocreateVariables = !strictLookup;

        return this;
    }

    /**
     * Specifies the parent scope for this scope.
     * <p>
     * If a scope cannot resolve a variable, it tries to resolve it using its parent scope. This permits to
     * share a certain set of variables.
     *
     * @param parent the parent scope to use. If <tt>null</tt>, the common root scope is used which defines a bunch of
     *               constants (e and pi).
     * @return the instance itself for fluent method calls
     */
    public Scope withParent(Scope parent) {
        if (parent == null) {
            this.parent = getRootScope();
        } else {
            this.parent = parent;
        }

        return this;
    }

    /**
     * Searches for a {@link Variable} with the given name.
     * <p>
     * If the variable does not exist <tt>null</tt>  will be returned
     *
     * @param name the name of the variable to search
     * @return the variable with the given name or <tt>null</tt> if no such variable was found
     */
    public Variable find(String name) {
        if (context.containsKey(name)) {
            return context.get(name);
        }
        if (parent != null) {
            return parent.find(name);
        }
        return null;
    }

    /**
     * Searches for or creates a variable with the given name.
     * <p>
     * If no variable with the given name is found, a new variable is created in this scope
     *
     * @param name the variable to look for
     * @return a variable with the given name
     * @throws IllegalArgumentException if {@link #autocreateVariables} is <tt>false</tt> and the given
     *                                  variable was not creted yet.
     */
    public Variable getVariable(String name) {
        Variable result = find(name);
        if (result != null) {
            return result;
        }
        if (!autocreateVariables) {
            throw new IllegalArgumentException();
        }

        return create(name);
    }

    /**
     * Searches or creates a variable in this scope.
     * <p>
     * Tries to find a variable with the given name in this scope. If no variable with the given name is found,
     * the parent scope is not checked, but a new variable is created.
     *
     * @param name the variable to search or create
     * @return a variable with the given name from the local scope
     */
    public Variable create(String name) {
        if (context.containsKey(name)) {
            return context.get(name);
        }
        Variable result = new Variable(name);
        context.put(name, result);

        return result;
    }

    /**
     * Removes the variable with the given name from this scope.
     * <p>
     * If will not remove the variable from a parent scope.
     *
     * @param name the name of the variable to remove
     * @return the removed variable or <tt>null</tt> if no variable with the given name existed
     */
    public Variable remove(String name) {
        if (context.containsKey(name)) {
            return context.remove(name);
        } else {
            return null;
        }
    }

    /**
     * Returns all names of variables known to this scope (ignoring those of the parent scope).
     *
     * @return a set of all known variable names
     */
    public Set<String> getLocalNames() {
        return context.keySet();
    }

    /**
     * Returns all names of variables known to this scope or one of its parent scopes.
     *
     * @return a set of all known variable names
     */
    public Set<String> getNames() {
        if (parent == null) {
            return getLocalNames();
        }
        Set<String> result = new TreeSet<>();
        result.addAll(parent.getNames());
        result.addAll(getLocalNames());
        return result;
    }

    /**
     * Returns all variables known to this scope (ignoring those of the parent scope).
     *
     * @return a collection of all known variables
     */
    public Collection<Variable> getLocalVariables() {
        return context.values();
    }

    /**
     * Returns all variables known to this scope or one of its parent scopes.
     *
     * @return a collection of all known variables
     */
    public Collection<Variable> getVariables() {
        if (parent == null) {
            return getLocalVariables();
        }
        List<Variable> result = new ArrayList<>();
        result.addAll(parent.getVariables());
        result.addAll(getLocalVariables());
        return result;
    }
}
