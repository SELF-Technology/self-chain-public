package org.self.tests.selfscript.expressions;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.objects.StateVariable;
import org.self.objects.Transaction;
import org.self.objects.Witness;
import org.self.objects.base.MiniNumber;

public class ConstantExpressionTests {

    @Test
    public void testConstructors() throws ExecutionException {
        BooleanValue bv = new BooleanValue(true);
        HexValue hv = new HexValue(new MiniNumber(Integer.valueOf(255)));
        NumberValue nv = new NumberValue(0x12345678);
        StringValue sv = new StringValue("[RETURN TRUE]");

        ConstantExpression ce1 = new ConstantExpression(bv);
        ConstantExpression ce2 = new ConstantExpression(hv);
        ConstantExpression ce3 = new ConstantExpression(nv);
        ConstantExpression ce4 = new ConstantExpression(sv);

        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<StateVariable>());

        assertEquals("should be equal ", bv, ce1.getValue(ctr));
        assertEquals("should be equal ", hv, ce2.getValue(ctr));
        assertEquals("should be equal ", nv, ce3.getValue(ctr));
        assertEquals("should be equal ", sv, ce4.getValue(ctr));
    }

    @Test
    public void testToString() throws ExecutionException {
        BooleanValue bv = new BooleanValue(true);
        HexValue hv = new HexValue(new MiniNumber(Integer.valueOf(255)));
        NumberValue nv = new NumberValue(0x12345678);
        StringValue sv = new StringValue("[RETURN TRUE]");

        ConstantExpression ce1 = new ConstantExpression(bv);
        ConstantExpression ce2 = new ConstantExpression(hv);
        ConstantExpression ce3 = new ConstantExpression(nv);
        ConstantExpression ce4 = new ConstantExpression(sv);

        String exp_s;
        String obj_s;

        exp_s = bv.toString();
        obj_s = ce1.toString();
        assertEquals("should be equal ", exp_s, obj_s);

        exp_s = hv.toString();
        obj_s = ce2.toString();
        assertEquals("should be equal ", exp_s, obj_s);

        exp_s = nv.toString();
        obj_s = ce3.toString();
        assertEquals("should be equal ", exp_s, obj_s);

        exp_s = sv.toString();
        obj_s = ce4.toString();
        assertEquals("should be equal ", exp_s, obj_s);
    }

}
