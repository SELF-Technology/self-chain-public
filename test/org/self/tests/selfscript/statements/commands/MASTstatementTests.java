package org.self.tests.selfscript.statements.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.statements.commands.MASTstatement;
import org.self.selfscript.values.HexValue;
import org.self.objects.ScriptProof;
import org.self.objects.Transaction;
import org.self.objects.Witness;
import org.self.utils.SelfLogger;

public class MASTstatementTests {

    @Test
    public void testConstructors() {
        ConstantExpression Empty = new ConstantExpression(new HexValue(""));
        ConstantExpression Random = new ConstantExpression(new HexValue("0xBC05B9280323A07AE6B2C6FA183F20991D38D01BC91F02C439CB5A33113E106C"));

        MASTstatement ms1 = new MASTstatement(Empty);
        assertEquals("MAST ", ms1.toString());

        MASTstatement ms2 = new MASTstatement(Random);
        assertEquals("MAST 0xBC05B9280323A07AE6B2C6FA183F20991D38D01BC91F02C439CB5A33113E106C", ms2.toString());
    }

    @Test
    public void testExecution() {
        {
            try {
                ScriptProof sp = new ScriptProof("");
                MASTstatement ms = new MASTstatement(new ConstantExpression(new HexValue(sp.getAddress().getAddressData())));

                Witness w = new Witness();
                w.addScript(sp);
                Contract ctr = new Contract("", "", w, new Transaction(), new ArrayList<>());
                try {
                    ms.execute(ctr);
                } catch (ExecutionException ex) {
                    fail();
                }
                assertEquals(false, ctr.isSuccessSet());
                assertEquals(false, ctr.isSuccess());
                assertEquals(1, ctr.getNumberOfInstructions());
            } catch (Exception e) {
                fail();
            }
        }
        {
            try {
                ScriptProof sp = new ScriptProof("RETURN TRUE");
                MASTstatement ms = new MASTstatement(new ConstantExpression(new HexValue(sp.getAddress().getAddressData())));

                Witness w = new Witness();
                w.addScript(sp);
                Contract ctr = new Contract("", "", w, new Transaction(), new ArrayList<>());
                try {
                    ms.execute(ctr);
                } catch (ExecutionException ex) {
                    fail();
                }
                assertEquals(true, ctr.isSuccessSet());
                assertEquals(true, ctr.isSuccess());
                assertEquals(3, ctr.getNumberOfInstructions());
            } catch (Exception e) {
                fail();
            }
        }
        {
            try {
                ScriptProof sp = new ScriptProof("RETURN FALSE");
                MASTstatement ms = new MASTstatement(new ConstantExpression(new HexValue(sp.getAddress().getAddressData())));

                Witness w = new Witness();
                w.addScript(sp);
                Contract ctr = new Contract("", "", w, new Transaction(), new ArrayList<>());
                try {
                    ms.execute(ctr);
                } catch (ExecutionException ex) {
                    fail();
                }
                assertEquals(true, ctr.isSuccessSet());
                assertEquals(false, ctr.isSuccess());
                assertEquals(3, ctr.getNumberOfInstructions());
            } catch (Exception e) {
                fail();
            }
        }
        {
            try {
                ScriptProof sp = new ScriptProof("Hello World");
                MASTstatement ms = new MASTstatement(new ConstantExpression(new HexValue(sp.getAddress().getAddressData())));

                Witness w = new Witness();
                w.addScript(sp);
                Contract ctr = new Contract("", "", w, new Transaction(), new ArrayList<>());
                assertThrows(ExecutionException.class, () -> {
                    ms.execute(ctr);
                });
                assertEquals(false, ctr.isSuccessSet());
                assertEquals(false, ctr.isSuccess());
                assertEquals(1, ctr.getNumberOfInstructions());
            } catch (Exception e) {
                fail();
            }
        }
        {
            try {
                ScriptProof sp = new ScriptProof("RETURN TRUE");
                MASTstatement ms = new MASTstatement(new ConstantExpression(new HexValue(sp.getAddress().getAddressData())));

                Witness w = new Witness();
                Contract ctr = new Contract("", "", w, new Transaction(), new ArrayList<>());
                assertThrows(ExecutionException.class, () -> {
                    ms.execute(ctr);
                });
                assertEquals(false, ctr.isSuccessSet());
                assertEquals(false, ctr.isSuccess());
                assertEquals(1, ctr.getNumberOfInstructions());
            } catch (Exception e) {
                fail();
            }
        }
    }
}
