package org.self.tests.selfscript.functions.maths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.functions.number.MIN;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.Transaction;
import org.self.objects.Witness;

//NumberValue MIN (NumberValue var1 … NumberValue varN)
public class MINTests {

    @Test
    public void testConstructors() {
        MIN fn = new MIN();
        SelfFunction mf = fn.getNewFunction();

        assertEquals("MIN", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = SelfFunction.getFunction("MIN");
            assertEquals("MIN", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (SelfParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
        ctr.setMaxInstructions(Integer.MAX_VALUE);
        
        MIN fn = new MIN();

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals("0", ((NumberValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue(Integer.MIN_VALUE)));
                mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextInt())));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Integer.toString(Integer.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue(Long.MIN_VALUE)));
                mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextLong())));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Long.toString(Long.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                SelfFunction mf = fn.getNewFunction();
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextInt())));
                }
                mf.addParameter(new ConstantExpression(new NumberValue(Integer.MIN_VALUE)));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Integer.toString(Integer.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                SelfFunction mf = fn.getNewFunction();
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextLong())));
                }
                mf.addParameter(new ConstantExpression(new NumberValue(Long.MIN_VALUE)));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Long.toString(Long.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                SelfFunction mf = fn.getNewFunction();
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextInt())));
                }
                mf.addParameter(new ConstantExpression(new NumberValue(Integer.MIN_VALUE)));
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextInt())));
                }
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Integer.toString(Integer.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                SelfFunction mf = fn.getNewFunction();
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextLong())));
                }
                mf.addParameter(new ConstantExpression(new NumberValue(Long.MIN_VALUE)));
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextLong())));
                }
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Long.toString(Long.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }

    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        MIN fn = new MIN();

        // Invalid param count
        {
            SelfFunction mf = fn.getNewFunction();
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
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
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
    }
}
