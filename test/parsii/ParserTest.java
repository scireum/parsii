/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import org.junit.Test;

import parsii.eval.Expression;
import parsii.eval.Function;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;

/**
 * Tests the {@link Parser} class.
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public class ParserTest {

    @Test
    public void limit_BigDecimal() throws ParseException {
        assertEquals(0.3d, Parser.parse("0.3+(10^6)-(10^6)").evaluate());
    }

    @Test
    public void simple() throws ParseException {
        assertEquals(-109d, Parser.parse("1 - (10 - -100)").evaluate());
        assertEquals(0.01d, Parser.parse("1 / 10 * 10 / 100").evaluate());
        assertEquals(-89d, Parser.parse("1 + 10 - 100").evaluate());
        assertEquals(91d, Parser.parse("1 - 10 - -100").evaluate());
        assertEquals(91d, Parser.parse("1 - 10  + 100").evaluate());
        assertEquals(-109d, Parser.parse("1 - (10 + 100)").evaluate());
        assertEquals(-89d, Parser.parse("1 + (10 - 100)").evaluate());
        assertEquals(100d, Parser.parse("1 / 1 * 100").evaluate());
        assertEquals(0.01d, Parser.parse("1 / (1 * 100)").evaluate());
        assertEquals(0.01d, Parser.parse("1 * 1 / 100").evaluate());
        assertEquals(7d, Parser.parse("3+4").evaluate());
        assertEquals(7d, Parser.parse("3      +    4").evaluate());
        assertEquals(-1d, Parser.parse("3+ -4").evaluate());
        assertEquals(-1d, Parser.parse("3+(-4)").evaluate());
    }

    @Test
    public void number() throws ParseException {
        assertEquals(4003.333333d, Parser.parse("3.333_333+4_000").evaluate());
    }

    private void assertEquals(double expected, BigDecimal actual) {
        org.junit.Assert.assertTrue("the value '" + actual + "' is not the expected value ('" + expected + "')", BigDecimal
                .valueOf(expected).compareTo(actual) == 0);
    }

    private void assertEquals(double expected, BigDecimal actual, double delta) {
        BigDecimal expectedBig = BigDecimal.valueOf(expected);
        BigDecimal deltaBig = BigDecimal.valueOf(delta);

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(9);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        org.junit.Assert.assertTrue("the value '" + df.format(actual) + "' is not the expected value ('" + expected + "')",
                expectedBig.subtract(actual).abs().compareTo(deltaBig) == -1);
    }

    @Test
    public void precedence() throws ParseException {
        // term vs. product
        assertEquals(19d, Parser.parse("3+4*4").evaluate());
        // product vs. power
        assertEquals(20.25d, Parser.parse("3^4/4").evaluate());
        // relation vs. product
        assertEquals(1d, Parser.parse("3 < 4*4").evaluate());
        assertEquals(0d, Parser.parse("3 > 4*4").evaluate());
        // brackets
        assertEquals(28d, Parser.parse("(3 + 4) * 4").evaluate());
    }

    @Test
    public void variables() throws ParseException {
        Scope scope = Scope.create();

        Variable a = scope.getVariable("a");
        Variable b = scope.getVariable("b");
        Expression expr = Parser.parse("3*a + 4 * b", scope);
        assertEquals(0d, expr.evaluate());
        a.setValue(BigDecimal.valueOf(2));
        assertEquals(6d, expr.evaluate());
        b.setValue(BigDecimal.valueOf(3));
        assertEquals(18d, expr.evaluate());
        assertEquals(18d, expr.evaluate());
    }

    @Test
    public void functions() throws ParseException {
        /* the most functions based on Math implementation. So we mused expect with inaccuracy */
        final double EPSILON = 0.0000000001;

        assertEquals(0d, Parser.parse("1 + sin(-pi) + cos(pi)").evaluate(), EPSILON);
        assertEquals(9.424777960769379715d, Parser.parse("pi * 3").evaluate(), EPSILON);
        assertEquals(8103.08392757538400d, Parser.parse("euler ^ 9  ").evaluate(), EPSILON);
        assertEquals(12391.6478079166974d, Parser.parse("euler ^ (pi * 3)").evaluate(), EPSILON);
        assertEquals(111.31777848985622d, Parser.parse("sqrt(euler ^ (pi * 3))").evaluate(), EPSILON);
        assertEquals(4.72038341576d, Parser.parse("tan(sqrt(euler ^ (pi * 3)))").evaluate(), EPSILON);
        assertEquals(3d, Parser.parse("| 3 - 6 |").evaluate(), EPSILON);
        assertEquals(3d, Parser.parse("if(3 > 2 && 2 < 3, 2+1, 1+1)").evaluate(), EPSILON);
        assertEquals(2d, Parser.parse("if(3 < 2 || 2 > 3, 2+1, 1+1)").evaluate(), EPSILON);
        assertEquals(2d, Parser.parse("min(3,2)").evaluate(), EPSILON);

        // Test a var arg method...
        Parser.registerFunction("avg", new Function() {
            @Override
            public int getNumberOfArguments() {
                return -1;
            }

            @Override
            public BigDecimal eval(List<Expression> args) {
                BigDecimal avg = BigDecimal.ZERO;
                if (args.isEmpty()) {
                    return avg;
                }
                for (Expression e : args) {
                    avg = avg.add(e.evaluate());
                }
                return avg.divide(BigDecimal.valueOf(args.size()));
            }

            @Override
            public boolean isNaturalFunction() {
                return true;
            }
        });
        assertEquals(3.25d, Parser.parse("avg(3,2,1,7)").evaluate());
    }

    @Test
    public void scopes() throws ParseException {
        Scope root = Scope.create();
        Variable a = root.getVariable("a").withValue(1);
        Scope subScope1 = Scope.createWithParent(root);
        Scope subScope2 = Scope.createWithParent(root);
        Variable b1 = subScope1.getVariable("b").withValue(2);
        Variable b2 = subScope2.getVariable("b").withValue(3);
        Variable c = root.getVariable("c").withValue(4);
        Variable c1 = subScope1.getVariable("c").withValue(5);
        org.junit.Assert.assertEquals(c, c1);
        Variable d = root.getVariable("d").withValue(9);
        Variable d1 = subScope1.create("d").withValue(7);
        assertNotEquals(d, d1);
        Expression expr1 = Parser.parse("a + b + c + d", subScope1);
        Expression expr2 = Parser.parse("a + b + c + d", subScope2);
        assertEquals(15d, expr1.evaluate());
        assertEquals(18d, expr2.evaluate());
        a.setValue(10);
        b1.setValue(20);
        b2.setValue(30);
        c.setValue(40);
        c1.setValue(50);
        assertEquals(87d, expr1.evaluate());
        assertEquals(99d, expr2.evaluate());
    }

    @Test
    public void errors() throws ParseException {
        // We expect the parser to continue after an recoverable error!
        try {
            Parser.parse("test(1 2)+sin(1,2)*34-34.45.45+");
        } catch (ParseException e) {
            org.junit.Assert.assertEquals(5, e.getErrors().size());
        }
    }

    @Test
    public void quantifiers() throws ParseException {
        assertEquals(1000d, Parser.parse("1K").evaluate());
        assertEquals(1000d, Parser.parse("1M * 1m").evaluate());
        assertEquals(1d, Parser.parse("1n * 1G").evaluate());
        assertEquals(1d, Parser.parse("(1M / 1k) * 1m").evaluate());
        assertEquals(1d, Parser.parse("1u * 10 k * 1000  m * 0.1 k").evaluate());
    }

    @Test
    public void getVariables() throws ParseException {
        Scope s = Scope.create();
        Parser.parse("a*b+c", s);
        assertTrue(s.getNames().contains("a"));
        assertTrue(s.getNames().contains("b"));
        assertTrue(s.getNames().contains("c"));
        assertFalse(s.getNames().contains("x"));

        // pi and euler are always defined...
        org.junit.Assert.assertEquals(5, s.getVariables().size());
    }

}
