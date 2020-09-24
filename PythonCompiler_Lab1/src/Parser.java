import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Class Parser
 * Parser task, using the tokens received from the Lexer, to form an AST
 */
public class Parser {

    private ArrayList<Token> tokens;
    private HashMap<String, AST> defAST;
    private AST mainAST;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.defAST = new HashMap<>();
        this.mainAST = new AST(new Token(null, "START"));

        /**
         * Create a function tree
         */
        for (Iterator<Token> tokenIterator = tokens.iterator(); tokenIterator.hasNext();) {
            Token token = tokenIterator.next();
            switch (token.getType()){
                case "DEF": {
                    AST tmp = new AST(parseDef(token, tokenIterator));
                    defAST.put(tmp.getRoot().getCurrent().getValue(), tmp);
                    break;
                }
                case "WORD": {
                    mainAST.getRoot().appendChild(parseWord(token, tokenIterator));
                    break;
                }
                default: continue;
            }
        }
    }

    /**
     * Parsing a Python def (function)
     */
    private Node_AST parseDef(Token prev, Iterator<Token> tokenIterator)  {
        int[] currentSpaceTabCount = {0, 0};
        String defName;
        /**
         * Checking for words
         */
        Token token = tokenIterator.next();
        if (!token.getType().equals("WORD")){
            fail(1, token);
        }
        /**
         * Checking for left and right brackets
         */
        defName = token.getValue();
        token = tokenIterator.next();
        if (!token.getType().equals("LBR")){
            fail(1, token);
        }
        token = tokenIterator.next();
        if (!token.getType().equals("RBR")){
            fail(1, token);
        }
        /**
         * Checking for colon
         */
        token = tokenIterator.next();
        if (!token.getType().equals("COLON")){
            fail(1, token);
        }
        /**
         * Checking for \n synbols
         */
        token = tokenIterator.next();
        if (!token.getType().equals("NEW_LINE")){
            fail(1, token);
        }
        token = tokenIterator.next();
        while (token.getType().matches("(TAB)|(SPACE)|(NEW_LINE)")){
            if (token.getType().equals("TAB")){
                currentSpaceTabCount[1]++;
            }
            else {
                currentSpaceTabCount[0]++;
            }
            token = tokenIterator.next();
        }

        Node_AST statement = parseStatement(token, tokenIterator, currentSpaceTabCount);
        Node_AST def = new Node_AST(new Token(defName, "DEF_WORD"));
        def.appendChild(statement);
        statement.setParent(def);

        return def;
    }

    /**
     * Parsing Python return
     */
    private Node_AST parseStatement(Token prev, Iterator<Token> tokenIterator, int[] spaceTabCount) {
        /**
         * Checking TAB before return
         */
        if (spaceTabCount[0] + spaceTabCount[1] == 0){
            fail(0, prev);
        }
        /**
         * Checking return
         */
        if (!prev.getType().equals("RETURN")){
            fail(1, prev);
        }
        /**
         * Checking value after return
         */
        Token token = tokenIterator.next();
        if (!token.getType().equals("INT") && !token.getType().equals("FLOAT") &&
                !token.getType().equals("HEXNUM")){
            fail(2, token);
        }
        Node_AST exp = parseExpression(token);
        Node_AST statement = new Node_AST(new Token("return", "RETURN"));
        statement.appendChild(exp);
        exp.setParent(statement);

        token = tokenIterator.next();
        if (!token.getType().equals("NEW_LINE")){
            fail(1, token);
        }

        return statement;
    }

    /**
     * Parsing value after return
     */
    private Node_AST parseExpression(Token token) {
        String value = token.getValue();

        switch (token.getType()){
            case "INT": {
                return new Node_AST(token);
            }
            case "FLOAT": {
                StringBuilder casted = new StringBuilder();
                for (char ch: value.toCharArray()) {
                    if (ch == '.'){
                        break;
                    }
                    casted.append(ch);
                }
                return new Node_AST(new Token(casted.toString(),
                        "INT(FLOAT)"));
            }
            case "HEXNUM": {
                return new Node_AST(new Token(Long.decode(value).toString(),
                        "INT(HEXNUM)"));
            }
//            case "OCTNUM": {
//                return new Node_AST(new Token(Integer.parseInt(value.substring(2), 8)+"",
//                        "INT(OCTNUM)"));
//            }
//            case "BINNUM": {
//                return new Node_AST(new Token(Integer.parseInt(value.substring(2), 2)+"",
//                        "INT(BINNUM)"));
//            }
//            case "STRING": {
//                if (value.length() == 1){
//                    return new Node_AST(new Token(Character.getNumericValue(value.toCharArray()[0])+"",
//                            "INT(CHAR)", token.getRow(), token.getColumn()));
//                }
//                else {
//                    fail(2, token);
//                }
//                break;
//            }
        }
        return null;
    }

    /**
     * Parsing name of function
     */
    private Node_AST parseWord(Token prev, Iterator<Token> tokenIterator)  {
        Token token = tokenIterator.next();

        switch (token.getType()){
            case "LBR" : {
                return parseFuncCall(prev, tokenIterator);
            }
            default: {
                fail(3, token);
                return null;
            }
        }
    }

    /**
     * Parsing function call
     */
    private Node_AST parseFuncCall(Token prev, Iterator<Token> tokenIterator) {
        Token token = tokenIterator.next();

        if (!token.getType().equals("RBR")){
            fail(1, token);
        }
        token = tokenIterator.next();
        if (!token.getType().equals("NEW_LINE")){
            fail(1, token);
        }
        Node_AST defCall = new Node_AST(new Token(prev.getValue(), "DEF_CALL"));
        if (!defAST.containsKey(prev.getValue())){
            fail(4, prev);
        }
        else {
            defCall.appendChild(defAST.get(prev.getValue()).getRoot());
        }

        return defCall;
    }

    /**
     * Parsing mistakes output
     */
    private void fail(int errId, Token token) {
        String message = "";

        switch (errId){
            case 0: {
                message = "Incorrect tab count";
                break;
            }
            case 1: {
                message = "Incorrect type";
                break;
            }
            case 2: {
                message = "Cannot cast to INT";
                break;
            }
            case 3: {
                message = "Unexpected token";
                break;
            }
            case 4: {
                message = "Unknown method call";
                break;
            }
            default: message = "Unknown error";
        }
        System.out.println(message);
    }

    public HashMap<String, AST> getDefAST() {
        return defAST;
    }

    public AST getMainAST() {
        return mainAST;
    }
}