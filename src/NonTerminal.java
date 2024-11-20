/**
 * Enum for Non-Terminals in the grammar
 */
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
    /** Term */
    TERM("Term"),
    /** Term' */
    TERMPRIME("Term'"),
    /** Factor */
    UNIT("Unit"),
    /** Condition */
    CONDITION("Cond"),
    /** Condition' */
    CONDITIONPRIME("Cond'"),
    /** Comparison */
    COMPARISON("Comp"),

    /* The following are technically not non-terminals, though we are just using this enum
       for the sake of pretty printing; could move to its own enum named better */
    /** Plus */
    PLUS("+"),
    /** Minus */
    MINUS("-"),
    /** Mul */
    MUL("*"),
    /** Div */
    DIV("/"),
    /** Epsilon */
    EPSILON("");

    private final String value;

    /**
     * Constructor for the NonTerminal enum
     * @param value the value of the non-terminal
     */
    NonTerminal(String value) {
        this.value = value;
    }

    /**
     * Getter for the value of the non-terminal
     * @return the value of the non-terminal
     */
    public String getValue() {
        return value;
    }

    /**
     * Overriding toString() to return the value of the non-terminal
     * @return the value of the non-terminal
     */
    @Override
    public String toString() {
        return getValue();
    }
}
