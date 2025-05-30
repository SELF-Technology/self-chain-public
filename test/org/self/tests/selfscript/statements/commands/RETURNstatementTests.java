package org.self.tests.selfscript.statements.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.statements.commands.RETURNstatement;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.objects.Transaction;
import org.self.objects.Witness;

public class RETURNstatementTests {

    @Test
    public void testConstructors() {
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new BooleanValue(true)));
            assertEquals("RETURN TRUE", rs.toString());
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new BooleanValue(false)));
            assertEquals("RETURN FALSE", rs.toString());
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new HexValue("")));
            assertEquals("RETURN ", rs.toString()); // Wrong???
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new HexValue("0x00")));
            assertEquals("RETURN 0x00", rs.toString());
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new HexValue("0x12345678")));
            assertEquals("RETURN 0x12345678", rs.toString());
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new HexValue("0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")));
            assertEquals("RETURN 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", rs.toString());
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new NumberValue(-1)));
            assertEquals("RETURN -1", rs.toString());
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new NumberValue(0)));
            assertEquals("RETURN 0", rs.toString());
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new NumberValue(1)));
            assertEquals("RETURN 1", rs.toString());
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new StringValue("")));
            assertEquals("RETURN ", rs.toString()); // Wrong???
        }
        {
            RETURNstatement rs = new RETURNstatement(new ConstantExpression(new StringValue("Hello World")));
            assertEquals("RETURN Hello World", rs.toString());
        }
    }

    @Test
    public void testExecution() {
        {
            ConstantExpression ce = new ConstantExpression(new BooleanValue(true));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                rs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(true, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new BooleanValue(false));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            try {
                rs.execute(ctr);
            } catch (ExecutionException ex) {
                fail();
            }
            assertEquals(true, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new HexValue(""));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new HexValue("0x00"));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new HexValue("0x12345678"));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new HexValue("0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new NumberValue(-1));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new NumberValue(0));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new NumberValue(1));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new StringValue(""));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
        {
            ConstantExpression ce = new ConstantExpression(new StringValue("Hello World"));
            RETURNstatement rs = new RETURNstatement(ce);
            Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
            assertThrows(ExecutionException.class, () -> {
            	rs.execute(ctr);
            });
            assertEquals(false, ctr.isSuccessSet());
            assertEquals(false, ctr.isSuccess());
        }
    }

}
