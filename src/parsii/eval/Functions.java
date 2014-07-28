/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contains a set of predefined standard functions.
 * <p>
 * Provides mostly functions defined by {@link Math}
 * <p>
 * <b>NOTE:</b> the {@link Math} based on double Values and should be have accuracy limitations.
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public class Functions {

    /**
     * Provides access to {@link Math#sin(BigDecimal)}
     */
    public static final Function SIN = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.sin(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#sinh(BigDecimal)}
     */
    public static final Function SINH = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.sinh(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#cos(BigDecimal)}
     */
    public static final Function COS = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.cos(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#cosh(BigDecimal)}
     */
    public static final Function COSH = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.cosh(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#tan(BigDecimal)}
     */
    public static final Function TAN = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.tan(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#tanh(BigDecimal)}
     */
    public static final Function TANH = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.tanh(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link BigDecimal#abs()}
     */
    public static final Function ABS = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return a.abs();
        }
    };

    /**
     * Provides access to {@link Math#asin(BigDecimal)}
     */
    public static final Function ASIN = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.asin(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#acos(BigDecimal)}
     */
    public static final Function ACOS = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.acos(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#atan(double))}
     */
    public static final Function ATAN = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.atan(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#atan2(BigDecimal, BigDecimal)}
     */
    public static final Function ATAN2 = new BinaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a, BigDecimal b) {
            return valueOf(Math.atan2(a.doubleValue(), b.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#round(BigDecimal)}
     */
    public static final Function ROUND = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.round(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#floor(BigDecimal)}
     */
    public static final Function FLOOR = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.floor(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#ceil(BigDecimal)}
     */
    public static final Function CEIL = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.ceil(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#sqrt(BigDecimal)}
     */
    public static final Function SQRT = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.sqrt(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#exp(BigDecimal)}
     */
    public static final Function EXP = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.exp(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#log(BigDecimal)}
     */
    public static final Function LN = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.log(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#log10(BigDecimal)}
     */
    public static final Function LOG = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.log10(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link BigDecimal#min(BigDecimal)}
     */
    public static final Function MIN = new BinaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a, BigDecimal b) {
            return a.min(b);
        }
    };

    /**
     * Provides access to {@link BigDecimal#max(BigDecimal)}
     */
    public static final Function MAX = new BinaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a, BigDecimal b) {
            return a.max(b);
        }
    };

    /**
     * Provides access to {@link Math#random()} which will be multiplied by the given argument.
     */
    public static final Function RND = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return a.multiply(valueOf(Math.random()));
        }
    };

    /**
     * Provides access to {@link Math#signum(double)}
     */
    public static final Function SIGN = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.signum(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#toDegrees(BigDecimal)}
     */
    public static final Function DEG = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.toDegrees(a.doubleValue()));
        }
    };

    /**
     * Provides access to {@link Math#toRadians(BigDecimal)}
     */
    public static final Function RAD = new UnaryFunction() {
        @Override
        protected BigDecimal eval(BigDecimal a) {
            return valueOf(Math.toRadians(a.doubleValue()));
        }
    };

    /**
     * Provides an if-like function
     * <p>
     * It expects three arguments: A condition, an expression being evaluated if the condition is 1 and an expression
     * which is being evaluated if the condition is not 1.
     * </p>
     */
    public static final Function IF = new Function() {
        @Override
        public int getNumberOfArguments() {
            return 3;
        }

        @Override
        public BigDecimal eval(List<Expression> args) {
            BigDecimal check = args.get(0).evaluate();
            if (check == null) {
                return check;
            }
            if (check == ONE) {
                return args.get(1).evaluate();
            } else {
                return args.get(2).evaluate();
            }
        }

        @Override
        public boolean isNaturalFunction() {
            return false;
        }
    };
}
