class Main {
    private static String encodingName = "UTF-8";
    private static LexicalAnalyzer lexer;

    public static void main(String[] args) {
        try {
            java.io.FileInputStream input = new java.io.FileInputStream(args[0]);
            java.io.Reader reader;
            reader = new java.io.InputStreamReader(input, encodingName);
            lexer = new LexicalAnalyzer(reader);
            lexer.yylex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}