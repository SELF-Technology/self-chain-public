package org.self.tests.selfscript.statements.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.BooleanExpression;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.expressions.Expression;
import org.self.selfscript.expressions.FunctionExpression;
import org.self.selfscript.expressions.GlobalExpression;
import org.self.selfscript.expressions.OperatorExpression;
import org.self.selfscript.expressions.VariableExpression;
import org.self.selfscript.functions.general.GET;
import org.self.selfscript.functions.number.DEC;
import org.self.selfscript.functions.sigs.CHECKSIG;
import org.self.selfscript.statements.commands.LETstatement;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.objects.Transaction;
import org.self.objects.Witness;

public class LETstatementTests {

    @Test
    public void testConstructors() {
        {
            LETstatement ls = new LETstatement("a",
                    new BooleanExpression(
                            new ConstantExpression(new BooleanValue(true)),
                            new ConstantExpression(new BooleanValue(true)),
                            BooleanExpression.BOOLEAN_AND));
            assertEquals("LET a = ( TRUE AND TRUE )", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new BooleanExpression(
                            new ConstantExpression(new BooleanValue(false)),
                            new ConstantExpression(new BooleanValue(false)),
                            BooleanExpression.BOOLEAN_EQ));
            assertEquals("LET a = ( FALSE EQ FALSE )", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new BooleanExpression(
                            new ConstantExpression(new BooleanValue(true)),
                            new ConstantExpression(new BooleanValue(false)),
                            BooleanExpression.BOOLEAN_NEQ));
            assertEquals("LET a = ( TRUE NEQ FALSE )", ls.toString());
        }

        {
            LETstatement ls = new LETstatement("a",
                    new ConstantExpression(new NumberValue(-1)));
            assertEquals("LET a = -1", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new ConstantExpression(new NumberValue(0)));
            assertEquals("LET a = 0", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new ConstantExpression(new NumberValue(1)));
            assertEquals("LET a = 1", ls.toString());
        }

        {
            LETstatement ls = new LETstatement("a",
                    new FunctionExpression(new GET()));
            assertEquals("LET a = function:GET, params:[]", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new FunctionExpression(new DEC()));
            assertEquals("LET a = function:DEC, params:[]", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new FunctionExpression(new CHECKSIG()));
            assertEquals("LET a = function:CHECKSIG, params:[]", ls.toString());
        }

        {
            LETstatement ls = new LETstatement("a",
                    new GlobalExpression("@SCRIPT"));
            assertEquals("LET a = global:@SCRIPT", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new GlobalExpression("@BLKNUM"));
            assertEquals("LET a = global:@BLKNUM", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new GlobalExpression("@ADDRESS"));
            assertEquals("LET a = global:@ADDRESS", ls.toString());
        }

        {
            LETstatement ls = new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(5)),
                            new ConstantExpression(new NumberValue(5)),
                            OperatorExpression.OPERATOR_ADD));
            assertEquals("LET a = ( 5 + 5 )", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(3)),
                            new ConstantExpression(new NumberValue(7)),
                            OperatorExpression.OPERATOR_MUL));
            assertEquals("LET a = ( 3 * 7 )", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(1)),
                            new ConstantExpression(new NumberValue(9)),
                            OperatorExpression.OPERATOR_SUB));
            assertEquals("LET a = ( 1 - 9 )", ls.toString());
        }

        {
            LETstatement ls = new LETstatement("a",
                    new VariableExpression("b"));
            assertEquals("LET a = variable:b", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new VariableExpression("c"));
            assertEquals("LET a = variable:c", ls.toString());
        }
        {
            LETstatement ls = new LETstatement("a",
                    new VariableExpression("d"));
            assertEquals("LET a = variable:d", ls.toString());
        }

        {
            ArrayList<Expression> arl = new ArrayList<Expression>();
            arl.add(new ConstantExpression(new BooleanValue(true)));
            arl.add(new ConstantExpression(new NumberValue(1)));
            arl.add(new ConstantExpression(new HexValue("0x12345678")));
            arl.add(new ConstantExpression(new StringValue("RETURN TRUE")));
            LETstatement ls = new LETstatement(arl,
                    new ConstantExpression(new NumberValue(42)));
            assertEquals("LET ( TRUE 1 0x12345678 RETURN TRUE ) = 42", ls.toString());
        }
        {
            ArrayList<Expression> arl = new ArrayList<Expression>();
            arl.add(new BooleanExpression(
                    new ConstantExpression(new BooleanValue(true)),
                    new ConstantExpression(new BooleanValue(true)),
                    BooleanExpression.BOOLEAN_AND));
            arl.add(new FunctionExpression(new CHECKSIG()));
            arl.add(new GlobalExpression("@ADDRESS"));
            arl.add(new OperatorExpression(
                    new ConstantExpression(new NumberValue(5)),
                    new ConstantExpression(new NumberValue(5)),
                    OperatorExpression.OPERATOR_ADD));
            arl.add(new VariableExpression("b"));
            LETstatement ls = new LETstatement(arl,
                    new ConstantExpression(new NumberValue(42)));
            assertEquals("LET ( ( TRUE AND TRUE ) function:CHECKSIG, params:[] global:@ADDRESS ( 5 + 5 ) variable:b ) = 42", ls.toString());
        }
    }

    @Test
    public void testExecution() {
        {
            LETstatement ls = new LETstatement("a",
                    new BooleanExpression(
                            new ConstantExpression(new BooleanValue(true)),
                            new ConstantExpression(new BooleanValue(true)),
                            BooleanExpression.BOOLEAN_AND));

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("TRUE", ((BooleanValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            LETstatement ls = new LETstatement("a",
                    new BooleanExpression(
                            new ConstantExpression(new BooleanValue(false)),
                            new ConstantExpression(new BooleanValue(false)),
                            BooleanExpression.BOOLEAN_EQ));

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("TRUE", ((BooleanValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            LETstatement ls = new LETstatement("a",
                    new BooleanExpression(
                            new ConstantExpression(new BooleanValue(true)),
                            new ConstantExpression(new BooleanValue(false)),
                            BooleanExpression.BOOLEAN_NEQ));

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("TRUE", ((BooleanValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }

        {
            LETstatement ls = new LETstatement("a",
                    new ConstantExpression(new NumberValue(-1)));
            assertEquals("LET a = -1", ls.toString());

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("-1", ((NumberValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            LETstatement ls = new LETstatement("a",
                    new ConstantExpression(new NumberValue(0)));

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("0", ((NumberValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            LETstatement ls = new LETstatement("a",
                    new ConstantExpression(new NumberValue(1)));

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("1", ((NumberValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }

        //{
        //    LETstatement ls = new LETstatement("a",
        //            new FunctionExpression(new GET()));
        //    assertEquals("LET a = function:GET, params:[]", ls.toString());
        //}
        //{
        //    LETstatement ls = new LETstatement("a",
        //            new FunctionExpression(new DEC()));
        //    assertEquals("LET a = function:DEC, params:[]", ls.toString());
        //}
        //{
        //    LETstatement ls = new LETstatement("a",
        //            new FunctionExpression(new CHECKSIG()));
        //    assertEquals("LET a = function:CHECKSIG, params:[]", ls.toString());
        //}
        //
        //{
        //    LETstatement ls = new LETstatement("a",
        //            new GlobalExpression("@SCRIPT"));
        //    assertEquals("LET a = global:@SCRIPT", ls.toString());
        //}
        //{
        //    LETstatement ls = new LETstatement("a",
        //            new GlobalExpression("@BLKNUM"));
        //    assertEquals("LET a = global:@BLKNUM", ls.toString());
        //}
        //{
        //    LETstatement ls = new LETstatement("a",
        //            new GlobalExpression("@ADDRESS"));
        //    assertEquals("LET a = global:@ADDRESS", ls.toString());
        //}
        {
            LETstatement ls = new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(5)),
                            new ConstantExpression(new NumberValue(5)),
                            OperatorExpression.OPERATOR_ADD));

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("10", ((NumberValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            LETstatement ls = new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(3)),
                            new ConstantExpression(new NumberValue(7)),
                            OperatorExpression.OPERATOR_MUL));

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("21", ((NumberValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            LETstatement ls = new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(1)),
                            new ConstantExpression(new NumberValue(9)),
                            OperatorExpression.OPERATOR_SUB));

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ls.execute(ctr);
                assertEquals("-8", ((NumberValue) ctr.getVariable("a")).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }

        //{
        //    LETstatement ls = new LETstatement("a",
        //            new VariableExpression("b"));
        //    assertEquals("LET a = variable:b", ls.toString());
        //}
        //{
        //    LETstatement ls = new LETstatement("a",
        //            new VariableExpression("c"));
        //    assertEquals("LET a = variable:c", ls.toString());
        //}
        //{
        //    LETstatement ls = new LETstatement("a",
        //            new VariableExpression("d"));
        //    assertEquals("LET a = variable:d", ls.toString());
        //}
//        {
//            ArrayList<Expression> arl = new ArrayList<Expression>();
//            arl.add(new ConstantExpression(new BooleanValue(true)));
//            arl.add(new ConstantExpression(new NumberValue(1)));
//            arl.add(new ConstantExpression(new HexValue("0x12345678")));
//            arl.add(new ConstantExpression(new StringValue("RETURN TRUE")));
//            LETstatement ls = new LETstatement(arl,
//                    new ConstantExpression(new NumberValue(42)));
//
//            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
//            try {
//                ls.execute(ctr);
//                assertEquals("42", ((NumberValue) ctr.getVariable("TRUE,1,0x12345678,RETURN TRUE,")).toString());
//            } catch (ExecutionException ex) {
//                fail();
//            }
//        }
//        {
//            ArrayList<Expression> arl = new ArrayList<Expression>();
//            arl.add(new BooleanExpression(
//                    new ConstantExpression(new BooleanValue(true)),
//                    new ConstantExpression(new BooleanValue(true)),
//                    BooleanExpression.BOOLEAN_AND));
//            //arl.add(new FunctionExpression(new CHECKSIG()));
//            //arl.add(new GlobalExpression("@ADDRESS"));
//            arl.add(new OperatorExpression(
//                    new ConstantExpression(new NumberValue(5)),
//                    new ConstantExpression(new NumberValue(5)),
//                    OperatorExpression.OPERATOR_ADD));
//            //arl.add(new VariableExpression("b"));
//            LETstatement ls = new LETstatement(arl,
//                    new ConstantExpression(new NumberValue(42)));
//            //assertEquals("LET ( ( TRUE AND TRUE ) function:CHECKSIG, params:[] global:@ADDRESS ( 5 + 5 ) variable:b ) = 42", ls.toString());
//
//            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
//            try {
//                ls.execute(ctr);
//                assertEquals("42", ((NumberValue) ctr.getVariable("TRUE,10,")).toString());
//            } catch (ExecutionException ex) {
//                fail();
//            }
//        }
    }

}
