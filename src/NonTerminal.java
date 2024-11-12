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
    /** PlusMinus */
    PLUSMINUS("PlusMinus"),
    /** Term */
    TERM("Term"),
    /** Term' */
    TERMPRIME("Term'"),
    /** MulDiv */
    MULDIV("MulDiv"),
    /** Factor */
    FACTOR("Factor"),
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
        return value;
    }
}
