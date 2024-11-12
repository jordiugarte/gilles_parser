import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class Parser {
    private LexicalAnalyzer lexer;
    private Symbol currentToken;
    private List<Integer> derivation;
    private boolean errorFlag;

    public Parser(LexicalAnalyzer lexer) throws IOException {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
        this.derivation = new ArrayList<>();
        this.errorFlag = false;
    }

    public List<Integer> getDerivation() {
        return derivation;
    }

    public boolean getErrorFlag() {
        return errorFlag;
    }

    public Symbol getCurrentToken() {
        return currentToken;
    }

    public ParseTree parse() throws IOException {
        ParseTree tree = program();
        if (!errorFlag && currentToken.getType() == LexicalUnit.EOS) {
            assert tree != null;
            return tree;
        } else {
            System.out.println("Should never reach here really, " +
                    "error will be thrown in program() before it gets here");
            return null;
        }
    }

    private void error(String message) {
        throw new RuntimeException("Error: " + message);
    }

    private void match(LexicalUnit expected) throws IOException {
        if (currentToken.getType() == expected) {
            currentToken = lexer.nextToken(); // Move to the next token
        } else {
            error("Expected " + expected + " but found " + currentToken.getType());
        }
    }

    private ParseTree program() throws IOException {
        if (currentToken.getType() == LexicalUnit.LET) {
            derivation.add(1); // Rule number [1]
            // Match LET
            match(LexicalUnit.LET);
            // Match [ProgName]
            match(LexicalUnit.PROGNAME);
            // Match BE
            match(LexicalUnit.BE);
            // Parse <Code>
            ParseTree codeTree = code();
            // Match END
            match(LexicalUnit.END);
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.LET)),
                    new ParseTree(new Symbol(LexicalUnit.PROGNAME)),
                    new ParseTree(new Symbol(LexicalUnit.BE)),
                    codeTree,
                    new ParseTree(new Symbol(LexicalUnit.END))
            );
            return new ParseTree(new Symbol(LexicalUnit.PROGNAME), children);
        } else {
            error("Expected LET");
            return null;
        }
    }

    private ParseTree code() throws IOException {
        if (currentToken.getType() == LexicalUnit.VARNAME || currentToken.getType() == LexicalUnit.IF || currentToken.getType() == LexicalUnit.WHILE || currentToken.getType() == LexicalUnit.OUTPUT || currentToken.getType() == LexicalUnit.INPUT) {
            derivation.add(2); // Rule number [2]
            // Parse <Instruction>
            ParseTree instructionTree = instruction();
            // Match ;
            match(LexicalUnit.COLUMN);
            // Parse <Code>
            ParseTree codeTree = code();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    instructionTree,
                    new ParseTree(new Symbol(LexicalUnit.COLUMN)),
                    codeTree
            );
            return new ParseTree(new Symbol(LexicalUnit.COLUMN), children);
        } else {
            derivation.add(3); // Rule number [3]
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.COLUMN));
        }
    }

    private ParseTree instruction() throws IOException {
        if (currentToken.getType() == LexicalUnit.VARNAME) {
            derivation.add(4); // Rule number [4]
            // Parse <Assign>
            return assign();
        } else if (currentToken.getType() == LexicalUnit.IF) {
            derivation.add(5); // Rule number [5]
            // Parse <If>
            return ifStatement();
        } else if (currentToken.getType() == LexicalUnit.WHILE) {
            derivation.add(6); // Rule number [6]
            // Parse <While>
            return whileStatement();
        } else if (currentToken.getType() == LexicalUnit.OUTPUT) {
            derivation.add(7); // Rule number [7]
            // Parse <Output>
            return output();
        } else if (currentToken.getType() == LexicalUnit.INPUT) {
            derivation.add(8); // Rule number [8]
            // Parse <Input>
            return input();
        } else {
            error("Expected VARNAME, IF, WHILE, OUT or IN");
            return null;
        }
    }

    private ParseTree assign() throws IOException {
        if (currentToken.getType() == LexicalUnit.VARNAME) {
            derivation.add(9); // Rule number [9]
            // Match [VarName]
            match(LexicalUnit.VARNAME);
            // Match =
            match(LexicalUnit.ASSIGN);
            // Parse ExprArith
            ParseTree exprArithTree = exprArith();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.VARNAME)),
                    new ParseTree(new Symbol(LexicalUnit.ASSIGN)),
                    exprArithTree
            );
            return new ParseTree(new Symbol(LexicalUnit.ASSIGN), children);
        } else {
            error("Expected VARNAME");
            return null;
        }
    }

    private ParseTree ifStatement() throws IOException {
        if (currentToken.getType() == LexicalUnit.IF) {
            derivation.add(10); // Rule number [10]
            // Match IF
            match(LexicalUnit.IF);
            // Match {
            match(LexicalUnit.LBRACK);
            // Parse <Cond>
            ParseTree condTree = cond();
            // Match }
            match(LexicalUnit.RBRACK);
            // Match THEN
            match(LexicalUnit.THEN);
            // Parse <Code>
            ParseTree codeTree = code();
            // Parse <IfTail>
            ParseTree ifTailTree = ifTail();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.IF)),
                    new ParseTree(new Symbol(LexicalUnit.LBRACK)),
                    condTree,
                    new ParseTree(new Symbol(LexicalUnit.RBRACK)),
                    new ParseTree(new Symbol(LexicalUnit.THEN)),
                    codeTree,
                    ifTailTree
            );
            return new ParseTree(new Symbol(LexicalUnit.IF), children);
        } else {
            error("Expected IF");
            return null;
        }
    }

    private ParseTree ifTail() throws IOException {
        if (currentToken.getType() == LexicalUnit.END) {
            derivation.add(11); // Rule number [11]
            // Match END
            match(LexicalUnit.END);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.END));
        } else if (currentToken.getType() == LexicalUnit.ELSE) {
            derivation.add(12); // Rule number [12]
            // Match ELSE
            match(LexicalUnit.ELSE);
            // Parse <Code>
            ParseTree codeTree = code();
            // Match END
            match(LexicalUnit.END);
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.ELSE)),
                    codeTree,
                    new ParseTree(new Symbol(LexicalUnit.END))
            );
            return new ParseTree(new Symbol(LexicalUnit.ELSE), children);
        } else {
            error("Expected END or ELSE");
            return null;
        }
    }

    private ParseTree whileStatement() throws IOException {
        if (currentToken.getType() == LexicalUnit.WHILE) {
            derivation.add(13); // Rule number [13]
            // Match WHILE
            match(LexicalUnit.WHILE);
            // Match {
            match(LexicalUnit.LBRACK);
            // Parse <Cond>
            ParseTree condTree = cond();
            // Match }
            match(LexicalUnit.RBRACK);
            // Match REPEAT
            match(LexicalUnit.REPEAT);
            // Parse <Code>
            ParseTree codeTree = code();
            // Match END
            match(LexicalUnit.END);
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.WHILE)),
                    new ParseTree(new Symbol(LexicalUnit.LBRACK)),
                    condTree,
                    new ParseTree(new Symbol(LexicalUnit.RBRACK)),
                    new ParseTree(new Symbol(LexicalUnit.REPEAT)),
                    codeTree,
                    new ParseTree(new Symbol(LexicalUnit.END))
            );
            return new ParseTree(new Symbol(LexicalUnit.WHILE), children);
        } else {
            error("Expected WHILE");
            return null;
        }
    }

    private ParseTree output() throws IOException {
        if (currentToken.getType() == LexicalUnit.OUTPUT) {
            derivation.add(14); // Rule number [14]
            // Match OUT
            match(LexicalUnit.OUTPUT);
            // Match (
            match(LexicalUnit.LPAREN);
            // Match [VarName]
            match(LexicalUnit.VARNAME);
            // Match )
            match(LexicalUnit.RPAREN);
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.OUTPUT)),
                    new ParseTree(new Symbol(LexicalUnit.LPAREN)),
                    new ParseTree(new Symbol(LexicalUnit.VARNAME)),
                    new ParseTree(new Symbol(LexicalUnit.RPAREN))
            );
            return new ParseTree(new Symbol(LexicalUnit.OUTPUT), children);
        } else {
            error("Expected OUT");
            return null;
        }
    }

    private ParseTree input() throws IOException {
        if (currentToken.getType() == LexicalUnit.INPUT) {
            derivation.add(15); // Rule number [15]
            // Match IN
            match(LexicalUnit.INPUT);
            // Match (
            match(LexicalUnit.LPAREN);
            // Match [VarName]
            match(LexicalUnit.VARNAME);
            // Match )
            match(LexicalUnit.RPAREN);
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.INPUT)),
                    new ParseTree(new Symbol(LexicalUnit.LPAREN)),
                    new ParseTree(new Symbol(LexicalUnit.VARNAME)),
                    new ParseTree(new Symbol(LexicalUnit.RPAREN))
            );
            return new ParseTree(new Symbol(LexicalUnit.INPUT), children);
        } else {
            error("Expected IN");
            return null;
        }
    }

    private ParseTree exprArith() throws IOException {
        if (currentToken.getType() == LexicalUnit.VARNAME || currentToken.getType() == LexicalUnit.NUMBER || currentToken.getType() == LexicalUnit.LPAREN) {
            derivation.add(16); // Rule number [16] - Could remove this rule!
            derivation.add(17); // Rule number [17]
            // Parse <Term>
            ParseTree termTree = term();
            // Parse <ExprArithTail>
            ParseTree exprArithTailTree = exprArithTail();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    termTree,
                    exprArithTailTree
            );
            return new ParseTree(new Symbol(LexicalUnit.VARNAME), children);
        } else {
            error("Expected VARNAME, NUMBER or (");
            return null;
        }
    }

    private ParseTree term() throws IOException {
        if (currentToken.getType() == LexicalUnit.VARNAME || currentToken.getType() == LexicalUnit.NUMBER || currentToken.getType() == LexicalUnit.LPAREN) {
            derivation.add(22); // Rule number [22]
            // Parse <Factor>
            ParseTree factorTree = factor();
            // Parse <TermTail>
            ParseTree termTailTree = termTail();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    factorTree,
                    termTailTree
            );
            return new ParseTree(new Symbol(LexicalUnit.VARNAME), children);
        } else {
            error("Expected VARNAME, NUMBER or (");
            return null;
        }
    }

    private ParseTree exprArithTail() throws IOException {
        if (currentToken.getType() == LexicalUnit.PLUS || currentToken.getType() == LexicalUnit.MINUS) {
            derivation.add(18); // Rule number [18]
            // Parse <PlusMinus>
            ParseTree plusMinusTree = plusMinus();
            // Parse <Term>
            ParseTree termTree = term();
            // Parse <ExprArithTail>
            ParseTree exprArithTailTree = exprArithTail();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    plusMinusTree,
                    termTree,
                    exprArithTailTree
            );
            return new ParseTree(new Symbol(LexicalUnit.PLUS), children);
        } else {
            derivation.add(19); // Rule number [19]
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.PLUS));
        }
    }

    private ParseTree plusMinus() throws IOException {
        if (currentToken.getType() == LexicalUnit.PLUS) {
            derivation.add(20); // Rule number [20]
            // Match +
            match(LexicalUnit.PLUS);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.PLUS));
        } else if (currentToken.getType() == LexicalUnit.MINUS) {
            derivation.add(21); // Rule number [21]
            // Match -
            match(LexicalUnit.MINUS);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.MINUS));
        } else {
            error("Expected + or -");
            return null;
        }
    }

    private ParseTree termTail() throws IOException {
        if (currentToken.getType() == LexicalUnit.TIMES || currentToken.getType() == LexicalUnit.DIVIDE) {
            derivation.add(23); // Rule number [23]
            // Parse <MulDiv>
            ParseTree mulDivTree = mulDiv();
            // Parse <Factor>
            ParseTree factorTree = factor();
            // Parse <TermTail>
            ParseTree termTailTree = termTail();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    mulDivTree,
                    factorTree,
                    termTailTree
            );
            return new ParseTree(new Symbol(LexicalUnit.TIMES), children);
        } else {
            derivation.add(24); // Rule number [24]
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.TIMES));
        }
    }

    private ParseTree mulDiv() throws IOException {
        if (currentToken.getType() == LexicalUnit.TIMES) {
            derivation.add(25); // Rule number [25]
            // Match *
            match(LexicalUnit.TIMES);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.TIMES));
        } else if (currentToken.getType() == LexicalUnit.DIVIDE) {
            derivation.add(26); // Rule number [26]
            // Match /
            match(LexicalUnit.DIVIDE);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.DIVIDE));
        } else {
            error("Expected * or /");
            return null;
        }
    }

    private ParseTree factor() throws IOException {
        if (currentToken.getType() == LexicalUnit.MINUS) {
            derivation.add(27); // Rule number [27]
            // Parse <Minus>
            return factor();
        } else if (currentToken.getType() == LexicalUnit.VARNAME) {
            derivation.add(28); // Rule number [28]
            // Match [VarName]
            match(LexicalUnit.VARNAME);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.VARNAME));
        } else if (currentToken.getType() == LexicalUnit.NUMBER) {
            derivation.add(29); // Rule number [29]
            // Match [Number]
            match(LexicalUnit.NUMBER);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.NUMBER));
        } else if (currentToken.getType() == LexicalUnit.LPAREN) {
            derivation.add(30); // Rule number [30]
            // Match (
            match(LexicalUnit.LPAREN);
            // Parse <ExprArith>
            ParseTree exprArithTree = exprArith();
            // Match )
            match(LexicalUnit.RPAREN);
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.LPAREN)),
                    exprArithTree,
                    new ParseTree(new Symbol(LexicalUnit.RPAREN))
            );
            return new ParseTree(new Symbol(LexicalUnit.LPAREN), children);
        } else {
            error("Expected VARNAME, NUMBER or (");
            return null;
        }
    }

    private ParseTree cond() throws IOException {
        if (currentToken.getType() == LexicalUnit.VARNAME || currentToken.getType() == LexicalUnit.NUMBER || currentToken.getType() == LexicalUnit.LPAREN) {
            derivation.add(31); // Rule number [31]
            // Parse <ExprArith>
            ParseTree exprArithTree1 = exprArith();
            // Parse <Comp>
            ParseTree compTree = comp();
            // Parse <ExprArith>
            ParseTree exprArithTree2 = exprArith();
            // Parse <CondTail>
            ParseTree condTailTree = condTail();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    exprArithTree1,
                    compTree,
                    exprArithTree2,
                    condTailTree
            );
            return new ParseTree(new Symbol(LexicalUnit.VARNAME), children);
        } else if (currentToken.getType() == LexicalUnit.PIPE) {
            derivation.add(32); // Rule number [32]
            // Match |
            match(LexicalUnit.PIPE);
            // Parse <CondTail>
            ParseTree condTailTree = condTail();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.PIPE)),
                    condTailTree
            );
            return new ParseTree(new Symbol(LexicalUnit.PIPE), children);
        } else {
            error("Expected VARNAME, NUMBER, ( or |");
            return null;
        }
    }

    private ParseTree comp() throws IOException {
        if (currentToken.getType() == LexicalUnit.EQUAL) {
            derivation.add(33); // Rule number [33]
            // Match ==
            match(LexicalUnit.EQUAL);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.EQUAL));
        } else if (currentToken.getType() == LexicalUnit.SMALEQ) {
            derivation.add(34); // Rule number [34]
            // Match <=
            match(LexicalUnit.SMALEQ);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.SMALEQ));
        } else if (currentToken.getType() == LexicalUnit.SMALLER) {
            derivation.add(35); // Rule number [35]
            // Match <
            match(LexicalUnit.SMALLER);
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.SMALLER));
        } else {
            error("Expected ==, <= or <");
            return null;
        }
    }

    private ParseTree condTail() throws IOException {
        if (currentToken.getType() == LexicalUnit.IMPLIES) {
            derivation.add(36); // Rule number [36]
            // Match ->
            match(LexicalUnit.IMPLIES);
            // Parse <Cond>
            ParseTree condTree = cond();
            // Build parse tree node
            List<ParseTree> children = Arrays.asList(
                    new ParseTree(new Symbol(LexicalUnit.IMPLIES)),
                    condTree
            );
            return new ParseTree(new Symbol(LexicalUnit.IMPLIES), children);
        } else {
            derivation.add(37); // Rule number [37]
            // Build parse tree node
            return new ParseTree(new Symbol(LexicalUnit.IMPLIES));
        }
    }
}
