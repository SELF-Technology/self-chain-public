package org.self.tests.selfscript.functions.maths;

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
import org.self.selfscript.functions.number.DEC;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.Transaction;
import org.self.objects.Witness;

//NumberValue DEC (NumberValue var)
public class DECTests {

    @Test
    public void testConstructors() {
        DEC fn = new DEC();
        SelfFunction mf = fn.getNewFunction();

        assertEquals("DEC", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = SelfFunction.getFunction("DEC");
            assertEquals("DEC", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (SelfParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        DEC fn = new DEC();

        { // More tests to be added, once the arithmetic is fixed, and we now the upper limits
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue(-1)));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-2", ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue("-0.99999999999999999")));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-1.99999999999999999", ((NumberValue) res).toString()); // should be -1.99999999999999999
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue("-0.99999")));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-1.99999", ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue("-0.49999999999999999")));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-1.49999999999999999", ((NumberValue) res).toString()); // should be -1.49999999999999999
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue("-0.49999")));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-1.49999", ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue(0)));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-1", ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue("0.49999")));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-0.50001", ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue("0.49999999999999999")));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-0.50000000000000001", ((NumberValue) res).toString()); // should be -0.50000000000000001
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue("0.99999")));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-0.00001", ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue("0.99999999999999999")));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals("-0.00000000000000001", ((NumberValue) res).toString()); // should be -0.00000000000000001
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
                    assertEquals("0", ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }

        }
    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        DEC fn = new DEC();

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
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
    }
}
