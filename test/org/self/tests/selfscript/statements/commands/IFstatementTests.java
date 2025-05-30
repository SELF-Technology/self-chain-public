package org.self.tests.selfscript.statements.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.statements.Statement;
import org.self.selfscript.statements.StatementBlock;
import org.self.selfscript.statements.commands.IFstatement;
import org.self.selfscript.statements.commands.RETURNstatement;
import org.self.selfscript.values.BooleanValue;
import org.self.objects.Transaction;
import org.self.objects.Witness;
import org.self.utils.SelfLogger;

public class IFstatementTests {

    @Test
    public void testConstructors() {
        ConstantExpression CheckTrue = new ConstantExpression(new BooleanValue(false));
        ConstantExpression CheckFalse = new ConstantExpression(new BooleanValue(false));

        RETURNstatement ReturnTrue = new RETURNstatement(CheckTrue);
        RETURNstatement ReturnFalse = new RETURNstatement(CheckFalse);

        ArrayList<Statement> CodeBlockReturnTrueAL = new ArrayList<Statement>();
        CodeBlockReturnTrueAL.add(ReturnTrue);
        StatementBlock CodeBlockReturnTrue = new StatementBlock(CodeBlockReturnTrueAL);

        ArrayList<Statement> CodeBlockReturnFalseAL = new ArrayList<Statement>();
        CodeBlockReturnFalseAL.add(ReturnFalse);
        StatementBlock CodeBlockReturnFalse = new StatementBlock(CodeBlockReturnTrueAL);

        IFstatement ifs = new IFstatement();
        ifs.addCondition(CheckFalse, CodeBlockReturnFalse);
        ifs.addCondition(CheckFalse, CodeBlockReturnFalse);
        ifs.addCondition(CheckTrue, CodeBlockReturnTrue);

        assertEquals("IF FALSE, ELSEIF FALSE, ELSEIF FALSE", ifs.toString());

    }

    @Test
    public void testExecution() {
        ConstantExpression CheckTrue = new ConstantExpression(new BooleanValue(true));
        ConstantExpression CheckFalse = new ConstantExpression(new BooleanValue(false));

        RETURNstatement ReturnTrue = new RETURNstatement(CheckTrue);
        RETURNstatement ReturnFalse = new RETURNstatement(CheckFalse);

        ArrayList<Statement> CodeBlockReturnTrueAL = new ArrayList<Statement>();
        CodeBlockReturnTrueAL.add(ReturnTrue);
        StatementBlock CodeBlockReturnTrue = new StatementBlock(CodeBlockReturnTrueAL);

        ArrayList<Statement> CodeBlockReturnFalseAL = new ArrayList<Statement>();
        CodeBlockReturnFalseAL.add(ReturnFalse);
        StatementBlock CodeBlockReturnFalse = new StatementBlock(CodeBlockReturnFalseAL);

        {
            IFstatement ifs = new IFstatement();

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            //assertThrows(ExecutionException.class, () -> { // should throw this
            //    ifs.execute(ctr);
            //});

            // but does not throw
            try {
                ifs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
            assertEquals(0, ctr.getNumberOfInstructions());
        }
        {
            IFstatement ifs = new IFstatement();
            ifs.addCondition(CheckTrue, CodeBlockReturnTrue);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ifs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(true, ctr.isSuccess());
            assertEquals(3, ctr.getNumberOfInstructions());
        }
        {
            IFstatement ifs = new IFstatement();
            ifs.addCondition(CheckTrue, CodeBlockReturnTrue);
            ifs.addCondition(CheckFalse, CodeBlockReturnFalse);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ifs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(true, ctr.isSuccess());
            assertEquals(3, ctr.getNumberOfInstructions());
        }
        {
            IFstatement ifs = new IFstatement();
            ifs.addCondition(CheckFalse, CodeBlockReturnFalse);
            ifs.addCondition(CheckTrue, CodeBlockReturnTrue);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ifs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(true, ctr.isSuccess());
            assertEquals(4, ctr.getNumberOfInstructions());
        }
        {
            IFstatement ifs = new IFstatement();
            ifs.addCondition(CheckTrue, CodeBlockReturnTrue);
            ifs.addCondition(CheckFalse, CodeBlockReturnFalse);
            ifs.addCondition(CheckFalse, CodeBlockReturnFalse);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ifs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(true, ctr.isSuccess());
            assertEquals(3, ctr.getNumberOfInstructions());
        }
        {
            IFstatement ifs = new IFstatement();
            ifs.addCondition(CheckFalse, CodeBlockReturnFalse);
            ifs.addCondition(CheckTrue, CodeBlockReturnTrue);
            ifs.addCondition(CheckFalse, CodeBlockReturnFalse);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ifs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(true, ctr.isSuccess());
            assertEquals(4, ctr.getNumberOfInstructions());
        }
        {
            IFstatement ifs = new IFstatement();
            ifs.addCondition(CheckFalse, CodeBlockReturnFalse);
            ifs.addCondition(CheckFalse, CodeBlockReturnFalse);
            ifs.addCondition(CheckTrue, CodeBlockReturnTrue);

            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                ifs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(true, ctr.isSuccess());
            assertEquals(5, ctr.getNumberOfInstructions());
        }
    }
}
