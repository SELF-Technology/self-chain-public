package org.self.tests.selfscript.functions.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.functions.general.GET;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.Transaction;
import org.self.objects.Witness;

//Value GET (Expression exp1 … Expression expN)
public class GETTests {

    @Test
    public void testConstructors() {
        GET fn = new GET();
        SelfFunction mf = fn.getNewFunction();

        assertEquals("GET", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = SelfFunction.getFunction("GET");
            assertEquals("GET", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (SelfParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
        ctr.setVariable("0,", new NumberValue(1));
        ctr.setVariable("1,", new NumberValue(2));
        ctr.setVariable("0,0,", new NumberValue(3));
        ctr.setVariable("0,1,", new NumberValue(4));
        ctr.setVariable("1,0,", new NumberValue(5));
        ctr.setVariable("1,1,", new NumberValue(6));
        ctr.setVariable("0,0,0,0,0,", new NumberValue(7));
        ctr.setVariable("0,0,0,0,1,", new NumberValue(8));
        ctr.setVariable("0,0,0,41,0,", new NumberValue(9));
        ctr.setVariable("0,0,32,0,0,", new NumberValue(10));
        ctr.setVariable("5,5,5,5,5,", new NumberValue(11));
        ctr.setVariable("TRUE,0x1234,-99,hello world,LET a = 5,", new NumberValue(12));

        GET fn = new GET();

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(1, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(1)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(2, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(3, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(1)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(4, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(1)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(5, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(1)));
            mf.addParameter(new ConstantExpression(new NumberValue(1)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(6, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(7, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(1)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(8, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(41)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(9, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(32)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(10, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(5)));
            mf.addParameter(new ConstantExpression(new NumberValue(5)));
            mf.addParameter(new ConstantExpression(new NumberValue(5)));
            mf.addParameter(new ConstantExpression(new NumberValue(5)));
            mf.addParameter(new ConstantExpression(new NumberValue(5)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals(11, ((NumberValue) res).getNumber().getAsInt());
            } catch (ExecutionException ex) {
                fail();
            }
        }
    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
        ctr.setVariable("0,", new NumberValue(1));
        ctr.setVariable("1,", new NumberValue(2));
        ctr.setVariable("0,0,", new NumberValue(3));
        ctr.setVariable("0,1,", new NumberValue(4));
        ctr.setVariable("1,0,", new NumberValue(5));
        ctr.setVariable("1,1,", new NumberValue(6));
        ctr.setVariable("0,0,0,0,0,", new NumberValue(7));
        ctr.setVariable("0,0,0,0,1,", new NumberValue(8));
        ctr.setVariable("0,0,0,41,0,", new NumberValue(9));
        ctr.setVariable("0,0,32,0,0,", new NumberValue(10));
        ctr.setVariable("5,5,5,5,5,", new NumberValue(11));

        GET fn = new GET();

        // Invalid param count
        {
            SelfFunction mf = fn.getNewFunction();
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        // Invalid param domain
        {
        }

        // Invalid param types
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x1234")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("HELLO WORLD")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(1234567890)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
    }
}
