package org.self.tests.selfscript.statements.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.statements.commands.ASSERTstatement;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.objects.Transaction;
import org.self.objects.Witness;

public class ASSERTstatementTests {

    @Test
    public void testConstructors() {
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new BooleanValue(true)));
            assertEquals("ASSERT TRUE", as.toString());
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new BooleanValue(false)));
            assertEquals("ASSERT FALSE", as.toString());
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new HexValue("")));
            assertEquals("ASSERT ", as.toString()); // Wrong???
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new HexValue("0x00")));
            assertEquals("ASSERT 0x00", as.toString());
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new HexValue("0x12345678")));
            assertEquals("ASSERT 0x12345678", as.toString());
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new HexValue("0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")));
            assertEquals("ASSERT 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", as.toString());
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new NumberValue(-1)));
            assertEquals("ASSERT -1", as.toString());
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new NumberValue(0)));
            assertEquals("ASSERT 0", as.toString());
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new NumberValue(1)));
            assertEquals("ASSERT 1", as.toString());
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new StringValue("")));
            assertEquals("ASSERT ", as.toString()); // Wrong???
        }
        {
            ASSERTstatement as = new ASSERTstatement(new ConstantExpression(new StringValue("Hello World")));
            assertEquals("ASSERT Hello World", as.toString());
        }
    }

    @Test
    public void testExecution() {
        {
            ConstantExpression ce = new ConstantExpression(new BooleanValue(true));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                as.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new BooleanValue(false));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                as.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new HexValue(""));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new HexValue("0x00"));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new HexValue("0x12345678"));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new HexValue("0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new NumberValue(-1));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new NumberValue(0));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new NumberValue(1));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new StringValue(""));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new StringValue("Hello World"));
            ASSERTstatement as = new ASSERTstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	as.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
    }

}
