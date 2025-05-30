package org.self.tests.selfscript.statements.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.BooleanExpression;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.expressions.OperatorExpression;
import org.self.selfscript.expressions.VariableExpression;
import org.self.selfscript.statements.Statement;
import org.self.selfscript.statements.StatementBlock;
import org.self.selfscript.statements.commands.LETstatement;
import org.self.selfscript.statements.commands.RETURNstatement;
import org.self.selfscript.statements.commands.WHILEstatement;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.NumberValue;
import org.self.objects.Transaction;
import org.self.objects.Witness;

public class WHILEstatementTests {

    @Test
    public void testConstructors() {
        BooleanExpression WhileCheck = new BooleanExpression(
                new VariableExpression("a"),
                new ConstantExpression(new NumberValue(5)),
                BooleanExpression.BOOLEAN_LT);

        OperatorExpression oe1 = new OperatorExpression(
                new VariableExpression("a"),
                new ConstantExpression(new NumberValue(1)),
                OperatorExpression.OPERATOR_ADD);

        LETstatement ls2 = new LETstatement("a", oe1);

        ArrayList<Statement> WhileCodeBlockAL = new ArrayList<Statement>();
        WhileCodeBlockAL.add(ls2);

        StatementBlock WhileCodeBlock = new StatementBlock(WhileCodeBlockAL);

        WHILEstatement ws = new WHILEstatement(WhileCheck, WhileCodeBlock);

        assertEquals("WHILE ( variable:a LT 5 )", ws.toString());

    }

    @Test
    public void testExecution() {
        {
            ArrayList<Statement> CodeBlockAL = new ArrayList<Statement>();

            LETstatement ls1 = new LETstatement("a", new ConstantExpression(new NumberValue(0)));

            CodeBlockAL.add(ls1);

            BooleanExpression WhileCheck = new BooleanExpression(
                    new VariableExpression("a"),
                    new ConstantExpression(new NumberValue(5)),
                    BooleanExpression.BOOLEAN_LT);

            OperatorExpression oe1 = new OperatorExpression(
                    new VariableExpression("a"),
                    new ConstantExpression(new NumberValue(1)),
                    OperatorExpression.OPERATOR_ADD);

            LETstatement ls2 = new LETstatement("a", oe1);

            ArrayList<Statement> WhileCodeBlockAL = new ArrayList<Statement>();
            WhileCodeBlockAL.add(ls2);

            StatementBlock WhileCodeBlock = new StatementBlock(WhileCodeBlockAL);

            WHILEstatement ws = new WHILEstatement(WhileCheck, WhileCodeBlock);

            CodeBlockAL.add(ws);

            StatementBlock CodeBlock = new StatementBlock(CodeBlockAL);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                CodeBlock.run(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            ArrayList<Statement> CodeBlockAL = new ArrayList<Statement>();

            LETstatement ls1 = new LETstatement("a", new ConstantExpression(new NumberValue(0)));

            CodeBlockAL.add(ls1);

            BooleanExpression WhileCheck = new BooleanExpression(
                    new VariableExpression("a"),
                    new ConstantExpression(new NumberValue(5)),
                    BooleanExpression.BOOLEAN_LT);

            OperatorExpression oe1 = new OperatorExpression(
                    new VariableExpression("a"),
                    new ConstantExpression(new NumberValue(1)),
                    OperatorExpression.OPERATOR_ADD);

            LETstatement ls2 = new LETstatement("a", oe1);

            ArrayList<Statement> WhileCodeBlockAL = new ArrayList<Statement>();
            WhileCodeBlockAL.add(ls2);

            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new BooleanValue(true)));
            WhileCodeBlockAL.add(rs);

            StatementBlock WhileCodeBlock = new StatementBlock(WhileCodeBlockAL);

            WHILEstatement ws = new WHILEstatement(WhileCheck, WhileCodeBlock);

            CodeBlockAL.add(ws);

            StatementBlock CodeBlock = new StatementBlock(CodeBlockAL);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                CodeBlock.run(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
        }
    }

}
