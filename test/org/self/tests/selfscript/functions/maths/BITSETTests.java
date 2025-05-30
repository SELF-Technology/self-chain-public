package org.self.tests.selfscript.functions.maths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.BitSet;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.functions.hex.BITSET;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.Transaction;
import org.self.objects.Witness;

//HEXValue BITSET (HEXValue var, NumberValue pos, BooleanVlaue val)
public class BITSETTests {

    @Test
    public void testConstructors() {
        BITSET fn = new BITSET();
        SelfFunction mf = fn.getNewFunction();

        assertEquals("BITSET", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = SelfFunction.getFunction("BITSET");
            assertEquals("BITSET", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (SelfParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        BITSET fn = new BITSET();

        {
            for (int i = 0; i < 63; i++) {
                {
                    BitSet bitSet = new BitSet(64);
                    bitSet.set(0, 64, false);
                    bitSet.set(i, true);
                    byte[] TestValue = bitSet.toByteArray();

                    SelfFunction mf = fn.getNewFunction();
                    mf.addParameter(new ConstantExpression(new HexValue(TestValue)));
                    mf.addParameter(new ConstantExpression(new NumberValue(i)));
                    mf.addParameter(new ConstantExpression(new BooleanValue(false)));
                    try {
                        Value res = mf.runFunction(ctr);
                        assertEquals(Value.VALUE_HEX, res.getValueType());
                        assertEquals("", ((HexValue) res).toString());
                    } catch (ExecutionException ex) {
                        fail();
                    }
                }

                {
                    BitSet bitSet = new BitSet(64);
                    bitSet.set(0, 64, true);
                    bitSet.set(i, false);
                    byte[] TestValue = bitSet.toByteArray();

                    SelfFunction mf = fn.getNewFunction();
                    mf.addParameter(new ConstantExpression(new HexValue(TestValue)));
                    mf.addParameter(new ConstantExpression(new NumberValue(i)));
                    mf.addParameter(new ConstantExpression(new BooleanValue(true)));
                    try {
                        Value res = mf.runFunction(ctr);
                        assertEquals(Value.VALUE_HEX, res.getValueType());
                        assertEquals("0xFFFFFFFFFFFFFFFF", ((HexValue) res).toString());
                    } catch (ExecutionException ex) {
                        fail();
                    }
                }

            }
        }
    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        BITSET fn = new BITSET();

        // Invalid param count
        {
            SelfFunction mf = fn.getNewFunction();
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new BooleanValue(false)));
            mf.addParameter(new ConstantExpression(new BooleanValue(false)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        // Invalid param domain
        {
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new HexValue("")));
                mf.addParameter(new ConstantExpression(new NumberValue(0)));
                mf.addParameter(new ConstantExpression(new BooleanValue(true)));
                assertThrows(ExecutionException.class, () -> {
                    Value res = mf.runFunction(ctr);
                });
            }
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new HexValue("0x00")));
                mf.addParameter(new ConstantExpression(new NumberValue(256)));
                mf.addParameter(new ConstantExpression(new BooleanValue(true)));
                assertThrows(ExecutionException.class, () -> {
                    Value res = mf.runFunction(ctr);
                });
            }
        }

        // Invalid param types
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new BooleanValue(false)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(123456798)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new BooleanValue(false)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new BooleanValue(false)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new BooleanValue(false)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new BooleanValue(false)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new BooleanValue(false)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            assertThrows(ExecutionException.class, () -> { // does not fail due to implicit conversion to bool
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> { // does not fail due to implicit conversion to bool
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x00")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> { // does not fail due to implicit conversion to bool
                Value res = mf.runFunction(ctr);
            });
        }

    }
}
