package org.self.tests.selfscript.statements;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.self.selfscript.Contract;
import org.self.selfscript.exceptions.SelfParseException;
import org.self.selfscript.statements.StatementBlock;
import org.self.selfscript.statements.StatementParser;
import org.self.selfscript.tokens.ScriptToken;
import org.self.utils.SelfLogger;

public class StatementParserTests {

    @Test
    public void testConstructors() {
        {
            try {
                String Script = "";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }
    }

    @Test
    public void testParsingLETArrays() {
        {
            try {
                String Script = "LET (a) = TRUE LET (b) = FALSE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "LET (a = TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "LET (a * ) = TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });	
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "LET ( ) = 5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }

        {
            try {
                String Script = "LET (a b c d e) = TRUE LET (b c d e f) = FALSE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "LET (TRUE 1 0x123456 d BOOL(1)) = TRUE LET (FALSE -1 0xABCDEF d SCRIPT(0xABCDEF)) = FALSE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
            	SelfLogger.log(ex);
            	fail();
            } catch (Exception ex) {
                fail();
            }
        }
    }

    @Test
    public void testParsingLET() {
        {
            try {
                String Script = "LET a = TRUE LET b = FALSE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "LET a = 0x12345678 LET b = 0x87654321";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "LET a = -1 LET b = 0 LET c = 1 LET d = 9999999999 LET e = -0.000005";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            assertThrows(SelfParseException.class, () -> {
                ArrayList<ScriptToken> tokens = new ArrayList<>();
                tokens.add(new ScriptToken(ScriptToken.TOKEN_COMMAND, "LET"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VARIABLE, "a"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VARIABLE, "+"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VALUE, "5"));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }

        {
            try {
                ArrayList<ScriptToken> tokens = new ArrayList<>();
                tokens.add(new ScriptToken(ScriptToken.TOKEN_COMMAND, "LET"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VARIABLE, "a"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VARIABLE, "="));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VALUE, "5"));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
        	assertThrows(SelfParseException.class, () -> {
                ArrayList<ScriptToken> tokens = new ArrayList<>();
                tokens.add(new ScriptToken(ScriptToken.TOKEN_COMMAND, "LET"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VARIABLE, "a"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_OPERATOR, "+"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VALUE, "5"));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
        	});
        }

        {
            try {
                ArrayList<ScriptToken> tokens = new ArrayList<>();
                tokens.add(new ScriptToken(ScriptToken.TOKEN_COMMAND, "LET"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VARIABLE, "a"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_OPERATOR, "="));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VALUE, "5"));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            assertThrows(SelfParseException.class, () -> {
                ArrayList<ScriptToken> tokens = new ArrayList<>();
                tokens.add(new ScriptToken(ScriptToken.TOKEN_COMMAND, "LET"));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_OPERATOR, "="));
                tokens.add(new ScriptToken(ScriptToken.TOKEN_VALUE, "5"));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }
    }

    @Test
    public void testParsingEXEC() {
        {
            try {
                String Script = "EXEC [RETURN TRUE]";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try { // Strange
                String Script = "EXEC [RETURN TRUE] + 543 RETURN TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
        	assertThrows(SelfParseException.class, () -> {
                String Script = "EXEC RETURN";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }
    }

    @Test
    public void testParsingMAST() {
        {
            try {
                String Script = "MAST 1 + 5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try { // Strange
                String Script = "MAST TRUE + 543";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
        	assertThrows(SelfParseException.class, () -> {
                String Script = "MAST RETURN TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }
    }

    @Test
    public void testParsingIF() {
        {
            String[] Exps = {"TRUE", "FALSE", "0x01", "0x00", "1", "0", "1.5", "0.0", "[hello world]", "[]"};
            for (String Exp : Exps) {
                try {
                    String Script = "IF " + Exp + " THEN RETURN TRUE ENDIF";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    StatementBlock sb = StatementParser.parseTokens(tokens,0);
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }

                try {
                    String Script = "IF " + Exp + " THEN RETURN TRUE ELSE RETURN FALSE ENDIF";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    StatementBlock sb = StatementParser.parseTokens(tokens,0);
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }

                try {
                    String Script = "IF " + Exp + " THEN RETURN TRUE ELSEIF FALSE THEN RETURN FALSE ELSE RETURN FALSE ENDIF";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    StatementBlock sb = StatementParser.parseTokens(tokens,0);
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }

                try {
                    String Script = "IF " + Exp + " THEN RETURN TRUE ELSE IF FALSE THEN RETURN FALSE ELSE RETURN FALSE ENDIF ENDIF";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    StatementBlock sb = StatementParser.parseTokens(tokens,0);
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "IF ( ) THEN RETURN TRUE ENDIF";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }
        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "IF ( A + ) THEN RETURN TRUE ENDIF";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }

        {
            assertThrows(SelfParseException.class, () -> {
                String Script = "IF FALSE THEN RETURN TRUE HELLO FALSE THEN RETURN FALSE ELSE RETURN FALSE ENDIF";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }

    }

    @Test
    public void testParsingWHILE() {
        {
            String[] Exps = {"TRUE", "FALSE", "0x01", "0x00", "1", "0", "1.5", "0.0", "[hello world]", "[]"};
            for (String Exp : Exps) {
                try {
                    String Script = "WHILE " + Exp + " DO RETURN TRUE ENDWHILE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    StatementBlock sb = StatementParser.parseTokens(tokens,0);
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }
        }
        {
            String[] Exps = {"TRUE", "FALSE", "0x01", "0x00", "1", "0", "1.5", "0.0", "[hello world]", "[]"};
            for (String Exp : Exps) {
                try {
                    String Script = "WHILE " + Exp + " DO WHILE " + Exp + " DO RETURN TRUE ENDWHILE ENDWHILE";
                    List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                    StatementBlock sb = StatementParser.parseTokens(tokens,0);
                } catch (SelfParseException ex) {
                    fail();
                } catch (Exception ex) {
                    fail();
                }
            }
        }
    }

    @Test
    public void testParsingASSERT() {
        {
            try {
                String Script = "ASSERT TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try {
                String Script = "ASSERT FALSE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
        	assertThrows(SelfParseException.class, () -> {
                String Script = "ASSERT RETURN TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }

        {
        	assertThrows(SelfParseException.class, () -> {
                String Script = "ASSERT RETURN A + @BLKNUM";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }

        {
            try { // Strange
                String Script = "ASSERT [RETURN TRUE] + 543 RETURN TRUE";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }
    }

    @Test
    public void testParsingRETURN() {
        {
            try {
                String Script = "RETURN 1 + 5";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
            try { // Strange
                String Script = "RETURN TRUE + 543";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            } catch (SelfParseException ex) {
                fail();
            } catch (Exception ex) {
                fail();
            }
        }

        {
        	assertThrows(SelfParseException.class, () -> {
                String Script = "RETURN MAST 1";
                List<ScriptToken> tokens = ScriptToken.tokenize(Contract.cleanScript(Script));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }
    }

    @Test
    public void testParsingException() {
        {
            assertThrows(SelfParseException.class, () -> {
                ArrayList<ScriptToken> tokens = new ArrayList<>();
                tokens.add(new ScriptToken(ScriptToken.TOKEN_COMMAND, "HELLO"));
                StatementBlock sb = StatementParser.parseTokens(tokens,0);
            });
        }
    }
}
