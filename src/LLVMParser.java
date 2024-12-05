import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLVMParser {

    private final Map<String, String> variables = new HashMap<>();

    private int inputCounter;
    private int labelCount;

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

    public String generate(ParseTree node) {
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
                case "While" -> whilE(node);
                default -> throw new RuntimeException("Unknown Non-terminal Expression");
            };
        } else {
            // Terminal symbols
            String terminal = expression.split("lexical unit: ")[1];
            String token = expression.split("lexical unit: ")[0].split("token: ")[1].replaceAll("\\s", "");
            return switch (terminal) {
                case "[ProgName]" -> "@main()";
                case "[VarName]", "[Number]" -> token;
                case "LET" -> "define i32 ";
                case "BE" -> line(" {");
                case "END" -> line("ret i32 0") + line("}");
                case "COLUMN" -> line("");
                case "OUT" -> "ret ";
                case "+" -> " add ";
                case "=" -> " = ";
                default -> "";
            };
        }
    }

    private String program(ParseTree node) {
        // Program -> LET [ProgName] BE <Code> END
        return line("@.str_in = private unnamed_addr constant [3 x i8] c\"%d\\00\"") +
                line("@.str_out = private unnamed_addr constant [5 x i8] c\"%d\\n\\00\"") +
                line("declare i32 @scanf(i8*, ...)") +
                line("declare i32 @printf(i8*, ...)") +
                line() +
                generate(node.getChildren().get(0)) +
                generate(node.getChildren().get(1)) +
                generate(node.getChildren().get(2)) +
                line("entry:") +
                line("%fmt_in = getelementptr inbounds [3 x i8], [3 x i8]* @.str_in, i32 0, i32 0") +
                line("%fmt_out = getelementptr inbounds [4 x i8], [4 x i8]* @.str_out, i32 0, i32 0") +
                line(generate(node.getChildren().get(3))) +
                line(generate(node.getChildren().get(4)));
    }

    private String code(ParseTree node) {
        // Code -> <Instruction> : <Code> | epsilon
        if (node.getChildren().size() > 1) {
            return line(generate(node.getChildren().getFirst())) +
                    line(generate(node.getChildren().get(2)));
        }
        return "";
    }

    private String instruction(ParseTree node) {
        // Instruction -> <Assign> | <If> | <While> | <Output> | <Input>
        return line(generate(node.getChildren().getFirst()));
    }

    private String assign(ParseTree node) {
        // Assign -> VarName = <ExprArith>
        String var = generate(node.getChildren().getFirst());
        return line("%" + variableRegistration(var) + generate(node.getChildren().get(1)) + "alloca i32") +
                line("store i32 " + generate(node.getChildren().get(2)) + ", i32* %" + var) +
                line("%" + variableRegistration(var) + generate(node.getChildren().get(1)) + "load i32, i32* %" + var);
    }

    private String exprArith(ParseTree node) {
        // ExprArith -> <Prod> <ExprArith'>
        return generate(node.getChildren().get(0)) +
                generate(node.getChildren().get(1));
    }

    private String exprArithPrime(ParseTree node) {
        // ExprArith' -> + <Prod> <ExprArith'> | - <Prod> <ExprArith'> | epsilon
        if (node.getChildren().size() == 3) {
            return generate(node.getChildren().get(0)) +
                    generate(node.getChildren().get(1)) +
                    generate(node.getChildren().get(2));

        }
        return "";
    }

    private String prod(ParseTree node) {
        // Prod -> <Atom> <Prod'>
        return generate(node.getChildren().get(0)) +
                generate(node.getChildren().get(1));
    }

    private String prodPrime(ParseTree node) {
        // Prod' -> * <Atom> <Prod'> | epsilon
        if (node.getChildren().size() == 3) {
            return generate(node.getChildren().getFirst()) +
                    generate(node.getChildren().get(1)) +
                    generate(node.getChildren().get(2));
        }
        return "";
    }

    private String atom(ParseTree node) {
        // Atom -> [Number] | [VarName] | ( <ExprArith> )
        if (node.getChildren().size() == 1) {
            return generate(node.getChildren().getFirst());
        } else {
            return generate(node.getChildren().getFirst()) +
                    generate(node.getChildren().get(1)) +
                    generate(node.getChildren().get(2));
        }
    }

    private String iF(ParseTree node) {
        // If -> IF { <Cond> } THEN <Code> END
//        String trueLabel = "label_" + labelCount++;
//        String endLabel = "label_" + labelCount++;
//
//        generate(node.getChildren().get(2)); // generate <Cond>
//        finalOutput.append("br i1 %").append(tempCount - 1).append(", label %").append(trueLabel).append(", label %").append(endLabel).append("\n");
//
//        finalOutput.append(trueLabel).append(":\n");
//        generate(node.getChildren().get(5)); // generate <Code>
//        finalOutput.append("br label %").append(endLabel).append("\n");
//
//        finalOutput.append(endLabel).append(":\n");
        return "";
    }

    private String ifTail(ParseTree node) {
        return "";
    }

    private String cond(ParseTree node) {
        return "";
    }

    private String condPrime(ParseTree node) {
        return "";
    }

    private String simpleCond(ParseTree node) {
        return "";
    }

    private String comp(ParseTree node) {
        // While -> WHILE { <Cond> } REPEAT <Code> END
//        String startLabel = "label_" + labelCount++;
//        String bodyLabel = "label_" + labelCount++;
//        String endLabel = "label_" + labelCount++;
//
//        finalOutput.append("br label %").append(startLabel).append("\n");
//        finalOutput.append(startLabel).append(":\n");
//
//        generate(node.getChildren().get(2)); // generate <Cond>
//        finalOutput.append("br i1 %").append(tempCount - 1).append(", label %").append(bodyLabel).append(", label %").append(endLabel).append("\n");
//
//        finalOutput.append(bodyLabel).append(":\n");
//        generate(node.getChildren().get(5)); // generate <Code>
//        finalOutput.append("br label %").append(startLabel).append("\n");
//
//        finalOutput.append(endLabel).append(":\n");
        return "";
    }

    private String whilE(ParseTree node) {
//        <While> → WHILE {<Cond>} REPEAT <Code> END
        return line("while_cond:") +
                line("while_body:") +
                generate(node.getChildren().get(5)) +
                line("while_end:");
    }

    private String output(ParseTree node) {
        // Output -> OUT([VarName])
        String var = variableRegistration(generate(node.getChildren().get(2)));
        return line("call i32 (i8*, ...) @printf(i8* %fmt_out, i32 %" + var + ")");
    }

    private String input(ParseTree node) {
        // <Input> → IN ( [VarName] )
        String var = variableRegistration(generate(node.getChildren().get(2)) + "_input");
        return line("%" + var + " = alloca i32") +
                line("call i32 (i8*, ...) @scanf(i8* %fmt_in, i32* %" + var + ")") +
                line("%" + variableRegistration(var) + generate(node.getChildren().get(1)) + " = load i32, i32* %" + var);
    }
}
