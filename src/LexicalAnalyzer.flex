%%

%class LexicalAnalyzer
%unicode
%line
%column
%type Symbol
%standalone

// Return value of the program
%eofval{
	return new Symbol(LexicalUnit.EOS, yyline, yycolumn);
%eofval}

%state PROGRAM, CODE, INSTRUCTION, ARITHMETIC, CONDITION, INPUT_OUTPUT

/* Regular expression macros */
ProgName     = [a-zA-Z_][a-zA-Z0-9_]*
VarName      = [a-zA-Z_][a-zA-Z0-9_]*
Number       = [0-9]+
Whitespace   = [ \t\r\n]+
Comment      = "!!"[^\n]*
ShortComment = "\$"[^\n]*

/* Lexer rules */
%%
<YYINITIAL> {
  {Whitespace}           { /* Ignore whitespace */ }
  {Comment}              { /* Ignore comments */ }
  {ShortComment}         { /* Ignore short comments */ }
  "LET"                  { yybegin(PROGRAM); System.out.println(new Symbol(LexicalUnit.LET, yyline, yycolumn, yytext())); }
}

/* Program name state */
<PROGRAM> {
  {Whitespace}           { /* Ignore whitespace */ }
  {ProgName}             { yybegin(CODE); System.out.println(new Symbol(LexicalUnit.PROGNAME, yyline, yycolumn, yytext())); }
}

/* Code block state */
<CODE> {
  {Whitespace}           { /* Ignore whitespace */ }
  {Comment}              { /* Ignore comments */ }
  {ShortComment}         { /* Ignore short comments */ }
  "BE"                   { System.out.println(new Symbol(LexicalUnit.BE, yyline, yycolumn, yytext())); }
  "END"                  { yybegin(YYINITIAL); System.out.println("CODE"+new Symbol(LexicalUnit.END, yyline, yycolumn, yytext())); }
  ":"                    { System.out.println(new Symbol(LexicalUnit.COLUMN, yyline, yycolumn, yytext())); }
  {VarName}"="           { yybegin(ARITHMETIC); System.out.println(new Symbol(LexicalUnit.ASSIGN, yyline, yycolumn, yytext())); }
  "="                    { yybegin(ARITHMETIC); System.out.println(new Symbol(LexicalUnit.ASSIGN, yyline, yycolumn, yytext())); }
  "IN"                   { yybegin(INPUT_OUTPUT); System.out.println(new Symbol(LexicalUnit.INPUT, yyline, yycolumn, yytext())); }
  "OUT"                  { yybegin(INPUT_OUTPUT); System.out.println(new Symbol(LexicalUnit.OUTPUT, yyline, yycolumn, yytext())); }
  "IF"                   { yybegin(CONDITION); System.out.println(new Symbol(LexicalUnit.IF, yyline, yycolumn, yytext())); }
  "WHILE"                { yybegin(CONDITION); System.out.println(new Symbol(LexicalUnit.WHILE, yyline, yycolumn, yytext())); }
  {VarName}              { System.out.println(new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext())); }
  "("                    { System.out.println(new Symbol(LexicalUnit.LPAREN, yyline, yycolumn, yytext())); }
  ")"                    { System.out.println(new Symbol(LexicalUnit.RPAREN, yyline, yycolumn, yytext())); }
}

/* Conditionals and loops */
<CONDITION> {
  {Whitespace}           { /* Ignore whitespace */ }
  {Number}               { System.out.println(new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, yytext())); }
  {VarName}              { System.out.println(new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext())); }
  "=="                   { System.out.println(new Symbol(LexicalUnit.EQUAL, yyline, yycolumn, yytext())); }
  "<="                   { System.out.println(new Symbol(LexicalUnit.SMALEQ, yyline, yycolumn, yytext())); }
  "<"                    { System.out.println(new Symbol(LexicalUnit.SMALLER, yyline, yycolumn, yytext())); }
  "\\|"                  { System.out.println(new Symbol(LexicalUnit.PIPE, yyline, yycolumn, yytext())); }
  "->"                   { System.out.println(new Symbol(LexicalUnit.IMPLIES, yyline, yycolumn, yytext())); }
  "\\("                  { System.out.println(new Symbol(LexicalUnit.LPAREN, yyline, yycolumn, yytext())); }
  "\\)"                  { System.out.println(new Symbol(LexicalUnit.RPAREN, yyline, yycolumn, yytext())); }
  "THEN"                 { yybegin(CODE); System.out.println(new Symbol(LexicalUnit.THEN, yyline, yycolumn, yytext())); }
  "ELSE"                 { yybegin(CODE); System.out.println(new Symbol(LexicalUnit.ELSE, yyline, yycolumn, yytext())); }
  "REPEAT"               { yybegin(CODE); System.out.println(n"CONDITION"+ew Symbol(LexicalUnit.REPEAT, yyline, yycolumn, yytext())); }  // Change here
  "END"                  { yybegin(YYINITIAL); System.out.println(new Symbol(LexicalUnit.END, yyline, yycolumn, yytext())); }
  ":"                    { System.out.println(new Symbol(LexicalUnit.COLUMN, yyline, yycolumn, yytext())); }
  "{"                  { System.out.println(new Symbol(LexicalUnit.LBRACK, yyline, yycolumn, yytext())); }
  "}"                  { yybegin(CODE); System.out.println(new Symbol(LexicalUnit.RBRACK, yyline, yycolumn, yytext())); }

}

/* Input/Output instructions */
<INPUT_OUTPUT> {
  {Whitespace}           { /* Ignore whitespace */ }
  "("                  { System.out.println(new Symbol(LexicalUnit.LPAREN, yyline, yycolumn, yytext())); }
  ")"                  { yybegin(CODE); System.out.println(new Symbol(LexicalUnit.RPAREN, yyline, yycolumn, yytext())); }
  {VarName}              { System.out.println(new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext())); }
  ":"                    { System.out.println(new Symbol(LexicalUnit.COLUMN, yyline, yycolumn, yytext())); }
}

/* Arithmetic expressions */
<ARITHMETIC> {
  {Whitespace}           { /* Ignore whitespace */ }
  {Number}               { System.out.println(new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, yytext())); }
  {VarName}              { System.out.println(new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext())); }
  "\\+"                  { System.out.println(new Symbol(LexicalUnit.PLUS, yyline, yycolumn, yytext())); }
  "-"                    { System.out.println(new Symbol(LexicalUnit.MINUS, yyline, yycolumn, yytext())); }
  "\\*"                  { System.out.println(new Symbol(LexicalUnit.TIMES, yyline, yycolumn, yytext())); }
  "/"                    { System.out.println(new Symbol(LexicalUnit.DIVIDE, yyline, yycolumn, yytext())); }
  "\\("                  { System.out.println(new Symbol(LexicalUnit.LPAREN, yyline, yycolumn, yytext())); }
  "\\)"                  { yybegin(CODE); System.out.println(new Symbol(LexicalUnit.RPAREN, yyline, yycolumn, yytext())); }
  ":"                    { yybegin(CODE); System.out.println(new Symbol(LexicalUnit.COLUMN, yyline, yycolumn, yytext())); }
}

/* Catch-all for any unrecognized input */
<YYINITIAL, PROGRAM, CODE, ARITHMETIC, CONDITION, INPUT_OUTPUT> {
  {Whitespace}           { /* Ignore whitespace */ }
  {Comment}              { /* Ignore comments */ }
  {ShortComment}         { /* Ignore short comments */ }
  .                      { throw new Error("Unrecognized character at line " + (yyline + 1) + ", column " + (yycolumn + 1) + ": '" + yytext() + "'"); }
}