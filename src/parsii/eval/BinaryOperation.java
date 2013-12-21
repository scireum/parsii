package parsii.eval;

import java.util.List;

/**
 * Represents a binary operation.
 * <p>
 * A binary operation has two sub-expressions. A set of supported operations is also defined. If both arguments are
 * constant, simplifying this expression will again lead to a constant expression.
 * </p>
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public class BinaryOperation extends Expression {

    /**
     * Enumerates the operations supported by this expression.
     */
    public static enum Op {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, POWER, LT, LT_EQ, EQ, GT_EQ, GT, NEQ, AND, OR;
    }


    private final Op op;
    private Expression left;
    private Expression right;

    /**
     * When comparing two double values, those are considered equal, if their difference is lower than the defined
     * epsilon. This is way better than relying on an exact comparison due to rounding errors
     */
    public static final double EPSILON = 0.0000001;

    public BinaryOperation(Op op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public double evaluate() {
        double a = left.evaluate();
        double b = right.evaluate();
        if (op == Op.ADD) {
            return a + b;
        } else if (op == Op.SUBTRACT) {
            return a - b;
        } else if (op == Op.MULTIPLY) {
            return a * b;
        } else if (op == Op.DIVIDE) {
            return a / b;
        } else if (op == Op.POWER) {
            return Math.pow(a, b);
        } else if (op == Op.MODULO) {
            return a % b;
        } else if (op == Op.LT) {
            return a < b ? 1 : 0;
        } else if (op == Op.LT_EQ) {
            return a < b || Math.abs(a - b) < EPSILON ? 1 : 0;
        } else if (op == Op.GT) {
            return a > b ? 1 : 0;
        } else if (op == Op.GT_EQ) {
            return a > b || Math.abs(a - b) > EPSILON ? 1 : 0;
        } else if (op == Op.EQ) {
            return Math.abs(a - b) < EPSILON ? 1 : 0;
        } else if (op == Op.NEQ) {
            return Math.abs(a - b) > EPSILON ? 1 : 0;
        } else if (op == Op.AND) {
            return a == 1 && b == 1 ? 1 : 0;
        } else if (op == Op.OR) {
            return a == 1 || b == 1 ? 1 : 0;
        }

        throw new UnsupportedOperationException(String.valueOf(op));
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
                BinaryOperation childOp = (BinaryOperation) right;
                if (op == childOp.op) {
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
                    } else if (childOp.left.isConstant()) {
                        // Since our left side is non constant, but the left side of the child expression is,
                        // we push the constant up, to support further optimizations
                        return new BinaryOperation(op, childOp.left, new BinaryOperation(op, left, childOp.right));
                    }
                }
            }
        }

//        // + and * and associative and commutative. Therefore we can change the order applied as we want.
//        // We therefore collect all operands as long as the operation is the same, pre-process all constant values
//        // and generate binary operations for the remaining expressions.
//        // Therefore we simplify
//        if (op == Op.ADD || op == Op.MULTIPLY) {
//            List<Expression> values = new ArrayList<Expression>();
//            List<Double> constants = new ArrayList<Double>();
//            visitBinaryOps(op, values, constants, this);
//            if (constants.isEmpty()) {
//                return this;
//            }
//            double constant = Op.ADD == op ? 0d : 1d;
//            for (Double val : constants) {
//                if (op == Op.ADD) {
//                    constant += val;
//                } else {
//                    constant *= val;
//                }
//            }
//            Expression expr = new Constant(constant);
//            for (Expression val : values) {
//                expr = new BinaryOperation(op, expr, val);
//            }
//            return expr;
//        }

        return super.simplify();
    }

    private void visitBinaryOps(Op op,
                                List<Expression> values,
                                List<Double> constants,
                                BinaryOperation binaryOperation) {
        if (binaryOperation.left instanceof BinaryOperation && ((BinaryOperation) binaryOperation.left).op == op) {
            visitBinaryOps(op, values, constants, (BinaryOperation) binaryOperation.left);
        } else if (binaryOperation.left.isConstant()) {
            constants.add(binaryOperation.left.evaluate());
        } else {
            values.add(binaryOperation.left);
        }
        if (binaryOperation.right instanceof BinaryOperation && ((BinaryOperation) binaryOperation.right).op == op) {
            visitBinaryOps(op, values, constants, (BinaryOperation) binaryOperation.right);
        } else if (binaryOperation.right.isConstant()) {
            constants.add(binaryOperation.right.evaluate());
        } else {
            values.add(binaryOperation.right);
        }
    }

}
