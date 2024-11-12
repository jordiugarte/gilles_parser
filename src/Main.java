import java.util.List;

class Main {
    private static String encodingName = "UTF-8";
    private static LexicalAnalyzer lexer;

    public static void main(String[] args) {
        try {
            java.io.FileInputStream input = new java.io.FileInputStream(args[0]);
            java.io.Reader reader;
            reader = new java.io.InputStreamReader(input, encodingName);
            lexer = new LexicalAnalyzer(reader);
            Parser parser = new Parser(lexer);
            ParseTree tree = parser.parse();
            List<Integer> derivation = parser.getDerivation();
            System.out.println("Derivation: " + derivation);
            System.out.println(tree.toLaTexTree());
            System.out.println(tree.toTikZ());
            System.out.println(tree.toLaTeX());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
