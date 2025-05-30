package org.self.tests.selfscript.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.NumberValue;

public class SelfFunctionTests {

    @Test
    public void testConstructors() {

        SelfFunction mf;
        try {
            mf = SelfFunction.getFunction("CONCAT");
            assertEquals("CONCAT", mf.getName());
        } catch (SelfParseException ex) {
            fail();
        }

        assertThrows(SelfParseException.class, () -> {
            SelfFunction.getFunction("DUMMYFUNCTION");
        });

        DUMMYFUNCTION df = new DUMMYFUNCTION();
        mf = df.getNewFunction();

        assertEquals("DUMMYFUNCTION", mf.getName());
        assertEquals(0, mf.getParameterNum());
    }

    @Test
    public void testGettersAndSetters() {
        DUMMYFUNCTION df = new DUMMYFUNCTION();
        SelfFunction mf = df.getNewFunction();

        assertEquals("DUMMYFUNCTION", mf.getName());
        assertEquals(0, mf.getParameterNum());

        ConstantExpression ce1 = new ConstantExpression(new BooleanValue(true));
        mf.addParameter(ce1);
        assertEquals(1, mf.getParameterNum());
        try {
            assertEquals(ce1, mf.getParameter(0));
        } catch (ExecutionException e) {
            fail();
        }

        ConstantExpression ce2 = new ConstantExpression(new NumberValue(1));
        mf.addParameter(ce2);
        try {
            assertEquals(ce2, mf.getParameter(1));
        } catch (ExecutionException e) {
            fail();
        }

        assertThrows(ExecutionException.class, () -> {
            mf.getParameter(2);
        });

        assertEquals(2, mf.getParameterNum());
    }
}
