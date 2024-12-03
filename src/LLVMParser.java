import java.util.HashMap;
import java.util.Map;

public class LLVMParser extends LLVMParserInterface {

    private StringBuilder finalOutput = new StringBuilder();
    private Map<String, String> variables = new HashMap<>();

    private int tempCount;
    private int labelCount;

    public String generateCode(ParseTree node) {
        generate(node);
        finalOutput.append("ret i32 0\n"); // Add LLVM footer
        return finalOutput.toString();
    }

    protected void generate(ParseTree node) {
        if (node.getLabel().toString().startsWith("Non-terminal symbol: ")) {
            String symbol = node.getLabel().toString().split("Non-terminal symbol: ")[1];
            System.out.println(symbol);
            switch (symbol) {
                case "Program":
                    program(node);
                    break;
                case "Code":
                    code(node);
                    break;
                case "Instruction":
                    instruction(node);
                    break;
                case "Assign":
                    assign(node);
                    break;
                case "ExprArith":
                    exprArith(node);
                    break;
                case "ExprArith'":
                    exprArithPrime(node);
                    break;
                case "Prod":
                    prod(node);
                    break;
                case "Prod'":
                    prodPrime(node);
                    break;
                case "Atom":
                    atom(node);
                    break;
                case "Output":
                    output(node);
                    break;
                case "If":
                    iF(node);
                    break;
                case "While":
                    whilE(node);
                    break;
                default:
                    for (ParseTree child : node.getChildren()) {
                        generate(child);
                    }
                    break;
            }
        } else if (node.getLabel().toString().startsWith("token: ")) {
            String token = node.getLabel().toString().replaceAll(" ", "").split("token:")[1].split("lexicalunit")[0];
            String lexicalUnit = node.getLabel().toString().split("lexical unit: ")[1];
            //TODO
        } else {
            //TODO
        }
    }

    @Override
    protected void program(ParseTree node) {
        // Program -> LET [ProgName] BE <Code> END
        finalOutput.append(line("; ModuleID = 'Hello world'"))
                .append(line("declare i32 @printf(i8*, ...)"))
                .append(line("@.str = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\""));
        generate(node.getChildren().get(3)); // Traverse <Code>
    }

    @Override
    protected void code(ParseTree node) {
        // Code -> <Instruction> : <Code> | epsilon
        if (node.getChildren().size() == 3) { // Non-epsilon
            generate(node.getChildren().get(0)); // Traverse <Instruction>
            generate(node.getChildren().get(2)); // Traverse <Code>
        }
    }

    @Override
    protected void instruction(ParseTree node) {
        // Instruction -> <Assign> | <If> | <While> | <Output>
        generate(node.getChildren().get(0));
    }

    @Override
    protected void assign(ParseTree node) {
        // Assign -> VarName = <ExprArith>
        String varName = node.getChildren().get(0).getLabel().getValue().toString();
        generate(node.getChildren().get(2)); // Traverse <ExprArith>
        String tempVar = "%" + (tempCount - 1); // The result of <ExprArith>
        String llvmVar = variables.computeIfAbsent(varName, v -> "@" + v);
        finalOutput.append("store i32 ").append(tempVar).append(", i32* ").append(llvmVar).append("\n");
    }

    @Override
    protected void exprArith(ParseTree node) {
        // ExprArith -> <Prod> <ExprArith'>
        generate(node.getChildren().get(0)); // generate <Prod>
        generate(node.getChildren().get(1)); // generate <ExprArith'>
    }

    @Override
    protected void exprArithPrime(ParseTree node) {
        // ExprArith' -> + <Prod> <ExprArith'> | epsilon
        if (node.getChildren().size() == 3) {
            generate(node.getChildren().get(1)); // generate <Prod>
            String right = "%" + (tempCount - 1);
            String left = "%" + (tempCount - 2);
            String result = "%" + tempCount++;
            finalOutput.append(result).append(" = add i32 ").append(left).append(", ").append(right).append("\n");
            generate(node.getChildren().get(2)); // generate <ExprArith'>
        }
    }

    @Override
    protected void prod(ParseTree node) {
        // Prod -> <Atom> <Prod'>
        generate(node.getChildren().get(0)); // generate <Atom>
        generate(node.getChildren().get(1)); // generate <Prod'>
    }

    @Override
    protected void prodPrime(ParseTree node) {
        // Prod' -> * <Atom> <Prod'> | epsilon
        if (node.getChildren().size() == 3) {
            generate(node.getChildren().get(1)); // generate <Atom>
            String right = "%" + (tempCount - 1);
            String left = "%" + (tempCount - 2);
            String result = "%" + tempCount++;
            finalOutput.append(result).append(" = mul i32 ").append(left).append(", ").append(right).append("\n");
            generate(node.getChildren().get(2)); // generate <Prod'>
        }
    }

    @Override
    protected void atom(ParseTree node) {
        // Atom -> [Number] | [VarName] | ( <ExprArith> )
        if (node.getChildren().get(0).getLabel().getType() == LexicalUnit.NUMBER) {
            String number = node.getChildren().get(0).getLabel().getValue().toString();
            String result = "%" + tempCount++;
            finalOutput.append(result).append(" = add i32 0, ").append(number).append("\n");
        } else if (node.getChildren().get(0).getLabel().getType() == LexicalUnit.VARNAME) {
            String varName = node.getChildren().get(0).getLabel().getValue().toString();
            String llvmVar = variables.computeIfAbsent(varName, v -> "@" + v);
            String result = "%" + tempCount++;
            finalOutput.append(result).append(" = load i32, i32* ").append(llvmVar).append("\n");
        } else {
            generate(node.getChildren().get(1)); // generate <ExprArith>
        }
    }

    @Override
    protected void iF(ParseTree node) {
        // If -> IF { <Cond> } THEN <Code> END
        String trueLabel = "label_" + labelCount++;
        String endLabel = "label_" + labelCount++;

        generate(node.getChildren().get(2)); // generate <Cond>
        finalOutput.append("br i1 %").append(tempCount - 1).append(", label %").append(trueLabel).append(", label %").append(endLabel).append("\n");

        finalOutput.append(trueLabel).append(":\n");
        generate(node.getChildren().get(5)); // generate <Code>
        finalOutput.append("br label %").append(endLabel).append("\n");

        finalOutput.append(endLabel).append(":\n");
    }

    @Override
    protected void ifTail(ParseTree node) {

    }

    @Override
    protected void cond(ParseTree node) {

    }

    @Override
    protected void condPrime(ParseTree node) {

    }

    @Override
    protected void simpleCond(ParseTree node) {

    }

    @Override
    protected void comp(ParseTree node) {
        // While -> WHILE { <Cond> } REPEAT <Code> END
        String startLabel = "label_" + labelCount++;
        String bodyLabel = "label_" + labelCount++;
        String endLabel = "label_" + labelCount++;

        finalOutput.append("br label %").append(startLabel).append("\n");
        finalOutput.append(startLabel).append(":\n");

        generate(node.getChildren().get(2)); // generate <Cond>
        finalOutput.append("br i1 %").append(tempCount - 1).append(", label %").append(bodyLabel).append(", label %").append(endLabel).append("\n");

        finalOutput.append(bodyLabel).append(":\n");
        generate(node.getChildren().get(5)); // generate <Code>
        finalOutput.append("br label %").append(startLabel).append("\n");

        finalOutput.append(endLabel).append(":\n");
    }

    @Override
    protected void whilE(ParseTree node) {

    }

    @Override
    protected void output(ParseTree node) {
        // Output -> OUT([VarName])
        String varName = node.getChildren().get(2).getLabel().getValue().toString();
        String llvmVar = variables.get(varName);
        String temp = "%" + tempCount++;
        finalOutput.append(temp).append(" = load i32, i32* ").append(llvmVar).append("\n")
                .append("call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str, i32 0, i32 0), i32 ")
                .append(temp).append(")\n");
    }

    @Override
    protected void input(ParseTree node) {

    }
}
