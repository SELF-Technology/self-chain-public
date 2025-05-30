package org.self.tests.selfscript.functions.txn.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.self.database.SelfDB;
import org.self.database.mmr.MMRProof;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.ExecutionException;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.expressions.ConstantExpression;
import org.self.selfscript.functions.SelfFunction;
import org.self.selfscript.functions.txn.input.GETINID;
import org.self.selfscript.values.BooleanValue;
import org.self.selfscript.values.HexValue;
import org.self.selfscript.values.NumberValue;
import org.self.selfscript.values.StringValue;
import org.self.selfscript.values.Value;
import org.self.objects.Address;
import org.self.objects.Coin;
import org.self.objects.CoinProof;
import org.self.objects.ScriptProof;
import org.self.objects.Token;
import org.self.objects.Transaction;
import org.self.objects.Witness;
import org.self.objects.base.MiniData;
import org.self.objects.base.MiniNumber;

//HEXValue GETINID (NumberValue input)
public class GETINIDTests {

    @Test
    public void testConstructors() {
        GETINID fn = new GETINID();
        SelfFunction mf = fn.getNewFunction();

        assertEquals("GETINID", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = SelfFunction.getFunction("GETINID");
            assertEquals("GETINID", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (SelfParseException ex) {
            fail();
        }
    }

    public static Address newSimpleAddress() {
    	//Random public key
    	MiniData pubk = MiniData.getRandomData(32);
    	
    	//Create a simple address
    	String simpleaddress = new String("RETURN SIGNEDBY("+pubk.to0xString()+")"); 
    	
    	//Now create the address
    	Address addr = new Address(simpleaddress);
    	
    	return addr;
    }
    
    @Test
    public void testValidParams() {

    	Address addr1 = newSimpleAddress();
        Address addr2 = newSimpleAddress();
        Address addr3 = newSimpleAddress();
        Address addr4 = newSimpleAddress();

        Transaction trx = new Transaction();

        Coin in1 = new Coin(Coin.COINID_OUTPUT, addr1.getAddressData(), new MiniNumber("25"), Token.TOKENID_SELF);
        trx.addInput(in1);

        Coin in2 = new Coin(Coin.COINID_OUTPUT, addr2.getAddressData(), new MiniNumber("75"), Token.TOKENID_SELF);
        trx.addInput(in2);

        Coin out1 = new Coin(Coin.COINID_OUTPUT, addr3.getAddressData(), new MiniNumber("40"), Token.TOKENID_SELF);
        trx.addOutput(out1);

        Coin out2 = new Coin(Coin.COINID_OUTPUT, addr4.getAddressData(), new MiniNumber("60"), Token.TOKENID_SELF);
        trx.addOutput(out2);

        Witness w = new Witness();
        try {
        	w.addScript(new ScriptProof(addr1.getScript()));
        	w.addScript(new ScriptProof(addr2.getScript()));
        } catch (Exception ex) {
            fail();
        }

        //Add the coin proofs to the witness
        w.addCoinProof(new CoinProof(in1, new MMRProof()));
        w.addCoinProof(new CoinProof(in2, new MMRProof()));
        
        Contract ctr = new Contract("", "", w, trx, new ArrayList<>());

        GETINID fn = new GETINID();

        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_HEX, res.getValueType());
                assertEquals(in1.getCoinID(), ((HexValue) res).getMiniData());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(1)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_HEX, res.getValueType());
                assertEquals(in2.getCoinID(), ((HexValue) res).getMiniData());
            } catch (ExecutionException ex) {
                fail();
            }
        }
    }

    @Test
    public void testInvalidParams() {

    	Address addr1 = newSimpleAddress();
        Address addr2 = newSimpleAddress();
        Address addr3 = newSimpleAddress();
        Address addr4 = newSimpleAddress();

        Transaction trx = new Transaction();

        Coin in1 = new Coin(Coin.COINID_OUTPUT, addr1.getAddressData(), new MiniNumber("25"), Token.TOKENID_SELF);
        trx.addInput(in1);

        Coin in2 = new Coin(Coin.COINID_OUTPUT, addr2.getAddressData(), new MiniNumber("75"), Token.TOKENID_SELF);
        trx.addInput(in2);

        Coin out1 = new Coin(Coin.COINID_OUTPUT, addr3.getAddressData(), new MiniNumber("40"), Token.TOKENID_SELF);
        trx.addOutput(out1);

        Coin out2 = new Coin(Coin.COINID_OUTPUT, addr4.getAddressData(), new MiniNumber("60"), Token.TOKENID_SELF);
        trx.addOutput(out2);

        Witness w = new Witness();
        try {
        	w.addScript(new ScriptProof(addr1.getScript()));
        	w.addScript(new ScriptProof(addr2.getScript()));
        } catch (Exception ex) {
            fail();
        }

        Contract ctr = new Contract("", "", w, trx, new ArrayList<>());

        GETINID fn = new GETINID();

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
            assertThrows(ExecutionException.class, () -> { // should throw this
                Value res = mf.runFunction(ctr);
            });
        }
        {
            SelfFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(35)));
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
