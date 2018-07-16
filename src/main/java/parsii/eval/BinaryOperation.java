/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii.eval;

/**
 * Represents a binary operation.
 * <p>
 * A binary operation has two sub-expressions. A set of supported operations is also defined. If both arguments are
 * constant, simplifying this expression will again lead to a constant expression.
 */
public class BinaryOperation implements Expression {

    /**
     * Enumerates the operations supported by this expression.
     */
    public enum Op {
        ADD(3), SUBTRACT(3), MULTIPLY(4), DIVIDE(4), MODULO(4), POWER(5), LT(2), LT_EQ(2), EQ(2), GT_EQ(2), GT(2), NEQ(2), AND(
                1), OR(1);

        private final int priority;

        Op(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    private final Op op;
    private Expression left;
    private Expression right;
    private boolean sealed = false;

    /**
     * When comparing two double values, those are considered equal, if their difference is lower than the defined
     * epsilon. This is way better than relying on an exact comparison due to rounding errors
     */
    public static final double EPSILON = 0.0000000001;

    /**
     * Creates a new binary operator for the given operator and operands.
     *
     * @param op    the operator of the operation
     * @param left  the left operand
     * @param right the right operand
     */
    public BinaryOperation(Op op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the operation performed by this binary operation.
     *
     * @return the operation performed
     */
    public Op getOp() {
        return op;
    }

    /**
     * Returns the left operand
     *
     * @return the left operand of this operation
     */
    public Expression getLeft() {
        return left;
    }

    /**
     * Replaces the left operand of the operation with the given expression.
     *
     * @param left the new expression to be used as left operand
     */
    public void setLeft(Expression left) {
        this.left = left;
    }

    /**
     * Returns the right operand
     *
     * @return the right operand of this operation
     */
    public Expression getRight() {
        return right;
    }

    /**
     * Marks an operation as sealed, meaning that re-ordering or operations on the same level must not be re-ordered.
     * <p>
     * Binary operations are sealed if they're e.g. surrounded by braces.
     */
    public void seal() {
        sealed = true;
    }

    /**
     * Determines if the operation is sealed and operands must not be re-ordered.
     *
     * @return <tt>true</tt> if the operation is protected by braces and operands might not be exchanged with
     * operations nearby.
     */
    public boolean isSealed() {
        return sealed;
    }

    @Override
    @SuppressWarnings({"squid:S3776", "squid:MethodCyclomaticComplexity"})
    public double evaluate() {
        double a = left.evaluate();
        double b = right.evaluate();

        switch (op) {
            case ADD:
                return a + b;
            case SUBTRACT:
                return a - b;
            case MULTIPLY:
                return a * b;
            case DIVIDE:
                return a / b;
            case POWER:
                return Math.pow(a, b);
            case MODULO:
                return a % b;
            case LT:
                return a < b ? 1 : 0;
            case LT_EQ:
                return a < b || Math.abs(a - b) < EPSILON ? 1 : 0;
            case GT:
                return a > b ? 1 : 0;
            case GT_EQ:
                return a > b || Math.abs(a - b) < EPSILON ? 1 : 0;
            case EQ:
                return Math.abs(a - b) < EPSILON ? 1 : 0;
            case NEQ:
                return Math.abs(a - b) > EPSILON ? 1 : 0;
            case AND:
                return Math.abs(a) > 0 && Math.abs(b) > 0 ? 1 : 0;
            case OR:
                return Math.abs(a) > 0 || Math.abs(b) > 0 ? 1 : 0;
            default:
                throw new UnsupportedOperationException(String.valueOf(op));
        }
    }

    @Override
    public Expression simplify() {
        left = left.simplify();
        right = right.simplify();
        // First of all we check of both sides are constant. If true, we can directly evaluate the result...
        if (left.isConstant() && right.isConstant()) {
            return new Constant(evaluate());
        }
        // + and * are commutative and associative, therefore we can reorder operands as we desire
        if (op == Op.ADD || op == Op.MULTIPLY) {
            // We prefer the have the constant part at the left side, re-order if it is the other way round.
            // This simplifies further optimizations as we can concentrate on the left side
            if (right.isConstant()) {
                Expression tmp = right;
                right = left;
                left = tmp;
            }

            if (right instanceof BinaryOperation) {
                Expression childOp = trySimplifyRightSide();
                if (childOp != null) {
                    return childOp;
                }
            }
        }

        return Expression.super.simplify();
    }

    private Expression trySimplifyRightSide() {
        BinaryOperation childOp = (BinaryOperation) right;
        if (op != childOp.op) {
            return null;
        }

        // We have a sub-operation with the same operator, let's see if we can pre-compute some constants
        if (left.isConstant()) {
            // Left side is constant, we therefore can combine constants. We can rely on the constant
            // being on the left side, since we reorder commutative operations (see above)
            if (childOp.left.isConstant()) {
                if (op == Op.ADD) {
                    return new BinaryOperation(op,
                                               new Constant(left.evaluate() + childOp.left.evaluate()),
                                               childOp.right);
                }
                if (op == Op.MULTIPLY) {
                    return new BinaryOperation(op,
                                               new Constant(left.evaluate() * childOp.left.evaluate()),
                                               childOp.right);
                }
            }
        }

        if (childOp.left.isConstant()) {
            // Since our left side is non constant, but the left side of the child expression is,
            // we push the constant up, to support further optimizations
            return new BinaryOperation(op, childOp.left, new BinaryOperation(op, left, childOp.right));
        }

        return null;
    }

    @Override
    public String toString() {
        return "(" + left.toString() + " " + op + " " + right + ")";
    }
}
