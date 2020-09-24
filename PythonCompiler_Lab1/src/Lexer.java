import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.Map;

/**
 * Class Lexer
 * This class is needed in order to highlight tokens in the Python file.
 */
public class Lexer {
    private String parseText;
    private Map<String, String> keywords;
    private Map<String, String> symbs;
    private Map<String, String> emptyPlaces;
    private ArrayList<Token> tokens = new ArrayList<>();

    public Lexer(String fileName, boolean isFile) {
        keywords = new HashMap<>();
        symbs = new HashMap<>();
        emptyPlaces = new HashMap<>();
        fillingMaps();

        /**
         * Python file reading
         */
        if (isFile) {
            try (FileReader reader = new FileReader(fileName)) {
                int symb;
                while ((symb = reader.read()) != -1) {
                    parseText += (char) symb;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        else{
            parseText = fileName;
        }
        makeToken();
    }

    /**
     * This method declare all keywords, symbols, empty places, new lines and tabulations
     */
    public void fillingMaps() {
        keywords.put("def", "DEF");
        keywords.put("return", "RETURN");

        symbs.put(",", "COMMA");
        symbs.put(":", "COLON");
        symbs.put("(", "LBR");
        symbs.put(")", "RBR");

        emptyPlaces.put(" ", "SPACE");
        emptyPlaces.put("\n", "NEW_LINE");
        emptyPlaces.put("\t", "TAB");

    }

    /**
     * This method splits Python file to the lines
     */
    public void makeToken(){
        if (parseText.substring(0, 4).equals("null")){
            parseText = parseText.substring(4);
        }
        String[] parseLines = parseText.split("([\n])|(\n)");

        for(int i = 0; i < parseLines.length; i++){
            if(parseLine(parseLines[i], i)){
                tokens.add(new Token("\n", emptyPlaces.get("\n")));
            }
        }
    }

    public boolean parseLine(String line, int row){
        String commentLine = line.split("#")[0];
        if(commentLine.matches("^\\*$")){
            return false;
        }

        String[] symbLine = commentLine.split("");
        boolean spaceTabPlace = true;
        if(symbLine.length == 0){
            return false;
        }

        /**
         * Checking for spaces
         */
        for(int i = 0; i < symbLine.length; i++){
            if(emptyPlaces.containsKey(symbLine[i])){
                if(spaceTabPlace){
                    tokens.add(new Token(symbLine[i], emptyPlaces.get(symbLine[i])));
                }
            }
            else {
                if (symbs.containsKey(symbLine[i])){
                    tokens.add(new Token(symbLine[i], symbs.get(symbLine[i])));
                }
                /**
                 * Checking for hex, oct and bin numbers
                 */
                else {
                    if (i + 1 != symbLine.length &&
                            symbLine[i].equals("0") &&
                            symbLine[i + 1].matches("[xob]")) {
                        StringBuilder num = new StringBuilder("0" + symbLine[i + 1]);
                        short j = 2;
                        while ((i + j < symbLine.length) && symbForNum(symbLine[i + j], symbLine[i + 1]) && j < 8) {
                            num.append(symbLine[i + j]);
                            j++;
                        }
                        switch (symbLine[i + 1]) {
                            case "x":
                                tokens.add(new Token(num.toString(), "HEXNUM"));
                                //case "b": tokens.add(new Token(num.toString(), "BINNUM", row, i));
                                //case "o": tokens.add(new Token(num.toString(), "OCTNUM", row, i));
                        }

                        i += num.length() - 1;

                    } else {
                        /**
                         * Checking for int and float numbers
                         */
                        if (symbLine[i].matches("\\d")) {
                            StringBuilder num = new StringBuilder();
                            boolean isFloat = false;
                            short j = 0;
                            while (i + j < symbLine.length && symbLine[i + j].matches("[\\d.]")) {
                                if (symbLine[i + j].equals(".")) {
                                    isFloat = true;
                                }
                                num.append(symbLine[i + j]);
                                j++;
                            }
                            if (isFloat) {
                                tokens.add(new Token(num.toString(), "FLOAT"));
                            } else {
                                tokens.add(new Token(num.toString(), "INT"));
                            }
                            i += num.length() - 1;
                        } else {
                            /**
                             * Checking for words
                             */
                            if (symbLine[i].matches("[a-zA-Z]")) {
                                StringBuilder num = new StringBuilder();
                                short j = 0;
                                while (i + j < symbLine.length && symbLine[i + j].matches("\\w")) {
                                    num.append(symbLine[i + j]);
                                    j++;
                                }
                                tokens.add(new Token(num.toString(), keywords.getOrDefault(num.toString(), "WORD")));
                                spaceTabPlace = false;
                                i += num.length() - 1;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checking symbols for the hex numbers
     */
    public boolean symbForNum(String s, String system){
        switch (system){
            case "x": return s.matches("[\\da-fA-F]");
            //case "o": return s.matches("[0-7]");
            //case "b": return s.equals("0") || s.equals("1")]");
            default: return false;
        }
    }

    public String getParseText(){
        return parseText;
    }

    public ArrayList<Token> getTokens(){
        return tokens;
    }

    /**
     * Output of all recognized characters
     */
    public void printToken(){
        for (Token token: tokens){
            String val;
            switch (token.getValue()){
                case "\n": val = "\\n"; break;
                case "\t": val = "\\t"; break;
                case " ": val = "\\s"; break;
                default: val = token.getValue();
            }
            System.out.println(String.format("[ %10s <==> %-10s ]", val, token.getType()));
            if(token.getType().equals("UNDEF")) {
                System.out.println(Character.getNumericValue(val.toCharArray()[0]));
            }
        }
    }
}
