import java.io.*;

/**
 *
 * Project Part 2: Parser
 *
 * @author Marie Van Den Bogaard, Léo Exibard, Gilles Geeraerts, Sarah Winter, edited by Mrudula Balachander
 *
 */

public class Main{
    /**
     *
     * The parser
     *
     * @param args  The argument(s) given to the program
     * @throws IOException java.io.IOException if an I/O-Error occurs
     * @throws FileNotFoundException java.io.FileNotFoundException if the specified file does not exist
     *
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, SecurityException, Exception{
        // Display the usage when no arguments are given
        if(args.length == 0){
            System.out.println("Usage:  java -jar part2.jar [OPTION] [FILE]\n"
                               + "\tOPTION:\n"
                               + "\t -wt (write-tree) filename.tex: writes latex tree to filename.tex\n"
                               + "\t -dr (display-rules): writes each rule in full\n"
                               + "\tFILE:\n"
                               + "\tA .gls file containing a GILLES program\n"
                               );
            System.exit(0);
        } else {
            boolean writeTree = false;
            boolean fullOutput = false;
            boolean fullLLVMOutput = true;
            BufferedWriter bwTree = null;
            BufferedWriter bwLlvm = null;
            FileWriter fwTree = null;
            FileReader codeSource = null;
            try {
                codeSource = new FileReader(args[args.length-1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ParseTree parseTree = null;
            String tex="\\documentclass{standalone}\\begin{document}Parsing error, no tree produced.\\end{document}";

            for (int i = 0 ; i < args.length; i++) {
                if (args[i].equals("-wt") || args[i].equals("--write-tree")) {
                    writeTree = true;
                    try {
                        fwTree = new FileWriter(args[i+1]);
                        bwTree = new BufferedWriter(fwTree);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (args[i].equals("-dr") || args[i].equals("--display-rules") ) {
                    fullOutput = true;
                }
            }
            Parser parser = new Parser(codeSource);
            if (fullOutput) {parser.displayFullRules();}
            try {
                parseTree = parser.parse();
                if (writeTree) {tex=parseTree.toLaTeX();};
            } catch (ParseException e) {
                System.out.println("Error:> " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error:> " + e);
            }
            if (writeTree) {
                try {
                    bwTree.write(tex);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bwTree != null)
                            bwTree.close();
                        if (fwTree != null)
                            fwTree.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            String[] llvmResult;
            try {
                LLVMParser llvmParser = new LLVMParser();
                llvmResult = llvmParser.generate(parseTree);
                String code = llvmResult[1];
                System.out.println(code);
                if (fullLLVMOutput) {
                    try {
                        String fileName = llvmResult[0];
                        System.out.println(code);
                        try {
                            File file = new File("./dist/" + fileName);
                            bwLlvm = new BufferedWriter(new FileWriter(file));
                            bwLlvm.write(code);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (bwLlvm != null) {
                                    bwLlvm.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            } catch (RuntimeException e) {
                System.out.println("Error:> " + e);
            }
        }
    }

    /** Default constructor (should not be used) */
    private Main(){};
}
