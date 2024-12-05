import java.util.HashMap;
import java.util.Map;

public class LLVMParser {

    private StringBuilder finalOutput = new StringBuilder();
    private Map<String, String> variables = new HashMap<>();

    private int tempCount;
    private int labelCount;

    private String line(String input) {
        return input.concat("\n");
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
            String token = expression.split("lexical unit: ")[0].split("token: ")[1].replaceAll(" ", "");
            return switch (terminal) {
                case "[ProgName]" -> "@" + token;
                case "[VarName]" -> "i32 %" + token;
                case "[Number]" -> token;
                case "LET" -> "define i32 ";
                case "BE" -> "(";
                case "END" -> line("}");
                case "COLUMN" -> line("");
                default -> "";
            };
        }
    }

    private String program(ParseTree node) {
        // Program -> LET [ProgName] BE <Code> END
        return generate(node.getChildren().get(0)) +
                generate(node.getChildren().get(1)) +
                generate(node.getChildren().get(2)) +
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
//        String varName = node.getChildren().get(0).getLabel().getValue().toString();
//        generate(node.getChildren().get(2)); // Traverse <ExprArith>
//        String tempVar = "%" + (tempCount - 1); // The result of <ExprArith>
//        String llvmVar = variables.computeIfAbsent(varName, v -> "@" + v);
//        finalOutput.append("store i32 ").append(tempVar).append(", i32* ").append(llvmVar).append("\n");
        return "";
    }

    private String exprArith(ParseTree node) {
        // ExprArith -> <Prod> <ExprArith'>
//        generate(node.getChildren().get(0)); // generate <Prod>
//        generate(node.getChildren().get(1)); // generate <ExprArith'>
        return "";
    }

    private String exprArithPrime(ParseTree node) {
        // ExprArith' -> + <Prod> <ExprArith'> | epsilon
//        if (node.getChildren().size() == 3) {
//            generate(node.getChildren().get(1)); // generate <Prod>
//            String right = "%" + (tempCount - 1);
//            String left = "%" + (tempCount - 2);
//            String result = "%" + tempCount++;
//            finalOutput.append(result).append(" = add i32 ").append(left).append(", ").append(right).append("\n");
//            generate(node.getChildren().get(2)); // generate <ExprArith'>
//        }
        return "";
    }

    private String prod(ParseTree node) {
        // Prod -> <Atom> <Prod'>
//        generate(node.getChildren().get(0)); // generate <Atom>
//        generate(node.getChildren().get(1)); // generate <Prod'>
        return "";
    }

    private String prodPrime(ParseTree node) {
        // Prod' -> * <Atom> <Prod'> | epsilon
//        if (node.getChildren().size() == 3) {
//            generate(node.getChildren().get(1)); // generate <Atom>
//            String right = "%" + (tempCount - 1);
//            String left = "%" + (tempCount - 2);
//            String result = "%" + tempCount++;
//            finalOutput.append(result).append(" = mul i32 ").append(left).append(", ").append(right).append("\n");
//            generate(node.getChildren().get(2)); // generate <Prod'>
//        }
        return "";
    }

    private String atom(ParseTree node) {
        // Atom -> [Number] | [VarName] | ( <ExprArith> )
//        if (node.getChildren().get(0).getLabel().getType() == LexicalUnit.NUMBER) {
//            String number = node.getChildren().get(0).getLabel().getValue().toString();
//            String result = "%" + tempCount++;
//            finalOutput.append(result).append(" = add i32 0, ").append(number).append("\n");
//        } else if (node.getChildren().get(0).getLabel().getType() == LexicalUnit.VARNAME) {
//            String varName = node.getChildren().get(0).getLabel().getValue().toString();
//            String llvmVar = variables.computeIfAbsent(varName, v -> "@" + v);
//            String result = "%" + tempCount++;
//            finalOutput.append(result).append(" = load i32, i32* ").append(llvmVar).append("\n");
//        } else {
//            generate(node.getChildren().get(1)); // generate <ExprArith>
//        }
        return "";
    }

    private String iF(ParseTree node) {
//        // If -> IF { <Cond> } THEN <Code> END
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
        return "";
    }

    private String output(ParseTree node) {
        // Output -> OUT([VarName])
//        String varName = node.getChildren().get(2).getLabel().getValue().toString();
//        String llvmVar = variables.get(varName);
//        String temp = "%" + tempCount++;
//        finalOutput.append(temp).append(" = load i32, i32* ").append(llvmVar).append("\n")
//                .append("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str, i32 0, i32 0), i32 ")
//                .append(temp).append(")\n");
        return "";
    }

    private String input(ParseTree node) {
        // <Input> → IN ( [VarName] )
        return "," + generate(node.getChildren().get(2));
    }
}
