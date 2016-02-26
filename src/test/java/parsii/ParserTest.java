/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package parsii;

import org.junit.Test;
import parsii.eval.BinaryOperation;
import parsii.eval.Expression;
import parsii.eval.Function;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link Parser} class.
 *
 * @author Andreas Haufler (aha@scireum.de)
 * @since 2013/09
 */
public class ParserTest {
    @Test
    public void simple() throws ParseException {
        assertEquals(-109d, Parser.parse("1 - (10 - -100)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(0.01d, Parser.parse("1 / 10 * 10 / 100").evaluate(), BinaryOperation.EPSILON);
        assertEquals(-89d, Parser.parse("1 + 10 - 100").evaluate(), BinaryOperation.EPSILON);
        assertEquals(91d, Parser.parse("1 - 10 - -100").evaluate(), BinaryOperation.EPSILON);
        assertEquals(91d, Parser.parse("1 - 10  + 100").evaluate(), BinaryOperation.EPSILON);
        assertEquals(-109d, Parser.parse("1 - (10 + 100)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(-89d, Parser.parse("1 + (10 - 100)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(100d, Parser.parse("1 / 1 * 100").evaluate(), BinaryOperation.EPSILON);
        assertEquals(0.01d, Parser.parse("1 / (1 * 100)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(0.01d, Parser.parse("1 * 1 / 100").evaluate(), BinaryOperation.EPSILON);
        assertEquals(7d, Parser.parse("3+4").evaluate(), BinaryOperation.EPSILON);
        assertEquals(7d, Parser.parse("3      +    4").evaluate(), BinaryOperation.EPSILON);
        assertEquals(-1d, Parser.parse("3+ -4").evaluate(), BinaryOperation.EPSILON);
        assertEquals(-1d, Parser.parse("3+(-4)").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void number() throws ParseException {
        assertEquals(4003.333333d, Parser.parse("3.333_333+4_000").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void precedence() throws ParseException {
        // term vs. product
        assertEquals(19d, Parser.parse("3+4*4").evaluate(), BinaryOperation.EPSILON);
        // product vs. power
        assertEquals(20.25d, Parser.parse("3^4/4").evaluate(), BinaryOperation.EPSILON);
        // relation vs. product
        assertEquals(1d, Parser.parse("3 < 4*4").evaluate(), BinaryOperation.EPSILON);
        assertEquals(0d, Parser.parse("3 > 4*4").evaluate(), BinaryOperation.EPSILON);
        // brackets
        assertEquals(28d, Parser.parse("(3 + 4) * 4").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void signed() throws ParseException {
        assertEquals(-2.02, Parser.parse("-2.02").evaluate(), BinaryOperation.EPSILON);
        assertEquals(2.02, Parser.parse("+2.02").evaluate(), BinaryOperation.EPSILON);
        assertEquals(1.01, Parser.parse("+2.02 + -1.01").evaluate(), BinaryOperation.EPSILON);
        assertEquals(-4.03, Parser.parse("-2.02 - +2.01").evaluate(), BinaryOperation.EPSILON);
        assertEquals(3.03, Parser.parse("+2.02 + +1.01").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void blockComment() throws ParseException {
        assertEquals(29, Parser.parse("27+ /*xxx*/ 2").evaluate(), BinaryOperation.EPSILON);
        assertEquals(29, Parser.parse("27+/*xxx*/ 2").evaluate(), BinaryOperation.EPSILON);
        assertEquals(29, Parser.parse("27/*xxx*/+2").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void startingWithDecimalPoint() throws ParseException {
        assertEquals(.2, Parser.parse(".2").evaluate(), BinaryOperation.EPSILON);
        assertEquals(.2, Parser.parse("+.2").evaluate(), BinaryOperation.EPSILON);
        assertEquals(.4, Parser.parse(".2+.2").evaluate(), BinaryOperation.EPSILON);
        assertEquals(.4, Parser.parse(".6+-.2").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void signedParentheses() throws ParseException {
        assertEquals(0.2, Parser.parse("-(-0.2)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(1.2, Parser.parse("1-(-0.2)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(0.8, Parser.parse("1+(-0.2)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(2.2, Parser.parse("+(2.2)").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void trailingDecimalPoint() throws ParseException {
        assertEquals(2., Parser.parse("2.").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void signedValueAfterOperand() throws ParseException {
        assertEquals(-1.2, Parser.parse("1+-2.2").evaluate(), BinaryOperation.EPSILON);
        assertEquals(3.2, Parser.parse("1++2.2").evaluate(), BinaryOperation.EPSILON);
        assertEquals(6 * -1.1, Parser.parse("6*-1.1").evaluate(), BinaryOperation.EPSILON);
        assertEquals(6 * 1.1, Parser.parse("6*+1.1").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void variables() throws ParseException {
        Scope scope = new Scope();

        Variable a = scope.create("a");
        Variable b = scope.create("b");
        Expression expr = Parser.parse("3*a + 4 * b", scope);
        assertEquals(0d, expr.evaluate(), BinaryOperation.EPSILON);
        a.setValue(2);
        assertEquals(6d, expr.evaluate(), BinaryOperation.EPSILON);
        b.setValue(3);
        assertEquals(18d, expr.evaluate(), BinaryOperation.EPSILON);
        assertEquals(18d, expr.evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void functions() throws ParseException {
        assertEquals(0d, Parser.parse("1 + sin(-pi) + cos(pi)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(4.72038341576d, Parser.parse("tan(sqrt(euler ^ (pi * 3)))").evaluate(), BinaryOperation.EPSILON);
        assertEquals(3d, Parser.parse("| 3 - 6 |").evaluate(), BinaryOperation.EPSILON);
        assertEquals(3d, Parser.parse("if(3 > 2 && 2 < 3, 2+1, 1+1)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(2d, Parser.parse("if(3 < 2 || 2 > 3, 2+1, 1+1)").evaluate(), BinaryOperation.EPSILON);
        assertEquals(2d, Parser.parse("min(3,2)").evaluate(), BinaryOperation.EPSILON);

        // Test a var arg method...
        Parser.registerFunction("avg", new Function() {
            @Override
            public int getNumberOfArguments() {
                return -1;
            }

            @Override
            public double eval(List<Expression> args) {
                double avg = 0;
                if (args.isEmpty()) {
                    return avg;
                }
                for (Expression e : args) {
                    avg += e.evaluate();
                }
                return avg / args.size();
            }

            @Override
            public boolean isNaturalFunction() {
                return true;
            }
        });
        assertEquals(3.25d, Parser.parse("avg(3,2,1,7)").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void scopes() throws ParseException {
        Scope root = new Scope();
        Variable a = root.getVariable("a").withValue(1);
        Scope subScope1 = new Scope().withParent(root);
        Scope subScope2 = new Scope().withParent(root);
        Variable b1 = subScope1.getVariable("b").withValue(2);
        Variable b2 = subScope2.getVariable("b").withValue(3);
        Variable c = root.getVariable("c").withValue(4);
        Variable c1 = subScope1.getVariable("c").withValue(5);
        assertEquals(c, c1);
        Variable d = root.getVariable("d").withValue(9);
        Variable d1 = subScope1.create("d").withValue(7);
        assertNotEquals(d, d1);
        Expression expr1 = Parser.parse("a + b + c + d", subScope1);
        Expression expr2 = Parser.parse("a + b + c + d", subScope2);
        assertEquals(15d, expr1.evaluate(), BinaryOperation.EPSILON);
        assertEquals(18d, expr2.evaluate(), BinaryOperation.EPSILON);
        a.setValue(10);
        b1.setValue(20);
        b2.setValue(30);
        c.setValue(40);
        c1.setValue(50);
        assertEquals(87d, expr1.evaluate(), BinaryOperation.EPSILON);
        assertEquals(99d, expr2.evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void errors() throws ParseException {
        // We expect the parser to continue after an recoverable error!
        try {
            Parser.parse("test(1 2)+sin(1,2)*34-34.45.45+");
            assertTrue(false);
        } catch (ParseException e) {
            assertEquals(5, e.getErrors().size());
        }

        // We expect the parser to report an invalid quantifier.
        try {
            Parser.parse("1x");
            assertTrue(false);
        } catch (ParseException e) {
            assertEquals(1, e.getErrors().size());
        }

        // We expect the parser to report an unfinished expression
        try {
            Parser.parse("1(");
            assertTrue(false);
        } catch (ParseException e) {
            assertEquals(1, e.getErrors().size());
        }
    }

    @Test
    public void relationalOperators() throws ParseException {
        // Test for Issue with >= and <= operators (#4)
        assertEquals(1d, Parser.parse("5 <= 5").evaluate(), BinaryOperation.EPSILON);
        assertEquals(1d, Parser.parse("5 >= 5").evaluate(), BinaryOperation.EPSILON);
        assertEquals(0d, Parser.parse("5 < 5").evaluate(), BinaryOperation.EPSILON);
        assertEquals(0d, Parser.parse("5 > 5").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void quantifiers() throws ParseException {
        assertEquals(1000d, Parser.parse("1K").evaluate(), BinaryOperation.EPSILON);
        assertEquals(1000d, Parser.parse("1M * 1m").evaluate(), BinaryOperation.EPSILON);
        assertEquals(1d, Parser.parse("1n * 1G").evaluate(), BinaryOperation.EPSILON);
        assertEquals(1d, Parser.parse("(1M / 1k) * 1m").evaluate(), BinaryOperation.EPSILON);
        assertEquals(1d, Parser.parse("1u * 10 k * 1000  m * 0.1 k").evaluate(), BinaryOperation.EPSILON);
    }

    @Test
    public void getVariables() throws ParseException {
        Scope s = new Scope();
        Parser.parse("a*b+c", s);
        assertTrue(s.getNames().contains("a"));
        assertTrue(s.getNames().contains("b"));
        assertTrue(s.getNames().contains("c"));
        assertFalse(s.getNames().contains("x"));

        // pi and euler are always defined...
        assertEquals(5, s.getVariables().size());
    }

    @Test
    public void errorOnUnknownVariable() throws ParseException {
        Scope s = new Scope();
        try {
            s.create("a");
            s.create("b");
            Parser.parse("a*b+c", s);
        } catch (ParseException e) {
            assertEquals(1, e.getErrors().size());
        }

        s.create("c");
        Parser.parse("a*b+c", s);
    }

    @Test
    public void removeVariable() throws ParseException {
        Scope s = new Scope();
        s.create("X");
        assertTrue(s.find("X") != null);
        assertTrue(s.remove("X") != null);
        assertTrue(s.find("X") == null);
    }

    @Test
    public void removeVariableFromSubscope() throws ParseException {
        Scope s = new Scope();
        Scope child = new Scope().withParent(s);
        s.create("X");
        assertTrue(child.find("X") != null);
        assertTrue(child.remove("X") == null);
        assertTrue(child.find("X") != null);
    }
}
