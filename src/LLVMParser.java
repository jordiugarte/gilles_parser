import java.util.*;

public class LLVMParser {

    private String programName = "";

    private final Map<String, String> variables = new HashMap<>();

    private int ifCounter, whileCounter, tempCounter, condCounter, prodCounter;

    private String line() {
        return line("");
    }

    private String line(String input) {
        return input.concat("\n");
    }

    private List<String> atomicQueue = new ArrayList<>();
    private List<String> prodQueue = new ArrayList<>();

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

    private String getPreviousTempVar() {
        return "%tmp" + (tempCounter - 1);
    }

    private String getCurrentCondVar() {
        return "%cond" + condCounter;
    }

    private String getNewCondVar() {
        condCounter++;
        return getCurrentCondVar();
    }

    private String getNewProd() {
        prodCounter++;
        return getCurrentProd();
    }

    private String getCurrentProd() {
        return "%prod" + prodCounter;
    }

    public String[] generate(ParseTree parseTree) throws RuntimeException {
        String result = look(parseTree);
        return new String[]{programName.concat(".ll"), result};
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
                case "[ProgName]" -> setProgramName(token);
                case "[VarName]" -> "%" + variableRegistration(token);
                case "[Number]" -> token;
                case "LET" -> "define i32 ";
                case "BE" -> line(" {");
                case "END" -> "ret i32 0";
                case "COLUMN" -> line();
                case "OUT" -> "ret ";
                case "+" -> "add";
                case "-" -> "sub";
                case "*" -> "mul";
                case "/" -> "sdiv";
                case "=" -> " = ";
                case "==" -> "icmp eq";
                case "<=" -> " icmp sle ";
                case "<" -> " icmp slt ";
                case "->" -> "and ";
                case "|" -> "|";
                case "ELSE" -> "ELSE";
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

    private String setProgramName(String token) {
        programName = token;
        return "@main()";
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
                line(look(node.getChildren().get(4))) +
                line("}");
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
                (isNumericAtomic(exprArith) ? "" : exprArith) +
                line("store i32 " + (isNumericAtomic(exprArith) ? exprArith : getCurrentTempVar()) + ", i32* " + varLLVM + ", align 4") +
                line(variableRegistration(varLLVM) + " = load i32, i32* " + varLLVM + ", align 4 ");
    }

    private boolean isNumericAtomic(String value) {
        return value.matches("-?\\d+");
    }

    private String exprArith(ParseTree node) {
        // <ExprArith> -> <Prod> <ExprArith'>
        String prod = look(node.getChildren().getFirst());
        String exprArithPrime = look(node.getChildren().get(1));
        return prod + exprArithPrime;
    }

    private String exprArithPrime(ParseTree node) {
        // <ExprArith'> -> + <Prod> <ExprArith'> | - <Prod> <ExprArith'> | ε
        if (node.getChildren().size() == 3) {
//            String prodBackground = look(node.getChildren().getFirst());
//            String exprArithPrimeBackground = look(node.getChildren().get(1));
//            if (exprArithPrimeBackground.isEmpty()) {
//                return prodBackground;
//            } else {
//                String operator = getFirst(node.getChildren().get(1));
//                if (isNumericAtomic(prodBackground)) {
//                    return exprArithPrimeBackground +
//                            line(getNewTempVar() + " = " + operator + " i32 " + prodBackground + ", " + getCurrentTempVar());
//                } else {
//                    return prodBackground +
//                            exprArithPrimeBackground +
//                            line(getNewTempVar() + " = " + operator + " i32 " + prodBackground + ", " + getCurrentTempVar());
//                }
//            }
        }
        return "";
    }

    private String prod(ParseTree node) {
        // Prod -> <Atom> <Prod'>
        String atom = look(node.getChildren().getFirst());
        atomicQueue.add(atom);
        return look(node.getChildren().get(1));
    }

    // Recursive
    private String prodPrime(ParseTree node) {
        // Prod' -> * <Atom> <Prod'> | / <Atom> <Prod'> | ε
        if (node.getChildren().size() == 3) {
            String atom = look(node.getChildren().get(1));
            String operator = look(node.getChildren().getFirst());
            atomicQueue.add(atom);
            if (atomicQueue.size() == 2) {
                // Check if atomic queue has at least a pair ov atoms
                String leftAtom = atomicQueue.removeFirst();
                String rightAtom = atomicQueue.removeFirst();
                return line(getNewProd() + " = " + operator + " i32 " + leftAtom + ", " + rightAtom) +
                        prodPrime(node.getChildren().get(2));
            } else if (atomicQueue.size() == 1) {
                // Check if atomic queue has at least one atom
                String currentProd = getCurrentProd();
                return line(getNewProd() + " = " + operator + " i32 " + currentProd + ", " + atom) +
                        prodPrime(node.getChildren().get(2));
            } else {
                return "";
            }
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
            return "-" + look(node.getChildren().get(1));
        }
    }


    private String iF(ParseTree node) {
        // If -> IF { <Cond> } THEN <Code> <IfTail>
        ifCounter++;
        String ifBlockLabel = "if_block" + ifCounter;
        String elseBlock = "else_block" + ifCounter;
        String condition = look(node.getChildren().get(2));
        String code = look(node.getChildren().get(5));
        String ifTail = look(node.getChildren().get(6));

        String elseLabel = "";
        if (getFirst(node.getChildren().get(6)).equals("ELSE")) {
            elseLabel = ", label %" + elseBlock;
        }
        return condition +
                line("br i1 " + getCurrentCondVar() + ", label %" + ifBlockLabel + elseLabel) +
                line(ifBlockLabel + ":") +
                code +
                line("br label %end" + ifCounter) +
                ifTail;
    }

    private String ifTail(ParseTree node) {
        // If -> END | ELSE <Code> END
        if (node.getChildren().size() == 3) {
            String elseLabel = line("else_block" + ifCounter + ":");
            String code = look(node.getChildren().get(1));
            String codeEnd = "end" + ifCounter + ":\n" + look(node.getChildren().get(2));
            return elseLabel + code + line("br label %end" + ifCounter) + codeEnd;
        } else {
            return line("end" + ifCounter + ":");
        }
    }

    private String cond(ParseTree node) {
        // Cond → <SimpleCond> <Cond’>
        String condPrimeFirst = getFirst(node.getChildren().get(1));
        if (condPrimeFirst.equals("and ")) {
            return condPrimeFirst + look(node.getChildren().get(1));
        } else {
            return line(look(node.getChildren().getFirst()));
        }
    }

    private String condPrime(ParseTree node) {
        // <Cond’> → -> <Cond> | epsilon
        if (node.getChildren().size() == 2) {
            return look(node.getChildren().get(1));
        }
        return "";
    }

    private String simpleCond(ParseTree node) {
        // <SimpleCond> -> \| <Cond> \| | <ExprArith> <Comp> <ExprArith>
        if (look(node.getChildren().getFirst()).equals("|") && look(node.getChildren().get(2)).equals("|")) {
            //  Return operator
            return look(node.getChildren().get(1));
        } else {
            //  Return comp
            String comp = look(node.getChildren().get(1));
            return line(getNewCondVar() + " = " + comp + " i32 " + look(node.getChildren().getFirst()) + ", " + look(node.getChildren().get(2)));
        }
    }

    private String comp(ParseTree node) {
        // <Comp> -> == | <= | <
        return look(node.getChildren().getFirst());
    }

    private String whilE(ParseTree node) {
        //  <While> → WHILE {<Cond>} REPEAT <Code> END
        whileCounter++;
        String whileCondLabel = "while_cond" + whileCounter;
        String whileBlockLabel = "while_block" + whileCounter;
        String condition = look(node.getChildren().get(2));
        String code = look(node.getChildren().get(5));
        String endLabel = "while_end" + whileCounter;

        return line("br label %" + whileCondLabel) +
                line(whileCondLabel + ":") +
                condition +
                line("br i1 " + getCurrentCondVar() + ", label %" + whileBlockLabel + ", label %" + endLabel) +
                line(whileBlockLabel + ":") +
                code +
                line("br label %" + whileCondLabel) +
                line(endLabel + whileCounter + ":") +
                line(look(node.getChildren().get(6)));
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
        return getFurther(node, 0);
    }

    private String getFurther(ParseTree node, int k) {
        return look(node.getChildren().get(k));
    }
}
