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
 * Contains a set of predefined standard functions.
 * <p>
 * Provides mostly functions defined by {@link Math}
 */
public class Functions {

    /**
     * Provides access to {@link Math#sin(double)}
     */
    public static final Function SIN = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.sin(a);
        }
    };

    /**
     * Provides access to {@link Math#sinh(double)}
     */
    public static final Function SINH = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.sinh(a);
        }
    };

    /**
     * Provides access to {@link Math#cos(double)}
     */
    public static final Function COS = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.cos(a);
        }
    };

    /**
     * Provides access to {@link Math#cosh(double)}
     */
    public static final Function COSH = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.cosh(a);
        }
    };

    /**
     * Provides access to {@link Math#tan(double)}
     */
    public static final Function TAN = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.tan(a);
        }
    };

    /**
     * Provides access to {@link Math#tanh(double)}
     */
    public static final Function TANH = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.tanh(a);
        }
    };

    /**
     * Provides access to {@link Math#abs(double)}
     */
    public static final Function ABS = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.abs(a);
        }
    };

    /**
     * Provides access to {@link Math#asin(double)}
     */
    public static final Function ASIN = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.asin(a);
        }
    };

    /**
     * Provides access to {@link Math#acos(double)}
     */
    public static final Function ACOS = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.acos(a);
        }
    };

    /**
     * Provides access to {@link Math#atan(double)}
     */
    public static final Function ATAN = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.atan(a);
        }
    };

    /**
     * Provides access to {@link Math#atan2(double, double)}
     */
    public static final Function ATAN2 = new BinaryFunction() {
        @Override
        protected double eval(double a, double b) {
            return Math.atan2(a, b);
        }
    };

    /**
     * Provides access to {@link Math#round(double)}
     */
    public static final Function ROUND = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.round(a);
        }
    };

    /**
     * Provides access to {@link Math#floor(double)}
     */
    public static final Function FLOOR = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.floor(a);
        }
    };

    /**
     * Provides access to {@link Math#ceil(double)}
     */
    public static final Function CEIL = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.ceil(a);
        }
    };

    /**
     * Provides access to {@link Math#pow(double, double)}
     */
    public static final Function POW = new BinaryFunction() {
        @Override
        protected double eval(double a, double b) {
            return Math.pow(a, b);
        }
    };

    /**
     * Provides access to {@link Math#sqrt(double)}
     */
    public static final Function SQRT = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.sqrt(a);
        }
    };

    /**
     * Provides access to {@link Math#exp(double)}
     */
    public static final Function EXP = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.exp(a);
        }
    };

    /**
     * Provides access to {@link Math#log(double)}
     */
    public static final Function LN = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.log(a);
        }
    };

    /**
     * Provides access to {@link Math#log10(double)}
     */
    public static final Function LOG = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.log10(a);
        }
    };

    /**
     * Provides access to {@link Math#min(double, double)}
     */
    public static final Function MIN = new BinaryFunction() {
        @Override
        protected double eval(double a, double b) {
            return Math.min(a, b);
        }
    };

    /**
     * Provides access to {@link Math#max(double, double)}
     */
    public static final Function MAX = new BinaryFunction() {
        @Override
        protected double eval(double a, double b) {
            return Math.max(a, b);
        }
    };

    /**
     * Provides access to {@link Math#random()} which will be multiplied by the given argument.
     */
    public static final Function RND = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.random() * a;
        }
    };

    /**
     * Provides access to {@link Math#signum(double)}
     */
    public static final Function SIGN = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.signum(a);
        }
    };

    /**
     * Provides access to {@link Math#toDegrees(double)}
     */
    public static final Function DEG = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.toDegrees(a);
        }
    };

    /**
     * Provides access to {@link Math#toRadians(double)}
     */
    public static final Function RAD = new UnaryFunction() {
        @Override
        protected double eval(double a) {
            return Math.toRadians(a);
        }
    };

    /**
     * Provides an if-like function
     * <p>
     * It expects three arguments: A condition, an expression being evaluated if the condition is non zero and an
     * expression which is being evaluated if the condition is zero.
     */
    public static final Function IF = new IfFunction();

    private Functions() {
    }

    private static class IfFunction implements Function {
        @Override
        public int getNumberOfArguments() {
            return 3;
        }

        @Override
        public double eval(List<Expression> args) {
            double check = args.get(0).evaluate();
            if (Double.isNaN(check)) {
                return check;
            }
            if (Math.abs(check) > 0) {
                return args.get(1).evaluate();
            } else {
                return args.get(2).evaluate();
            }
        }

        @Override
        public boolean isNaturalFunction() {
            return false;
        }
    }
}
