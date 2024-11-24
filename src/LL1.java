import java.util.*;

public class LL1 {

    private static LL1 instance;

    public Map<LexicalUnit, int[]> table = new HashMap<>();
    public List<LexicalUnit>
            programFirstSet,
            codeFirstSet,
            instructionFirstSet,
            assignFirstSet,
            ifFirstSet,
            ifTailFirstSet,
            whileFirstSet,
            outFirstSet,
            inputFirstSet,
            unitFirstSet,
            exprFirstSet,
            exprPrimeFirstSet,
            exprArithFirstSet,
            termFirstSet,
            termPrimeFirstSet,
            mulDivFirstSet,
            condFirstSet, condPrimeFirstSet, compFirstSet,
            plusMinusFirstSet
                    = new ArrayList<>();


    public static LL1 getInstance() {
        if (instance == null) {
            instance = new LL1();
        }
        return instance;
    }

    public LL1() {
        table = Map.ofEntries(
                Map.entry(LexicalUnit.LET, new int[]{1}),
                Map.entry(LexicalUnit.PROGNAME, new int[]{}),
                Map.entry(LexicalUnit.BE, new int[]{}),
                Map.entry(LexicalUnit.END, new int[]{11}),
                Map.entry(LexicalUnit.COLON, new int[]{2}),
                Map.entry(LexicalUnit.VARNAME, new int[]{4, 9, 16, 17, 22, 27, 30}),
                Map.entry(LexicalUnit.ASSIGN, new int[]{}),
                Map.entry(LexicalUnit.IF, new int[]{5, 10}),
                Map.entry(LexicalUnit.LBRACK, new int[]{}),
                Map.entry(LexicalUnit.RBRACK, new int[]{}),
                Map.entry(LexicalUnit.THEN, new int[]{}),
                Map.entry(LexicalUnit.ELSE, new int[]{12}),
                Map.entry(LexicalUnit.WHILE, new int[]{6, 13}),
                Map.entry(LexicalUnit.REPEAT, new int[]{}),
                Map.entry(LexicalUnit.OUTPUT, new int[]{7, 14}),
                Map.entry(LexicalUnit.INPUT, new int[]{8, 15}),
                Map.entry(LexicalUnit.PLUS, new int[]{18, 20}),
                Map.entry(LexicalUnit.MINUS, new int[]{16, 17, 18, 21, 22, 27, 30}),
                Map.entry(LexicalUnit.TIMES, new int[]{23, 25}),
                Map.entry(LexicalUnit.DIVIDE, new int[]{23, 26}),
                Map.entry(LexicalUnit.EQUAL, new int[]{33}),
                Map.entry(LexicalUnit.SMALEQ, new int[]{34}),
                Map.entry(LexicalUnit.SMALLER, new int[]{35}),
                Map.entry(LexicalUnit.NUMBER, new int[]{16, 17, 22, 28, 30}),
                Map.entry(LexicalUnit.LPAREN, new int[]{16, 17, 22, 29, 30}),
                Map.entry(LexicalUnit.RPAREN, new int[]{}),
                Map.entry(LexicalUnit.IMPLIES, new int[]{31})
        );

        programFirstSet = List.of(LexicalUnit.LET);
        instructionFirstSet = List.of(LexicalUnit.VARNAME, LexicalUnit.IF, LexicalUnit.WHILE, LexicalUnit.OUTPUT, LexicalUnit.INPUT);
        codeFirstSet = instructionFirstSet; //+Epsilon
        assignFirstSet = List.of(LexicalUnit.VARNAME);
        ifFirstSet = List.of(LexicalUnit.IF);
        ifTailFirstSet = List.of(LexicalUnit.END, LexicalUnit.ELSE);
        whileFirstSet = List.of(LexicalUnit.WHILE);
        outFirstSet = List.of(LexicalUnit.OUTPUT);
        inputFirstSet = List.of(LexicalUnit.INPUT);
        unitFirstSet = List.of(LexicalUnit.MINUS, LexicalUnit.VARNAME, LexicalUnit.NUMBER, LexicalUnit.LPAREN);
        exprFirstSet = unitFirstSet;
        exprPrimeFirstSet = List.of(LexicalUnit.PLUS, LexicalUnit.MINUS); //+Epsilon
        exprArithFirstSet = exprFirstSet;
        termFirstSet = unitFirstSet;
        mulDivFirstSet = List.of(LexicalUnit.TIMES, LexicalUnit.DIVIDE);
        termPrimeFirstSet = mulDivFirstSet;
        plusMinusFirstSet = List.of(LexicalUnit.PLUS, LexicalUnit.MINUS);
        condFirstSet = List.of(LexicalUnit.PIPE, LexicalUnit.MINUS, LexicalUnit.VARNAME, LexicalUnit.NUMBER, LexicalUnit.LPAREN);
        condPrimeFirstSet = List.of(LexicalUnit.IMPLIES); //+Epsilon
        compFirstSet = List.of(LexicalUnit.EQUAL, LexicalUnit.SMALEQ, LexicalUnit.SMALLER);
    }
}
