import java.util.*;

public class LLVMParser {

    private String programName = "";

    private final HashMap<String, Integer> variables = new HashMap<>();
    private HashSet<String> variableSet = new LinkedHashSet<>();

    private int ifCounter, whileCounter, arithCounter, condCounter, prodCounter;

    private final List<String> atomicQueue = new ArrayList<>();
    private final List<String> prodQueue = new ArrayList<>();
    private final List<String> conditionQueue = new ArrayList<>();
    private final List<String> endQueue = new ArrayList<>();


    private String line() {
        return line("");
    }

    private String line(String input) {
        return input.concat("\n");
    }

    private String getCurrentProdVar() {
        return "%prod" + prodCounter;
    }

    private String getNewProdVar() {
        prodCounter++;
        return getCurrentProdVar();
    }

    private String getCurrentArithVar() {
        return "%arith" + arithCounter;
    }

    private String getNewArithVar() {
        arithCounter++;
        return getCurrentArithVar();
    }

    private String getCurrentCondVar() {
        return "%cond" + condCounter;
    }

    private String getNewCondVar() {
        condCounter++;
        return getCurrentCondVar();
    }

    private void updateVariableCounter(String varName) {
        int index = variables.get(varName) + 1;
        variables.put(varName, index);
    }

    private void addVariable(String filteredVariable) {
        variables.put(filteredVariable, 1);
    }

    private String getCurrentVariableVal(String varName) {
        return "%".concat(varName) + "_val" + variables.get(varName);
    }

    private String getProgramEnd() {
        return endQueue.removeFirst();
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
                case "[VarName]" -> "%".concat(token);
                case "[Number]" -> token;
                case "LET" -> "define i32 ";
                case "BE" -> line(" {");
                case "END" -> getProgramEnd();
                case "COLUMN" -> line();
                case "OUT" -> "ret ";
                case "+" -> "add";
                case "-" -> "sub";
                case "*" -> "mul";
                case "/" -> "sdiv";
                case "=" -> " = ";
                case "==" -> "icmp eq";
                case "<=" -> "icmp sle";
                case "<" -> "icmp slt";
                case "->" -> "and";
                case "|" -> "|";
                case "ELSE" -> "ELSE";
                default -> "";
            };
        }
    }
    public void assignAllVariablesFirst(ParseTree node) {
        if (node.getChildren().isEmpty()) {
            String expression = node.getLabel().toString();
            String terminal = expression.split("lexical unit: ")[1];
            String token = expression.split("lexical unit: ")[0].split("token: ")[1].replaceAll("\\s", "");
            if (terminal.equals("[VarName]")) {
                variableSet.add(token);
            }
        }
        for (ParseTree leaf : node.getChildren()) {
            assignAllVariablesFirst(leaf);
        }
    }

    private String logicalImplication() {
        return "define i1 @logical_implication(i1 %p, i1 %q) {\n" +
                "entry:\n" +
                "    %not_p = xor i1 %p, true\n" +
                "    %result = or i1 %not_p, %q\n" +
                "    ret i1 %result\n" +
                "}\n";
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
        assignAllVariablesFirst(node);
        endQueue.addFirst("ret i32 0");
        return line(logicalImplication()) +
                line(inputDefinition()) +
                line(outputDefinition()) +
                look(node.getChildren().get(0)) +
                look(node.getChildren().get(1)) +
                look(node.getChildren().get(2)) +
                line("entry:") +

                look(node.getChildren().get(3)) +
                look(node.getChildren().get(4)) +
                line() +
                line("}");
    }

    private String code(ParseTree node) {
        // Code -> <Instruction> : <Code> | epsilon
        if (node.getChildren().size() == 3) {
            return updateVars() + line(look(node.getChildren().getFirst())) +
                    look(node.getChildren().get(2));
        }
        return "";
    }

    private String instruction(ParseTree node) {
        // Instruction -> <Assign> | <If> | <While> | <Output> | <Input>
        return look(node.getChildren().getFirst());
    }

    private String varNameLoadCall(String varName) {
        return line("%" + varName + "_val" + variables.get(varName) + " = load i32, i32* %" + varName + ", align 4");
    }

    private String updateVars() {
        StringBuilder stringBuilder = new StringBuilder();
        variables.forEach((value, index) -> {
            updateVariableCounter(value);
            stringBuilder.append(varNameLoadCall(value));
        });
        return stringBuilder.toString();
    }

    private String assign(ParseTree node) {
        // Assign -> [VarName] = <ExprArith>
        String varName = look(node.getChildren().get(0));
        String exprArith = look(node.getChildren().get(2));
        String currentArithVar = getCurrentArithVar();
        String filteredVariable = varName.substring(1);
        if (!variables.containsKey(filteredVariable)) {
            addVariable(filteredVariable);
            String allocation = line(varName + " = alloca i32, align 4");
            String assign = line("store i32 " + currentArithVar + ", i32* " + varName + ", align 4");
            return exprArith + allocation + assign + varNameLoadCall(filteredVariable);
        } else {
            String reAssign = line("store i32 " + currentArithVar + ", i32* " + varName + ", align 4");
            updateVariableCounter(filteredVariable);
            return exprArith + reAssign + varNameLoadCall(filteredVariable);
        }
    }

    private String exprArith(ParseTree node) {
        // <ExprArith> -> <Prod> <ExprArith'>
        String prod = look(node.getChildren().getFirst());
        prodQueue.addFirst(getCurrentProdVar());
        String exprArithPrime = look(node.getChildren().get(1));
        String currentArithVar = getCurrentArithVar();
        return prod + exprArithPrime + line(getNewArithVar() + " = add i32 0, " + (exprArithPrime.isEmpty() ? prodQueue.removeFirst() : currentArithVar));
    }

    private String exprArithPrime(ParseTree node) {
        // <ExprArith'> -> + <Prod> <ExprArith'> | - <Prod> <ExprArith'> | ε
        if (node.getChildren().size() == 3) {
            String operator = look(node.getChildren().getFirst());
            String prod = look(node.getChildren().get(1));
            prodQueue.addFirst(getCurrentProdVar());
            if (prodQueue.size() == 2) {
                // Check if atomic queue has at least a pair ov atoms
                String rightAtom = prodQueue.removeFirst();
                String leftAtom = prodQueue.removeFirst();
                return prod + line(getNewArithVar() + " = " + operator + " i32 " + leftAtom + ", " + rightAtom) +
                        exprArithPrime(node.getChildren().get(2));
            } else if (prodQueue.size() == 1) {
                // Check if atomic queue has at least one atom
                String currentExprArithVar = getCurrentArithVar();
                return prod + line(getNewArithVar() + " = " + operator + " i32 " + currentExprArithVar + ", " + prodQueue.removeFirst()) +
                        exprArithPrime(node.getChildren().get(2));
            } else {
                return "";
            }
        }
        return "";
    }

    private String prod(ParseTree node) {
        // Prod -> <Atom> <Prod'>
        String possibleAtomicExprArith = "";
        String atomReference = "";
        if (isArithmeticAtom(node.getChildren().getFirst())) {
            possibleAtomicExprArith = look(node.getChildren().getFirst().getChildren().get(1));
            atomReference = getCurrentArithVar();
        } else {
            atomReference = look(node.getChildren().getFirst());
        }
        atomicQueue.add(atomReference);
        String prod = look(node.getChildren().get(1));
        String currentProdVar = getCurrentProdVar();
        return possibleAtomicExprArith + prod + line(getNewProdVar() + " = add i32 0, " + (prod.isEmpty() ? atomicQueue.removeFirst() : currentProdVar));
    }

    // Recursive
    private String prodPrime(ParseTree node) {
        // Prod' -> * <Atom> <Prod'> | / <Atom> <Prod'> | ε
        if (node.getChildren().size() == 3) {
            String possibleAtomicExprArith = "";
            String atomReference = "";
            if (isArithmeticAtom(node.getChildren().get(1))) {
                possibleAtomicExprArith = look(node.getChildren().get(1).getChildren().get(1));
                atomReference = getCurrentArithVar();
            } else {
                atomReference = look(node.getChildren().get(1));
            }
            String operator = look(node.getChildren().getFirst());
            atomicQueue.add(atomReference);
            if (atomicQueue.size() == 2) {
                // Check if atomic queue has at least a pair ov atoms
                String leftAtom = atomicQueue.removeFirst();
                String rightAtom = atomicQueue.removeFirst();
                return possibleAtomicExprArith + line(getNewProdVar() + " = " + operator + " i32 " + leftAtom + ", " + rightAtom) +
                        prodPrime(node.getChildren().get(2));
            } else if (atomicQueue.size() == 1) {
                // Check if atomic queue has at least one atom
                String currentProd = getCurrentProdVar();
                return possibleAtomicExprArith + line(getNewProdVar() + " = " + operator + " i32 " + currentProd + ", " + atomicQueue.removeFirst()) +
                        prodPrime(node.getChildren().get(2));
            } else {
                return "";
            }
        }
        return "";
    }

    private boolean isArithmeticAtom(ParseTree atom) {
        return atom.getChildren().size() == 3;
    }

    private String atom(ParseTree node) {
        // Atom -> [Number] | [VarName] | ( <ExprArith> ) | - <Atom>
        if (node.getChildren().size() == 1) {
            String atom = look(node.getChildren().getFirst());
            if (atom.startsWith("%")) {
                return getCurrentVariableVal(atom.substring(1));
            } else {
                return atom;
            }
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
        String conditionVar = getCurrentCondVar();
        String code = look(node.getChildren().get(5));
        String ifTail = look(node.getChildren().get(6));
        String elseLabel = ", label %" + elseBlock;
        return updateVars() + condition +
                line("br i1 " + conditionVar + ", label %" + ifBlockLabel + elseLabel) +
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
            String codeEnd = "end" + ifCounter;
            endQueue.addFirst(codeEnd);
            return elseLabel + code + line("br label %end" + ifCounter) +
                    look(node.getChildren().get(2)) + ":";
        } else {
            String codeEnd = "end" + ifCounter;
            endQueue.addFirst(codeEnd);
            return line("else_block" + ifCounter + ":") +
                    line("br label %" + codeEnd) +
                    look(node.getChildren().getFirst()) + ":";
        }
    }

    private String cond(ParseTree node) {
        // Cond → <SimpleCond> <Cond’>
        String simpleCond = look(node.getChildren().getFirst());
        String condPrime = look(node.getChildren().get(1));
        return simpleCond + condPrime;
    }

    private String condPrime(ParseTree node) {
        // <Cond’> → -> <Cond> | epsilon
        if (node.getChildren().size() == 2) {
            String cond = look(node.getChildren().get(1));
            if (!conditionQueue.isEmpty()) {
                if (conditionQueue.size() > 1) {
                    String left = conditionQueue.removeFirst();
                    String right = conditionQueue.removeFirst();
                    return cond + line(getNewCondVar() + " = call i1 @logical_implication(i1" + left + ", i1" + right + ")");
                } else {
                    String currentCondVar = getCurrentCondVar();
                    String right = conditionQueue.removeFirst();
                    return cond + line(getNewCondVar() + " = call i1 @logical_implication(i1" + currentCondVar + ", i1" + right + ")");
                }
                // Check if atomic queue has at least one atom
            } else {
                return "";
            }
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
            String leftExprArithm = look(node.getChildren().getFirst());
            String leftExprArithmReference = getCurrentArithVar();
            String comp = look(node.getChildren().get(1));
            String rightExprArithm = look(node.getChildren().get(2));
            String rightExprArithmReference = getCurrentArithVar();
            String newCondVar = getNewCondVar();
            conditionQueue.add(newCondVar);
            return leftExprArithm + rightExprArithm +
                    line(newCondVar + " = " + comp + " i32 " +
                            leftExprArithmReference + ", " + rightExprArithmReference);
        }
    }

    private String comp(ParseTree node) {
        // <Comp> -> == | <= | <
        return look(node.getChildren().getFirst());
    }

    private String whilE(ParseTree node) {
        //  <While> → WHILE { <Cond> } REPEAT <Code> END
        whileCounter++;
        String whileCondLabel = "while_cond" + whileCounter;
        String whileBlockLabel = "while_block" + whileCounter;
        String endLabel = "while_end" + whileCounter;
        endQueue.addFirst(endLabel);
        String condition = look(node.getChildren().get(2));
        String currentCondVar = getCurrentCondVar();
        String code = look(node.getChildren().get(5));

        return line("br label %" + whileCondLabel) +
                line() +
                line(whileCondLabel + ":") +
                condition +
                line("br i1 " + currentCondVar + ", label %" + whileBlockLabel + ", label %" + endLabel) +
                line() +
                line(whileBlockLabel + ":") +
                code +
                line("br label %" + whileCondLabel) +
                line() +
                line(look(node.getChildren().get(6)) + ":");
    }

    private String output(ParseTree node) {
        // Output -> OUT([VarName])
        String varName = look(node.getChildren().get(2));
        return line("call void @println(i32 " + getCurrentVariableVal(varName.substring(1)) + ")");
    }

    private String input(ParseTree node) {
        // <Input> → IN ( [VarName] )
        String varName = look(node.getChildren().get(2));
        String tempInputVar = varName + "_input";
        String filteredVariable = varName.substring(1);
        variables.put(filteredVariable, 1);
        return line(tempInputVar + " = call i32 @readInt()") +
                line(varName + " = alloca i32, align 4") +
                line("store i32 " + tempInputVar + ", i32* " + varName + ", align 4") +
                varNameLoadCall(filteredVariable);
    }
}
