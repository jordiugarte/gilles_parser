import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

class Main {
    private static String encodingName = "UTF-8";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar part2.jar [OPTION] [FILE]");
            return;
        }

        try {
            String option = args[0];
            String sourceFile = args[args.length - 1];

            FileInputStream input = new FileInputStream(sourceFile);
            Reader reader = new InputStreamReader(input, encodingName);

            LexicalAnalyzer lexer = new LexicalAnalyzer(reader);
            Parser parser = new Parser(lexer);
            ParseTree tree = parser.parse();
            List<Integer> derivation = parser.getDerivation();

            if (option.equals("-wt")) {
                if (args.length != 3) {
                    System.err.println("Usage: java -jar part2.jar -wt outputFile.tex sourceFile.gls");
                    return;
                }
                String outputTexFile = args[1];
                saveToFile(outputTexFile, tree.toLaTeX());
            }
            System.out.println(derivation.stream().map(Object::toString).collect(Collectors.joining(" ")));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void saveToFile(String fileName, String content) {
        try (PrintWriter fileWriter = new PrintWriter(fileName)) {
            fileWriter.print(content);
            // System.out.println("LaTex output saved to " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
