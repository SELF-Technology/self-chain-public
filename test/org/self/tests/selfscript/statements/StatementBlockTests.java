package org.self.tests.selfscript.statements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.expressions.OperatorExpression;
import org.self.selfscript.expressions.VariableExpression;
import org.self.selfscript.statements.Statement;
import org.self.selfscript.statements.StatementBlock;
import org.self.selfscript.statements.commands.LETstatement;
import org.self.selfscript.statements.commands.RETURNstatement;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.NumberValue;
import org.self.objects.Transaction;
import org.self.objects.Witness;

public class StatementBlockTests {

    @Test
    public void testConstructors() {
        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();

            try {
                StatementBlock sb = new StatementBlock(Stats);
            } catch (Exception e) {
                fail();
            }
        }

        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();
            ConstantExpression cet = new ConstantExpression(new BooleanValue(true));
            Stats.add(new RETURNstatement(cet));

            try {
                StatementBlock sb = new StatementBlock(Stats);
            } catch (Exception e) {
                fail();
            }
        }

        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();
            ConstantExpression cet = new ConstantExpression(new BooleanValue(true));
            ConstantExpression cef = new ConstantExpression(new BooleanValue(true));
            Stats.add(new RETURNstatement(cet));
            Stats.add(new RETURNstatement(cef));
            Stats.add(new RETURNstatement(cet));
            Stats.add(new RETURNstatement(cef));

            try {
                StatementBlock sb = new StatementBlock(Stats);
            } catch (Exception e) {
                fail();
            }

        }
    }

    @Test
    public void testExecution() {
        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();
            StatementBlock sb = new StatementBlock(Stats);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                sb.run(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }

        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();
            ConstantExpression cet = new ConstantExpression(new BooleanValue(true));
            Stats.add(new RETURNstatement(cet));
            StatementBlock sb = new StatementBlock(Stats);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                sb.run(ctr);
            } catch (ExecutionException e) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(true, ctr.isSuccess());
        }

        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();
            ConstantExpression cef = new ConstantExpression(new BooleanValue(false));
            Stats.add(new RETURNstatement(cef));
            StatementBlock sb = new StatementBlock(Stats);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                sb.run(ctr);
            } catch (ExecutionException e) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }

        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();

            Stats.add(new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(0)),
                            new ConstantExpression(new NumberValue(0)),
                            OperatorExpression.OPERATOR_ADD)));
            Stats.add(new RETURNstatement(
                    new VariableExpression("a")));

            StatementBlock sb = new StatementBlock(Stats);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
                sb.run(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }

        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();

            Stats.add(new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(5)),
                            new ConstantExpression(new NumberValue(5)),
                            OperatorExpression.OPERATOR_ADD)));
            Stats.add(new RETURNstatement(
                    new VariableExpression("a")));

            StatementBlock sb = new StatementBlock(Stats);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
                sb.run(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }

        {
            ArrayList<Statement> Stats = new ArrayList<Statement>();

            Stats.add(new LETstatement("a",
                    new OperatorExpression(
                            new ConstantExpression(new NumberValue(5)),
                            new ConstantExpression(new NumberValue(5)),
                            OperatorExpression.OPERATOR_ADD)));
            Stats.add(new RETURNstatement(
                    new VariableExpression("a")));
            Stats.add(new RETURNstatement(
                    new VariableExpression("a")));

            StatementBlock sb = new StatementBlock(Stats);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
                sb.run(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
    }
}
