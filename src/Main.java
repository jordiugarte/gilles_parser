import java.util.List;
import java.util.stream.Collectors;

class Main {
    private static String encodingName = "UTF-8";
    private static LexicalAnalyzer lexer;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar part2.jar [OPTION] [FILE]");
            return;
        }

        try {
            String option = args[0];
            String sourceFile = args[args.length - 1];
            java.io.FileInputStream input = new java.io.FileInputStream(sourceFile);
            java.io.Reader reader = new java.io.InputStreamReader(input, encodingName);
            lexer = new LexicalAnalyzer(reader);
            Parser parser = new Parser(lexer);
            ParseTree tree = parser.parse();
            List<Integer> derivation = parser.getDerivation();

            switch (option) {
                case "-wt":
                    if (args.length != 3) {
                        System.err.println("Usage: java -jar part2.jar -wt outputFile.tex sourceFile.gls");
                        return;
                    }
                    String outputTexFile = args[1];
                    saveToFile(outputTexFile, tree.toLaTeX());
                    break;
                default:
                    System.out.println(tree.toLaTexTree());
                    break;
            }
            System.out.println(derivation.stream().map(Object::toString).collect(Collectors.joining(" ")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveToFile(String fileName, String content) {
        try (java.io.PrintWriter out = new java.io.PrintWriter(fileName)) {
            out.print(content);
            System.out.println("LaTex output saved to " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
