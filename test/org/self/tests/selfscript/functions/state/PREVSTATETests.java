package org.self.tests.selfscript.functions.state;

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
import org.self.selfscript.functions.state.PREVSTATE;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.StateVariable;
import org.self.objects.Transaction;
import org.self.objects.Witness;

//BooleanValue PREVSTATE  (NumberValue statenum)
//HEXValue PREVSTATE  (NumberValue statenum)
//NumberValue PREVSTATE  (NumberValue statenum)
//ScriptValue PREVSTATE  (NumberValue statenum)
public class PREVSTATETests {

    @Test
    public void testConstructors() {
        PREVSTATE fn = new PREVSTATE();
        SelfFunction mf = fn.getNewFunction();

        assertEquals("PREVSTATE", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = SelfFunction.getFunction("PREVSTATE");
            assertEquals("PREVSTATE", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (SelfParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {

        ArrayList<StateVariable> PrevStates = new ArrayList<StateVariable>();
        for (int i = 0; i < 16; i++) {
            PrevStates.add(new StateVariable(4 * i + 0, new BooleanValue(true).toString()));
            PrevStates.add(new StateVariable(4 * i + 1, new HexValue("0x12345678").toString()));
            PrevStates.add(new StateVariable(4 * i + 2, new NumberValue(i).toString()));
            PrevStates.add(new StateVariable(4 * i + 3, new StringValue("[ Hello World " + Integer.toString(4 * i + 3)).toString() + " ]"));
        }

        Contract ctr = new Contract("", "", new Witness(), new Transaction(), PrevStates);

        PREVSTATE fn = new PREVSTATE();

        for (int i = 0; i < PrevStates.size(); i++) {
            {
                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue(i)));
                try {
                    Value res = mf.runFunction(ctr);
                    if (i % 4 == 0) {
                        assertEquals(Value.VALUE_BOOLEAN, res.getValueType());
                        assertEquals(Value.getValue(PrevStates.get(i).toString()).toString(), ((BooleanValue) res).toString());
                    }
                    if (i % 4 == 1) {
                        assertEquals(Value.VALUE_HEX, res.getValueType());
                        assertEquals(Value.getValue(PrevStates.get(i).toString()).toString(), ((HexValue) res).toString());
                    }
                    if (i % 4 == 2) {
                        assertEquals(Value.VALUE_NUMBER, res.getValueType());
                        assertEquals(Value.getValue(PrevStates.get(i).toString()).toString(), ((NumberValue) res).toString());
                    }
                    if (i % 4 == 3) {
                        assertEquals(Value.VALUE_SCRIPT, res.getValueType());
                        assertEquals(Value.getValue(PrevStates.get(i).toString()).toString(), ((StringValue) res).toString());
                    }

                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        PREVSTATE fn = new PREVSTATE();

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
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        // Invalid param domain
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(-1)));
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
            mf.addParameter(new ConstantExpression(new HexValue("0x12345678")));
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
