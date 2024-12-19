import java.util.*;

/**
 * This class is responsible for generating the LLVM code from the parse tree.
 */
public class LLVMParser {

    private String programName = "";

    private final HashMap<String, Integer> variables = new HashMap<>();
    private HashSet<String> variableSet = new LinkedHashSet<>();

    private int ifCounter, whileCounter, arithCounter, condCounter, prodCounter;

    private final List<String> atomicQueue = new ArrayList<>();
    private final List<String> prodQueue = new ArrayList<>();
    private final List<String> conditionQueue = new ArrayList<>();
    private final List<String> endQueue = new ArrayList<>();

    /**
     * Generates a new line in the LLVM code.
     * @return String
     */
    private String line() {
        return line("");
    }

    /**
     * Generates a new line in the LLVM code with the given input.
     * @param input The input to generate the new line.
     * @return String
     */
    private String line(String input) {
        return input.concat("\n");
    }

    /**
     * Returns the current production variable.
     * @return String
     */
    private String getCurrentProdVar() {
        return "%prod" + prodCounter;
    }

    /**
     * Returns a new production variable.
     * @return String
     */
    private String getNewProdVar() {
        prodCounter++;
        return getCurrentProdVar();
    }

    /**
     * Returns the current arithmetic variable.
     * @return String
     */
    private String getCurrentArithVar() {
        return "%arith" + arithCounter;
    }

    /**
     * Returns a new arithmetic variable.
     * @return String
     */
    private String getNewArithVar() {
        arithCounter++;
        return getCurrentArithVar();
    }

    /**
     * Returns the current condition variable.
     * @return String
     */
    private String getCurrentCondVar() {
        return "%cond" + condCounter;
    }

    /**
     * Returns a new condition variable.
     * @return String
     */
    private String getNewCondVar() {
        condCounter++;
        return getCurrentCondVar();
    }

    /**
     * Updates the variable counter.
     * @param varName The variable name to update.
     */
    private void updateVariableCounter(String varName) {
        int index = variables.get(varName) + 1;
        variables.put(varName, index);
    }

    /**
     * Adds a new variable to the variables map.
     * @param filteredVariable The variable to add.
     */
    private void addVariable(String filteredVariable) {
        variables.put(filteredVariable, 1);
    }

    /**
     * Returns the current variable value with the given variable name in LLVM format.
     * @param varName The variable name to get the value from.
     * @return String
     */
    private String getCurrentVariableVal(String varName) {
        return "%".concat(varName) + "_val" + variables.get(varName);
    }

    /**
     * Returns the program end.
     * @return String
     */
    private String getProgramEnd() {
        return endQueue.removeFirst();
    }

    /**
     * Generates LLVM code from the provided parse tree.
     *
     * @param parseTree The parse tree to generate the LLVM code from.
     * @return An array containing the name of the program and the generated LLVM code.
     * @throws RuntimeException If an unknown non-terminal expression is found.
     */
    public String[] generate(ParseTree parseTree) throws RuntimeException {
        String result = look(parseTree);
        return new String[]{programName.concat(".ll"), result};
    }

    /**
     * Processes the given parse tree node and returns its corresponding LLVM code.
     * This method is recursive.
     *
     * @param node The parse tree node to process.
     * @return The LLVM code corresponding to the given parse tree node.
     * @throws RuntimeException If an unknown non-terminal expression is found.
     */
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

    /**
     * Assigns all variables in the given parse tree to initialize them
     * before generating the LLVM code.
     *
     * @param node The parse tree node to assign the variables from.
     */
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

    /**
     * Creates the LLVM code for logical implication between two boolean values.
     *
     * @return The LLVM code for logical implication.
     */
    private String logicalImplication() {
        return "define i1 @logical_implication(i1 %p, i1 %q) {\n" +
                "entry:\n" +
                "    %not_p = xor i1 %p, true\n" +
                "    %result = or i1 %not_p, %q\n" +
                "    ret i1 %result\n" +
                "}\n";
    }

    /**
     * Defines the LLVM code for reading an integer input.
     *
     * @return The LLVM code for reading an integer input.
     */
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

    /**
     * Defines the LLVM code for printing an integer output.
     *
     * @return The LLVM code for printing an integer output.
     */
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

    /**
     * Sets the program name to the given token.
     * @param token The token to set the program name to.
     * @return String indicating the start of the program.
     */
    private String setProgramName(String token) {
        programName = token;
        return "@main()";
    }

    /**
     * Generates the LLVM code for the Program non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Program non-terminal expression.
     */
    private String program(ParseTree node) {
        // Program -> LET [ProgName] BE <Code> END
        assignAllVariablesFirst(node);
        endQueue.addFirst("ret i32 0");
        String variablesAssignation = "";
        for (String var : variableSet) {
            addVariable(var);
            String allocation = line("%" + var + " = alloca i32, align 4");
            variablesAssignation = variablesAssignation.concat(allocation);
        }
        return line(logicalImplication()) +
                line(inputDefinition()) +
                line(outputDefinition()) +
                look(node.getChildren().get(0)) +
                look(node.getChildren().get(1)) +
                look(node.getChildren().get(2)) +
                line("entry:") +
                variablesAssignation +
                look(node.getChildren().get(3)) +
                look(node.getChildren().get(4)) +
                line() +
                line("}");
    }

    /**
     * Generates the LLVM code for the Code non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Code non-terminal expression.
     */
    private String code(ParseTree node) {
        // Code -> <Instruction> : <Code> | epsilon
        if (node.getChildren().size() == 3) {
            return updateVars() + line(look(node.getChildren().getFirst())) +
                    look(node.getChildren().get(2));
        }
        return "";
    }

    /**
     * Generates the LLVM code for the Instruction non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Instruction non-terminal expression.
     */
    private String instruction(ParseTree node) {
        // Instruction -> <Assign> | <If> | <While> | <Output> | <Input>
        return look(node.getChildren().getFirst());
    }

    /**
     * Generates the LLVM code for the Assign non-terminal expression.
     * @param varName The variable name to assign the value to.
     * @return The LLVM code for the Assign non-terminal expression.
     */
    private String varNameLoadCall(String varName) {
        return line("%" + varName + "_val" + variables.get(varName) + " = load i32, i32* %" + varName + ", align 4");
    }

    /**
     * Updates the variables references in the LLVM code.
     * @return The updated variables references in the LLVM code.
     */
    private String updateVars() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(line("; Updated variables references"));
        variables.forEach((value, index) -> {
            updateVariableCounter(value);
            stringBuilder.append(varNameLoadCall(value));
        });
        stringBuilder.append(line());
        return stringBuilder.toString();
    }

    /**
     * Generates the LLVM code for the Assign non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Assign non-terminal expression.
     */
    private String assign(ParseTree node) {
        // Assign -> [VarName] = <ExprArith>
        String varName = look(node.getChildren().get(0));
        String exprArith = look(node.getChildren().get(2));
        String currentArithVar = getCurrentArithVar();
        String filteredVariable = varName.substring(1);
        String reAssign = line("store i32 " + currentArithVar + ", i32* " + varName + ", align 4");
        updateVariableCounter(filteredVariable);
        return exprArith + reAssign + varNameLoadCall(filteredVariable);
    }

    /**
     * Generates the LLVM code for the ExprArith non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the ExprArith non-terminal expression.
     */
    private String exprArith(ParseTree node) {
        // <ExprArith> -> <Prod> <ExprArith'>
        String prod = look(node.getChildren().getFirst());
        prodQueue.addFirst(getCurrentProdVar());
        String exprArithPrime = look(node.getChildren().get(1));
        String currentArithVar = getCurrentArithVar();
        return prod + exprArithPrime + line(getNewArithVar() + " = add i32 0, " + (exprArithPrime.isEmpty() ? prodQueue.removeFirst() : currentArithVar));
    }

    /**
     * Generates the LLVM code for the ExprArith' non-terminal expression.
     *
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the ExprArith' non-terminal expression.
     */
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

    /**
     * Generates the LLVM code for the Prod non-terminal expression.
     *
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Prod non-terminal expression.
     */
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

    /**
     * Generates the LLVM code for the Prod' non-terminal expression in a recursive manner.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Prod' non-terminal expression.
     */
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

    /**
     * Checks if the given parse tree node is an arithmetic atom.
     * @param atom The parse tree node to check.
     * @return True if the given parse tree node is an arithmetic atom, false otherwise.
     */
    private boolean isArithmeticAtom(ParseTree atom) {
        return atom.getChildren().size() == 3;
    }

    /**
     * Returns the value of the given arithmetic atom.
     * @param node The parse tree node to get the value from.
     * @return The value of the given arithmetic atom.
     */
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

    /**
     * Generates the LLVM code for the If non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the If non-terminal expression.
     */
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
        return condition +
                line("br i1 " + conditionVar + ", label %" + ifBlockLabel + elseLabel) +
                line(ifBlockLabel + ":") +
                code +
                line("br label %end" + ifCounter) +
                ifTail;
    }

    /**
     * Generates the LLVM code for the IfTail non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the IfTail non-terminal expression.
     */
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

    /**
     * Generates the LLVM code for the Cond non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Cond non-terminal expression.
     */
    private String cond(ParseTree node) {
        // Cond → <SimpleCond> <Cond’>
        String updatedVars = updateVars();
        String simpleCond = look(node.getChildren().getFirst());
        String condPrime = look(node.getChildren().get(1));
        return updatedVars + simpleCond + condPrime;
    }

    /**
     * Generates the LLVM code for the Cond' non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Cond' non-terminal expression.
     */
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

    /**
     * Generates the LLVM code for the SimpleCond non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the SimpleCond non-terminal expression.
     */
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

    /**
     * Returns the current condition variable.
     * @param node The parse tree node to get the current condition variable from.
     * @return The current condition variable.
     */
    private String comp(ParseTree node) {
        // <Comp> -> == | <= | <
        return look(node.getChildren().getFirst());
    }

    /**
     * While non-terminal expression. We use "while" as "whilE" to avoid conflict with the reserved word.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the While non-terminal expression.
     */
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

    /**
     * Generates the LLVM code for the Output non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Output non-terminal expression.
     */
    private String output(ParseTree node) {
        // Output -> OUT([VarName])
        String updatedVars = updateVars();
        String varName = look(node.getChildren().get(2));
        return updatedVars + line("call void @println(i32 " + getCurrentVariableVal(varName.substring(1)) + ")");
    }

    /**
     * Generates the LLVM code for the Input non-terminal expression.
     * @param node The parse tree node to generate the LLVM code from.
     * @return The LLVM code for the Input non-terminal expression.
     */
    private String input(ParseTree node) {
        // <Input> → IN ( [VarName] )
        String varName = look(node.getChildren().get(2));
        String tempInputVar = varName + "_input";
        String filteredVariable = varName.substring(1);
        updateVariableCounter(filteredVariable);
        return line(tempInputVar + " = call i32 @readInt()") +
                line("store i32 " + tempInputVar + ", i32* " + varName + ", align 4") +
                varNameLoadCall(filteredVariable);
    }
}
