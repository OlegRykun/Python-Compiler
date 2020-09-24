/**
 * Python compiler
 * Rykun Oleg IO-82
 * Main class
 */

public class Lab1 {
    public static void main(String[] args) {
        long time = System.nanoTime();
        System.out.println("\nPython file compilation is started");

        /**
         * Sign a Python file
         */
        Lexer lexer = new Lexer("1-16-IO-82-Rykun.py", true);

        /**
         * All tokens output that were seen in the Python file
         */
        lexer.printToken();

        System.out.println("\n========================================\n");

        Parser parser = new Parser(lexer.getTokens());

        System.out.println("\n========================================\n");

        /**
         * Create an Assembler file
         */
        ASS_Gen asm_creator = new ASS_Gen(parser.getMainAST(), parser.getDefAST());
        String filePath = "1-16-IO-82-Rykun.asm";
        boolean success = asm_creator.createAssFile(filePath);

        /**
         * Information output about compilation(success or fail, directory with ASS-file and time)
         */
        time = System.nanoTime() - time;
        if (success){
            System.out.println("Compilation was successful,\n'code.asm' is located in " +
                    System.getProperty("user.dir") + "\\" + filePath);
            System.out.println(String.format("\nTime: %,9.3f ms\n", time/1000000.0));
        }
        else {
            System.err.println("Compilation was failed");
        }
    }
}