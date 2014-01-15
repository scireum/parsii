/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains a mapping of names to variables.
 * <p>
 * Both the user as well as the {@link Parser} use a Scope to resolve a name into a {@link Variable}. In
 * contrast to a simple Map, this approach provides two advantages: It's usually faster, as the variable
 * only needs to be resolved once. Modifying it and especially reading it when evaluating an expression is as
 * cheap as a simple field access. The second advantage is that scopes can be chained. So variables can be either
 * shared by two expression or kept separate, if required.
 * </p>
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public class Scope {
    private Scope parent;
    private Map<String, Variable> context = new ConcurrentHashMap<String, Variable>();

    private static Scope root;

    /*
     * Use one of the static factories
     */
    private Scope() {

    }

    /**
     * Creates a new empty scope.
     * <p>
     * The scope will not be completely empty, as {@link Math#PI} (pi) and {@link Math#E} (E) are always
     * defined as constants.
     * </p>
     *
     * @return a new scope.
     */
    public static Scope create() {
        Scope result = new Scope();
        result.parent = getRootScope();

        return result;
    }

    /*
     * Creates the internal root scope which contains eternal constants ;-)
     */
    private static Scope getRootScope() {
        if (root == null) {
            root = new Scope();
            root.getVariable("pi").makeConstant(Math.PI);
            root.getVariable("E").makeConstant(Math.E);
        }

        return root;
    }

    /**
     * Creates a new scope with the given parent.
     * <p>
     * If a variable is resolved, first the own scope is scanned. If no variable for the given name is found, the
     * parent scope is scanned. If this yields a variable, it will be returned. Otherwise it will be defined
     * in the local scope (not in the parent).
     * </p>
     *
     * @param parent the scope to use as lookup source for further variables
     * @return a new scope with the given parent as super scope
     */
    public static Scope createWithParent(Scope parent) {
        Scope result = create();
        result.parent = parent;

        return result;
    }

    /**
     * Searches for a {@link Variable} with the given name.
     * <p>
     * If the variable does not exist <tt>null</tt>  will be returned
     * </p>
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
     * <p>If no variable with the given name is found, a new variable is created in this scope</p>
     *
     * @param name the variable to look for
     * @return a variable with the given name
     */
    public Variable getVariable(String name) {
        Variable result = find(name);
        if (result != null) {
            return result;
        }
        return create(name);
    }

    /**
     * Searches or creates a variable in this scope.
     * <p>
     * Tries to find a variable with the given name in this scope. If no variable with the given name is found,
     * the parent scope is not checked, but a new variable is created.
     * </p>
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
}
