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

SPACE = [ \n\t]+
FILE = [.-_/a-zA-Z0-9]+
// FIXME:  You'll want to define a few more patterns here, to 
// keep the rules below simple

%%

{SPACE}    { ; /* skip */ }

  // FIXME
  /*  Here is a set of patterns for matching and discarding some comments. 
   *  The Cool spec says comments can be nested, but we can't match
   *  nested comments with a single regular expression (that's a theorem
   *  you may recall from automata theory).  So, you'll need to augment
   *  the DFA with some state ... keep track of nesting level. 
   */ 

"(*" { yybegin(INCOMMENT); comment_begin_line = yyline; }
<INCOMMENT> {
  "*)"  { yybegin(YYINITIAL);  }
  [^\*]+ { /* skip */ }
  .     { /* skip */ }
  \n    { /* skip */ }
  <<EOF>> { lexical_error("Comment \"(*...\"  missing ending \"*)\"" +
                          "\nComment began on line " +comment_begin_line ); 
	    yybegin(YYINITIAL); 
          }
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
/* FIXME: There's a bunch more */ 


/* Keywords */
"true"	    { return mkSym( sym.TRUE ); }
"false"	    { return mkSym( sym.FALSE ); }
"inherits"  { return mkSym( sym.INHERITS ); }
"case"	    { return mkSym( sym.CASE ); }
"esac"	    { return mkSym( sym.ESAC ); }
/* FIXME: There's a bunch more */


/* Identifiers */ 
/* FIXME:  There are two kinds, ID and TYPEID, depending on 
 * capitalization. 
 */ 

/* Literals
 * FIXME:  There are integer literals and string literals.  
 * Integer literals are easy; one regular expression will do. 
 * String literals are tricky:  You'll need to use explicit states. 
 * You can decide whether to convert integer literals to type Integer
 * here (in which case the parser will have to cast them from Object)
 * or in the parser (in which case the parser will have to do the 
 * conversion). 
 * 
 * I'll give some starter code for literal strings, but you'll need 
 * to fix it up to handle escaped characters (which are incorporated 
 * into the string literal) and unescaped newlines (which are an error). 
 */ 

\"   { yybegin(STRING); lit=""; }
<STRING> {
    /* Need some lexical errors here */ 
    \"	      { yybegin(YYINITIAL); 
                return mkSym( sym.STRINGLIT, lit ); }				  }

/* Default when we don't match anything above 
 * is a scanning error.  We don't want too many of 
 * these, but it's hard to know how much to gobble ... 
 */ 
.   { lexical_error("Illegal character '" +
      	              yytext() +
		      "' "); 
    }

