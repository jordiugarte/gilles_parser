package com.gilles;

class Main {

    private static String encodingName = "UTF-8";

    public static void main(String[] args) {
        try {
            java.io.FileInputStream input = new java.io.FileInputStream(args[0]);
            java.io.Reader reader;
            reader = new java.io.InputStreamReader(input, encodingName);
            com.gilles.LexicalAnalyzer lexer = new com.gilles.LexicalAnalyzer(reader);
            lexer.yylex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}