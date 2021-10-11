/* JFlex spec for Cool scanner */ 

import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;

%% 

%cup 
%class Scanner
%line
%column
%type ComplexSymbolFactory.ComplexSymbol

%{  // Code to be included in the Scanner class goes here


  // Stack of file names, for 'include' files
  java.util.Stack<String> input_stack = new java.util.Stack<String>(); 
  String cur_file = "";

  // Buffer for building up tokens that take more than one pattern,
  // e.g., quoted strings 
  StringBuffer string = new StringBuffer();

  // The default "Symbol" class in CUP is stupid.  We need the alternative
  // ComplexSymbol class, which is managed by a ComplexSymbolFactory, 
  // because in Java you don't have Problems, you have ProblemFactories
  // We share the ComplexSymbolFactory with other components;
  // CUP examples do this, but I'm not sure why or if it's necessary.
  ComplexSymbolFactory symbolFactory; 

  // Alternate constructor to share the factory
  public Scanner(java.io.Reader in, ComplexSymbolFactory sf){
	this(in);
	symbolFactory = sf;
  }


  // Factor a bunch of tedium from constructing ComplexSymbol objects
  // into helper functions. 
  /**
   * Create a Symbol (token + location + text) for a lexeme 
   * that is not converted to another kind of value, e.g., 
   * keywords, punctuation, etc.
   */
  public ComplexSymbolFactory.ComplexSymbol mkSym(int id) {
    	    return (ComplexSymbolFactory.ComplexSymbol) 
               symbolFactory.newSymbol( 
	    	  sym.terminalNames[id], // per Cup documentation, no idea why
		  id,               // the actual integer used as a token code
		  new Location(yyline+1, yycolumn+1),  	// Left extent of token
		  new Location(yyline+1, yycolumn+yylength()), // Right extent
		  yytext()       	 		  // Text of the token
		  );
   }

  /**
   * Create a Symbol (token + location + value) for a lexeme 
   * that is not converted to another kind of value, e.g., 
   * an integer literal, or a token that requires more than 
   * one pattern to match (so that we can't just grab yytext).
   */
   public ComplexSymbolFactory.ComplexSymbol mkSym(int id, Object value) {
    	    return (ComplexSymbolFactory.ComplexSymbol) 
                symbolFactory.newSymbol( 
	    	  sym.terminalNames[id],  // per Cup documentation, no idea why
		  id,           	  // the actual integer token code
		  new Location(yyline+1, yycolumn+1),  // Left extent of token
		  new Location(yyline+1, yycolumn+yylength()), // Right extent
		  value      	 		 // e.g. Integer for int value
		  );
   }


   int lexical_error_count = 0; 
   int comment_begin_line = 0; /* For running off end of file in comment */ 
   int MAX_LEX_ERRORS = 20;

   String lit = ""; 
   int count = 0;

  // If the driver gives us an error report class, we use it to print lexical
  // error messages
  ErrorReport report = null; 
  public void setErrorReport( ErrorReport _report) {
       report = _report;
  }

  void err(String msg) {
    if (report == null) {
        System.err.println(msg); 
    } else {
        report.err(msg); 
    }
   }

  void lexical_error(String msg) {
    String full_msg = "Lexical error at " + cur_file + 
    		      " line " + yyline + 
    		       ", column " + yycolumn +
		       ": " + msg; 
    err(full_msg); 
    if (++lexical_error_count > MAX_LEX_ERRORS) {
       err("Too many lexical errors, giving up."); 
       System.exit(1); 
    }
  }
  
%}
// %debug


%state INCLUDEFILE
%xstate INCOMMENT
%xstate STRING

SPACE = [ \n\t\r]+
FILE = [.-_/a-zA-Z0-9]+
BASICCOMMENT = "--" [^\n\r]* "\n" | "--" [^\n\r]* "\r"
IDENT = [a-z] [_a-zA-Z0-9]* 
TYPEIDENT = [A-Z] [_a-zA-Z0-9]*
INTEGER = [0-9]+
LINETERMINATOR = \r|\n|\r\n

%%

{SPACE}    { ; /* skip */ }
{BASICCOMMENT}  { ; /* skip */ }

"(*" { yybegin(INCOMMENT); comment_begin_line = yyline; }
<INCOMMENT> {

  [\n\r]* "*)"  { 
         if (count == 0){
             yybegin(YYINITIAL);  
	 }
         else {
             --count;
         }
  }
  [^\*]+ {  /* skip */ }
  .     {  /* skip */ }
  \n    {  /* skip */ }
 [\n\r]* "(*"  { ++count;}
  <<EOF>> { lexical_error("Comment \"(*...\"  missing ending \"*)\"" +
                          "\nComment began on line " +comment_begin_line ); 
	    yybegin(YYINITIAL); 
          }
}

 "*)" {
      lexical_error("Comment \"*)...\"  missing starting \"(*\"");
     }


/* This isn't in the COOL language spec, but it's a good
 * trick to know.  Note that you must use the --skel option
 * with jflex and specify the skeleton.nested skeleton file, 
 * in place of the standard jflex skeleton. 
 */
"#include" { yybegin(INCLUDEFILE); }
<INCLUDEFILE>{FILE}  { 
             String filename=yytext(); 
   	     yybegin(YYINITIAL); 
	     input_stack.push(cur_file); 
	     cur_file = filename; 
	     yypushStream(new java.io.FileReader(yytext())); 
}
<<EOF>>    { if (yymoreStreams()) {
	        yypopStream(); 
		cur_file = input_stack.pop(); 
	     }  else {
                return mkSym( sym.EOF ); 
	     }
           }


/* Punctuation */ 

"("	   { return mkSym( sym.LPAREN ); }
")"	   { return mkSym( sym.RPAREN ); }
";"	   { return mkSym( sym.SEMI ); }
":"        { return mkSym( sym.COLON ); }
","        { return mkSym( sym.COMMA ); }
"."        { return mkSym( sym.DOT ); }
"@"        { return mkSym( sym.AT ); }
"{"	   { return mkSym( sym.LBRACE ); }
"}"	   { return mkSym( sym.RBRACE ); }
"+"        { return mkSym( sym.PLUS ); }
"-"        { return mkSym( sym.MINUS ); }
"/"       { return mkSym( sym.DIV ); }
"*"        { return mkSym( sym.TIMES ); }
"~"        { return mkSym( sym.NEG ); }
"<-"       { return mkSym( sym.ASSIGN ); }
":="       { return mkSym( sym.ASSIGN ); }
"="        { return mkSym( sym.EQ ); }
"<"        { return mkSym( sym.LT ); }
"<="       { return mkSym( sym.LEQ ); }
"=>"       { return mkSym( sym.RIGHTARROW ); }


/* Keywords */
"true"	    { return mkSym( sym.TRUE, Boolean.TRUE ); }
"false"	    { return mkSym( sym.FALSE, Boolean.FALSE ); }
"inherits"  { return mkSym( sym.INHERITS ); }
"case"	    { return mkSym( sym.CASE ); }
"esac"	    { return mkSym( sym.ESAC ); }
"loop"	    { return mkSym( sym.LOOP ); }
"pool"	    { return mkSym( sym.POOL ); }
"do"        { return mkSym( sym.LOOP ); }
"od"        { return mkSym( sym.POOL ); }
"let"       { return mkSym( sym.LET ); }
"in"        { return mkSym( sym.IN ); }
"if"        { return mkSym( sym.IF ); }
"fi"        { return mkSym( sym.FI ); }
"then"      { return mkSym( sym.THEN ); }
"else"      { return mkSym( sym.ELSE ); }
"while"     { return mkSym( sym.WHILE ); }
"new"       { return mkSym( sym.NEW ); }
"of"        { return mkSym( sym.OF ); }
"class"     { return mkSym( sym.CLASS ); }
"inherits"  { return mkSym( sym.INHERITS ); }
"isvoid"    { return mkSym( sym.ISVOID ); }
"not"       { return mkSym( sym.NOT ); }


/* Identifiers */ 

{IDENT}		{ return mkSym( sym.ID, yytext() ); }
{TYPEIDENT}	{ return mkSym( sym.TYPEID, yytext() ); }


/* Literals */

{INTEGER}	{return mkSym( sym.INTLIT, new Integer(yytext() ) ); }


\"   { yybegin(STRING); lit=""; }
<STRING> {
    /* Need some lexical errors here */ 
    [^\n\"\\\r]+    { lit=lit+yytext(); }

    \"	      { yybegin(YYINITIAL); 
                return mkSym( sym.STRINGLIT, lit ); }	

    "\\b"         { lit=lit+"\b"; }
    "\\t"         { lit=lit+"\t"; }
    "\\n"         { lit=lit+"\n"; }
    "\\f"         { lit=lit+"\f"; }
    "\\r"         { lit=lit+"\r"; }
    "\\\""        { lit=lit+"\""; }
    "\\'"         { lit=lit+"\'"; }
    "\\\\"        { lit=lit+"\\"; }

    \\.       { lexical_error("Illegal escape sequence '"+yytext()+"' "); }
    {LINETERMINATOR}      { lexical_error("Unterminated string at end of the line"); }
}

/* Default when we don't match anything above 
 * is a scanning error.  We don't want too many of 
 * these, but it's hard to know how much to gobble ... 
 */ 
.   { lexical_error("Illegal character '" +
      	              yytext() +
		      "' "); 
    }

