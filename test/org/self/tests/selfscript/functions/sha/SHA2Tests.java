package org.self.tests.selfscript.functions.sha;

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
import org.self.selfscript.functions.sha.SHA2;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.Transaction;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.utils.Crypto;

//HEXValue SHA2 (HEXValue data)
//HEXValue SHA2 (ScriptValue data)
public class SHA2Tests {

    @Test
    public void testConstructors() {
        SHA2 fn = new SHA2();
        SelfFunction mf = fn.getNewFunction();

        assertEquals("SHA2", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = SelfFunction.getFunction("SHA2");
            assertEquals("SHA2", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (SelfParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        SHA2 fn = new SHA2();

        {
            for (int i = 0; i < 100; i++) {
                HexValue Param = new HexValue(MiniData.getRandomData(64).to0xString());
                HexValue Result = new HexValue(Crypto.getInstance().hashSHA2(Param.getRawData()));

                SelfFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(Param));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_HEX, res.getValueType());
                    assertEquals(Result.toString(), ((HexValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }

        {
            HexValue Param = new HexValue("");
            HexValue Result = new HexValue(Crypto.getInstance().hashSHA2(Param.getRawData()));

            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(Param));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_HEX, res.getValueType());
                assertEquals(Result.toString(), ((HexValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("")));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_HEX, res.getValueType());
                assertEquals("0xE3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855", ((HexValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }

    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        SHA2 fn = new SHA2();

        // Invalid param count
        {
            SelfFunction mf = fn.getNewFunction();
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
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

    }
}
