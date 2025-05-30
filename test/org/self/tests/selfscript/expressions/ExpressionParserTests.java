package org.self.tests.selfscript.expressions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.expressions.BooleanExpression;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.expressions.ExpressionParser;
import org.self.selfscript.expressions.OperatorExpression;
import org.self.selfscript.tokens.ScriptToken;

public class ExpressionParserTests {

    @Test
    public void testBooleanExpressions() {

        String[] Ops = {"AND", "NAND", "OR", "NOR", "XOR", "NXOR", "EQ", "NEQ", "LT", "LTE", "GT", "GTE"};
        for (String Op : Ops) {
            {
                try {
                    String Script = "TRUE " + Op + " TRUE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( TRUE " + Op + " TRUE )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "TRUE " + Op + " FALSE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( TRUE " + Op + " FALSE )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "FALSE " + Op + " TRUE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( FALSE " + Op + " TRUE )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "FALSE " + Op + " FALSE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( FALSE " + Op + " FALSE )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0x1 " + Op + " 0x1";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 0x01 " + Op + " 0x01 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0x1 " + Op + " 0x0";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 0x01 " + Op + " 0x00 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0x0 " + Op + " 0x1";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 0x00 " + Op + " 0x01 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0x0 " + Op + " 0x0";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 0x00 " + Op + " 0x00 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "1 " + Op + " 1";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 1 " + Op + " 1 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "1 " + Op + " 0";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 1 " + Op + " 0 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0 " + Op + " 1";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 0 " + Op + " 1 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0 " + Op + " 0";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 0 " + Op + " 0 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "1.5 " + Op + " 1.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 1.5 " + Op + " 1.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "1.5 " + Op + " 0.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 1.5 " + Op + " 0.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0.5 " + Op + " 1.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 0.5 " + Op + " 1.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0.5 " + Op + " 0.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( 0.5 " + Op + " 0.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "-1.5 " + Op + " -1.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( -1.5 " + Op + " -1.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "-1.5 " + Op + " -0.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( -1.5 " + Op + " -0.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "-0.5 " + Op + " -1.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( -0.5 " + Op + " -1.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "-0.5 " + Op + " -0.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( -0.5 " + Op + " -0.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "a " + Op + " b";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( variable:a " + Op + " variable:b )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "[Hello World] " + Op + " [Hello World]";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( Hello World " + Op + " Hello World )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "LEN(0x12345678) " + Op + " LEN(0x12345678)";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( function:LEN, params:[0x12345678] " + Op + " function:LEN, params:[0x12345678] )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                assertThrows(SelfParseException.class, () -> {
                    String Script = "LEN 0x12345678 " + Op + " LEN 0x12345678";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                });
            }

            {
                try {
                    String Script = "@BLKNUM " + Op + " @BLKTIME";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( global:@BLKNUM " + Op + " global:@BLKTIME )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "((a) " + Op + " (((b))))";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( variable:a " + Op + " variable:b )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "((a) " + Op + " (((b))))";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), BooleanExpression.class);
                    assertEquals("( variable:a " + Op + " variable:b )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }
            {
                assertThrows(SelfParseException.class, () -> {
                    String Script = "(A " + Op + " B B";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                });
            }

            /*{
                assertThrows(SelfParseException.class, () -> {
                    String Script = "(A " + Op + " B) [";
                    List<Token> tokens = Token.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                });
            }*/
        }
    }

    @Test
    public void testBooleanExpressionsUnary() {

        {
            try {
                String Script = "NOT TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( TRUE )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT FALSE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( FALSE )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT 0x1";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( 0x01 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT 0x0";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( 0x00 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT 1";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( 1 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT 0";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( 0 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT 1.5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( 1.5 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT 0.5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( 0.5 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT -1.5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( -1.5 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT -0.5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( -0.5 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT a";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( variable:a )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT [Hello World]";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( Hello World )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NOT LEN(0x12345678)";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( function:LEN, params:[0x12345678] )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "NOT LEN 0x12345678";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
            });
        }

        {
            try {
                String Script = "NOT @BLKNUM";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), BooleanExpression.class);
                assertEquals("NOT ( global:@BLKNUM )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "NOT ((A)";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
            });
        }
        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "NOT (A";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
            });
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "NOT (A";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
            });
        }
    }

    @Test
    public void testOperatorExpressions() {

        String[] Ops = {"+", "-", "*", "/", "<<", ">>", "%", "&", "|", "^"};
        for (String Op : Ops) {
            {
                try {
                    String Script = "TRUE " + Op + " TRUE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( TRUE " + Op + " TRUE )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "TRUE " + Op + " FALSE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( TRUE " + Op + " FALSE )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "FALSE " + Op + " TRUE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( FALSE " + Op + " TRUE )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "FALSE " + Op + " FALSE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( FALSE " + Op + " FALSE )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0x1 " + Op + " 0x1";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 0x01 " + Op + " 0x01 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0x1 " + Op + " 0x0";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 0x01 " + Op + " 0x00 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0x0 " + Op + " 0x1";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 0x00 " + Op + " 0x01 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0x0 " + Op + " 0x0";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 0x00 " + Op + " 0x00 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "1 " + Op + " 1";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 1 " + Op + " 1 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "1 " + Op + " 0";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 1 " + Op + " 0 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0 " + Op + " 1";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 0 " + Op + " 1 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0 " + Op + " 0";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 0 " + Op + " 0 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "1.5 " + Op + " 1.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 1.5 " + Op + " 1.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "1.5 " + Op + " 0.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 1.5 " + Op + " 0.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0.5 " + Op + " 1.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 0.5 " + Op + " 1.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "0.5 " + Op + " 0.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( 0.5 " + Op + " 0.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "-1.5 " + Op + " -1.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( -1.5 " + Op + " -1.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "-1.5 " + Op + " -0.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( -1.5 " + Op + " -0.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "-0.5 " + Op + " -1.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( -0.5 " + Op + " -1.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "-0.5 " + Op + " -0.5";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( -0.5 " + Op + " -0.5 )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "a " + Op + " b";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( variable:a " + Op + " variable:b )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "[Hello World] " + Op + " [Hello World]";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( Hello World " + Op + " Hello World )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "LEN(0x12345678) " + Op + " LEN(0x12345678)";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( function:LEN, params:[0x12345678] " + Op + " function:LEN, params:[0x12345678] )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                assertThrows(SelfParseException.class, () -> {
                    String Script = "LEN 0x12345678 " + Op + " LEN 0x12345678";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                });
            }

            {
                try {
                    String Script = "@BLKNUM " + Op + " @BLKTIME";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( global:@BLKNUM " + Op + " global:@BLKTIME )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "((a) " + Op + " (((b))))";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( variable:a " + Op + " variable:b )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }

            {
                try {
                    String Script = "((a) " + Op + " (((b))))";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                    assertEquals(expr.getClass(), OperatorExpression.class);
                    assertEquals("( variable:a " + Op + " variable:b )", expr.toString());
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }
            {
                assertThrows(SelfParseException.class, () -> {
                    String Script = "(A " + Op + " B B";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                });
            }

            /*{
                assertThrows(SelfParseException.class, () -> {
                    String Script = "(A " + Op + " B) [";
                    List<Token> tokens = Token.tokenize(Contract.cleanScript(Script));
                    Expression expr = ExpressionParser.getExpression(tokens,0);
                });
            }*/
        }
    }

    @Test
    public void testOperatorExpressionsUnary() {

        {
            try {
                String Script = "NEG TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( TRUE )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG FALSE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( FALSE )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG 0x1";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( 0x01 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG 0x0";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( 0x00 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG 1";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( 1 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG 0";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( 0 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG 1.5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( 1.5 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG 0.5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( 0.5 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG -1.5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( -1.5 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG -0.5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( -0.5 )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG a";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( variable:a )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG [Hello World]";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( Hello World )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "NEG LEN(0x12345678)";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( function:LEN, params:[0x12345678] )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "NEG LEN 0x12345678";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
            });
        }

        {
            try {
                String Script = "NEG @BLKNUM";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
                assertEquals(expr.getClass(), OperatorExpression.class);
                assertEquals(" - ( global:@BLKNUM )", expr.toString());
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "NEG ((A)";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
            });
        }
        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "NEG (A";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
            });
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "NEG (A";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                Expression expr = ExpressionParser.getExpression(tokens,0);
            });
        }
    }

    @Test
    public void testOperatorExpressionsExceptions() {
        assertThrows(SelfParseException.class, () -> {
            String Script = "A + B";
            List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
            tokens.add(new ScriptToken(-1, "@#$"));
            Expression expr = ExpressionParser.getExpression(tokens,0);
        });

        assertThrows(SelfParseException.class, () -> {
            ArrayList<ScriptToken> tokens = new ArrayList<>();
            tokens.add(new ScriptToken(ScriptToken.TOKEN_OPENBRACKET, "("));
            tokens.add(new ScriptToken(-1, "@#$"));
            tokens.add(new ScriptToken(ScriptToken.TOKEN_CLOSEBRACKET, ")"));
            Expression expr = ExpressionParser.getExpression(tokens,0);
        });
    }
}
