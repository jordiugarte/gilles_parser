import java.util.*;

public class LLVMParser {

    private final Map<String, String> variables = new HashMap<>();

    private int ifCounter, whileCounter, tempCounter;

    private String line() {
        return line("");
    }

    private String line(String input) {
        return input.concat("\n");
    }

    private String variableRegistration(String var) {
        // Returns the variable name or its 'val' name if it was already registered
        if (variables.containsKey(var)) {
            return variables.get(var);
        } else {
            variables.put(var, var.concat("_val"));
            return var;
        }
    }

    private String getCurrentTempVar() {
        return "%tmp" + tempCounter;
    }

    private String getNewTempVar() {
        tempCounter++;
        return getCurrentTempVar();
    }

    public String generate(ParseTree parseTree) {
        String result = look(parseTree);
        return result;
    }

    public String look(ParseTree node) {
        String expression = node.getLabel().toString();
        if (expression.startsWith("Non-terminal symbol: ")) {
            String nonTerminal = expression.split("Non-terminal symbol: ")[1];
            return switch (nonTerminal) {
                case "Program" -> program(node);
                case "Code" -> code(node);
                case "Instruction" -> instruction(node);
                case "Assign" -> assign(node);
                case "ExprArith" -> exprArith(node);
                case "ExprArith'" -> exprArithPrime(node);
                case "Prod" -> prod(node);
                case "Prod'" -> prodPrime(node);
                case "Atom" -> atom(node);
                case "Input" -> input(node);
                case "Output" -> output(node);
                case "If" -> iF(node);
                case "Cond" -> cond(node);
                case "Cond'" -> condPrime(node);
                case "Comp" -> comp(node);
                case "SimpleCond" -> simpleCond(node);
                case "IfTail" -> ifTail(node);
                case "While" -> whilE(node);
                default -> throw new RuntimeException("Unknown Non-terminal Expression: " + nonTerminal);
            };
        } else {
            // Terminal symbols
            String terminal = expression.split("lexical unit: ")[1];
            String token = expression.split("lexical unit: ")[0].split("token: ")[1].replaceAll("\\s", "");
            return switch (terminal) {
                case "[ProgName]" -> "@main()";
                case "[VarName]" -> "%" + variableRegistration(token);
                case "[Number]" -> token;
                case "LET" -> "define i32 ";
                case "BE" -> line(" {");
                case "END" -> line("ret i32 0") + line("}");
                case "COLUMN" -> line();
                case "OUT" -> "ret ";
                case "+" -> "add";
                case "-" -> "sub";
                case "*" -> "mul";
                case "/" -> "sdiv";
                case "=" -> " = ";
                case "==" -> " icmp ";
                case "<=" -> " icmp sle ";
                case "<" -> " icmp slt ";
                case "->" -> " and ";
                default -> "";
            };
        }
    }

    private String inputDefinition() {
        return "@.strR = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1\n" +
                "define i32 @readInt() #0 {\n" +
                "  %1 = alloca i32, align 4\n" +
                "  %2 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %1)\n" +
                "  %3 = load i32, i32* %1, align 4\n" +
                "  ret i32 %3\n" +
                "}\n" +
                "\n" +
                "declare i32 @scanf(i8*, ...) #1\n";
    }

    private String outputDefinition() {
        return "@.strP = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\", align 1\n" +
                "define void @println(i32 %x) #0 {\n" +
                "  %1 = alloca i32, align 4\n" +
                "  store i32 %x, i32* %1, align 4\n" +
                "  %2 = load i32, i32* %1, align 4\n" +
                "  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)\n" +
                "  ret void\n" +
                "}\n" +
                "\n" +
                "declare i32 @printf(i8*, ...) #1\n";
    }

    private String program(ParseTree node) {
        // Program -> LET [ProgName] BE <Code> END
        return line(inputDefinition()) +
                line(outputDefinition()) +
                look(node.getChildren().get(0)) +
                look(node.getChildren().get(1)) +
                look(node.getChildren().get(2)) +
                line("entry:") +
                line(look(node.getChildren().get(3))) +
                look(node.getChildren().get(4));
    }

    private String code(ParseTree node) {
        // Code -> <Instruction> : <Code> | epsilon
        if (node.getChildren().size() > 1) {
            return line(look(node.getChildren().getFirst())) +
                    line(look(node.getChildren().get(2)));
        }
        return "";
    }

    private String instruction(ParseTree node) {
        // Instruction -> <Assign> | <If> | <While> | <Output> | <Input>
        return look(node.getChildren().getFirst());
    }

    private String assign(ParseTree node) {
        // Assign -> [VarName] = <ExprArith>
        String varName = look(node.getChildren().get(0));
        String exprArith = look(node.getChildren().get(2));

        String varLLVM = variableRegistration(varName);
        return line(varLLVM + " = alloca i32, align 4") +
                exprArith +
                line("store i32 " + getCurrentTempVar() + ", i32* " + varLLVM + ", align 4") +
                line(variableRegistration(varLLVM) + " = load i32, i32* " + varLLVM + ", align 4 ");
    }

    private String exprArith(ParseTree node) {
        // <ExprArith> -> <Prod> <ExprArith'>
        String firstAtom = getFirst(node.getChildren().getFirst());
        String exprArithPrimeFirst = getFirst(node.getChildren().get(1));
        String operator = "";
        String prod = look(node.getChildren().getFirst());
        if (exprArithPrimeFirst.equals("add") || exprArithPrimeFirst.equals("sub")) {
            operator = exprArithPrimeFirst;
        }
        if (operator.isEmpty()) {
            return prod;
        } else {
            return getNewTempVar() + " = " + operationLine(operator, firstAtom, look(node.getChildren().get(1)));
        }
    }

    private String exprArithPrime(ParseTree node) {
        // <ExprArith'> -> + <Prod> <ExprArith'> | - <Prod> <ExprArith'> | ε
        if (node.getChildren().size() == 3) {
            String exprArithPrime = look(node.getChildren().get(2));
            return exprArithPrime + line(look(node.getChildren().get(1)));
        }
        return "";
    }

    private String prod(ParseTree node) {
        // Prod -> <Atom> <Prod'>
        String prodPrimeFirst = getFirst(node.getChildren().get(1));
        String prodPrime = look(node.getChildren().get(1));
        String operator = "";
        if (prodPrimeFirst.equals("mul") || prodPrimeFirst.equals("sdiv")) {
            operator = prodPrimeFirst;
        }
        String atom = look(node.getChildren().getFirst());
        if (operator.isEmpty()) {
            return atom;
        } else {
            return operationLine(operator, atom, prodPrime);
        }
    }

    private String prodPrime(ParseTree node) {
        // Prod' -> * <Atom> <Prod'> | / <Atom> <Prod'> | ε
        if (node.getChildren().size() == 3) {
            return look(node.getChildren().get(1));
        }
        return "";
    }

    private String atom(ParseTree node) {
        // Atom -> [Number] | [VarName] | ( <ExprArith> ) | - <Atom>
        if (node.getChildren().size() == 1) {
            return look(node.getChildren().getFirst());
        } else if (node.getChildren().size() == 3) {
            return look(node.getChildren().get(1));
        } else {
            return look(node.getChildren().get(1));
        }
    }


    private String iF(ParseTree node) {
        // If -> IF { <Cond> } THEN <Code> <IfTail>
        ifCounter++;
        String cond = "cond" + ifCounter;
        String then = "then" + ifCounter;
        String elsE = "else" + ifCounter;
        String ifTail = look(node.getChildren().get(5));
        return line("%" + cond + " = " + look(node.getChildren().get(2))) +
                line("br i1 %" + cond + ", label %" + then + (ifTail.isEmpty() ? "" : ", label %" + elsE)) +
                line(then + ":") +
                line(ifTail) +
                line(look(node.getChildren().get(6)));
    }

    private String ifTail(ParseTree node) {
        // If -> END | ELSE <Code> END
        String elsE = "else" + ifCounter;
        if (node.getChildren().size() > 1) {
            return line(elsE + ":") +
                    line(look(node.getChildren().get(1)));
        }
        return "";
    }

    private String cond(ParseTree node) {
        // Cond → <SimpleCond> <Cond’>
        return line(look(node.getChildren().getFirst()) + look(node.getChildren().get(1)));
    }

    private String condPrime(ParseTree node) {
        // <Cond’> → -> <Cond> | epsilon
        if (node.getChildren().size() == 2) {
            return look(node.getChildren().getFirst()) + look(node.getChildren().get(1));
        }
        return "";
    }

    private String simpleCond(ParseTree node) {
        // <SimpleCond> -> \| <Cond> \| | <ExprArith> <Comp> <ExprArith>
//TODO
//        %i_val = load i32, i32* %i
//        %cmp = icmp slt i32 %i_val, 10
//        br i1 %cmp, label %body, label %end

        if (node.getChildren().size() == 1) {
            return look(node.getChildren().getFirst());
        } else {
            return look(node.getChildren().get(1)) +
                    look(node.getChildren().getFirst()) +
                    look(node.getChildren().get(2));
        }
    }

    private String comp(ParseTree node) {
        return look(node.getChildren().getFirst());
    }

    private String whilE(ParseTree node) {
        //  <While> → WHILE {<Cond>} REPEAT <Code> END
        whileCounter++;
        return line("while_cond" + whileCounter + ":") +
                look(node.getChildren().get(2)) +
                line("while_body" + whileCounter + ":") +
                look(node.getChildren().get(5));
    }

    private String output(ParseTree node) {
        // Output -> OUT([VarName])
        String var = variableRegistration(look(node.getChildren().get(2)));
        return line("call void @println(i32 " + var + ")");
    }

    private String input(ParseTree node) {
        // <Input> → IN ( [VarName] )
        String var = look(node.getChildren().get(2));
        variableRegistration(var);
        return line(variableRegistration(var) + " = call i32 @readInt()");
    }

    private String getFirst(ParseTree node) {
        return look(node.getChildren().getFirst());
    }

    private String operationLine(String operator, String atom1, String atom2) {
        return line(operator + " i32 " + atom1 + ", " + atom2);
    }
}
