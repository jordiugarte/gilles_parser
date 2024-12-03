public abstract class LLVMParserInterface {
    public static String BREAK = "\n";

    protected String line(String input) {
        return input.concat(BREAK);
    }

    public abstract String generateCode(ParseTree node);

    protected abstract void generate(ParseTree node);
    protected abstract void program(ParseTree node);
    protected abstract void code(ParseTree node);
    protected abstract void instruction(ParseTree node);
    protected abstract void assign(ParseTree node);
    protected abstract void exprArith(ParseTree node);
    protected abstract void exprArithPrime(ParseTree node);
    protected abstract void prod(ParseTree node);
    protected abstract void prodPrime(ParseTree node);
    protected abstract void atom(ParseTree node);
    protected abstract void iF(ParseTree node);
    protected abstract void ifTail(ParseTree node);
    protected abstract void cond(ParseTree node);
    protected abstract void condPrime(ParseTree node);
    protected abstract void simpleCond(ParseTree node);
    protected abstract void comp(ParseTree node);
    protected abstract void whilE(ParseTree node);
    protected abstract void output(ParseTree node);
    protected abstract void input(ParseTree node);
}
