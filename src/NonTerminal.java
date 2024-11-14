public enum NonTerminal {
    /** Program */
    PROGRAM("Program"),
    /** Code */
    CODE("CodeBlock"),
    /** Instruction */
    INSTRUCTION("Instruction"),
    /** Assignment */
    ASSIGNMENT("Assign"),
    /** If */
    IFSTATEMENT("IfStatement"),
    /** IfTail */
    IFTAIL("IfTail"),
    /** While */
    WHILESTATEMENT("WhileStatement"),
    /** Output */
    OUTPUTSTATEMENT("OutputStatement"),
    /** Input */
    INPUTSTATEMENT("InputStatement"),
    /** Arithmetic Expression */
    ARITHMETICEXPRESSION("ExprArith"),
    /** Expression */
    EXPRESSION("Expr"),
    /** Expression' */
    EXPRESSIONPRIME("Expr'"),
    /** Plus */
    PLUS("+"),
    /** Minus */
    MINUS("-"),
    /** Term */
    TERM("Term"),
    /** Term' */
    TERMPRIME("Term'"),
    /** Mul */
    MUL("*"),
    /** Div */
    DIV("/"),
    /** Factor */
    UNIT("Unit"),
    /** Condition */
    CONDITION("Cond"),
    /** Condition' */
    CONDITIONPRIME("Cond'"),
    /** Comparison */
    COMPARISON("Comp"),
    /** Epsilon */
    EPSILON("");

    private final String value;

    NonTerminal(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /*
    Overriding toString() will let us get the value of the enum directly!
     */
    @Override
    public String toString() {
        return getValue();
    }
}
