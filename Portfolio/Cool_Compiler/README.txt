This is the beginning of a compiler project that I started for the language Cool, whose manual can be found at

https://theory.stanford.edu/~aiken/software/cool/cool-manual.pdf

So far, the working components are the scanner/lexer and the parser.  In order to write these, I worked off jflex and cup skeletons provided for me - these have been included in their own directory in order to be clear about which parts of this were coded by me personally.

The next part of the compiler is that abstract syntax tree and type checker, which is currently under development.

In order to compile the working files, simply use the 'make' command inside the WorkingScannerParser directory or the WorkingAST directory.

Then, if the desired Cool file is addtest_works.cool, we would run the compiler as follows:

java -cp .:./lib/commons-cli-1.2.jar:./lib/java-cup-11b-runtime.jar:./lib/java-cup-11b.jar/ Cool ./tests/addtest_works.cool

With regards to tests, the following provided files parse without error:

arith.cool
tiny.cool
hairyscary.cool
typecase.cool

The other tests fail on syntax errors, mainly due to there being no class definition or declaration.

In addition, I have fixed my own file coolBasics.cool to parse correctly by adding and removing semicolons where needed, fixing an if statement to terminate correctly, formatting dispatch statements correctly, and removing some incorrect and redundant variable declarations.  While the other files in 'tests' folder here have been changed to '<-' assignments and 'loop-pool' syntax, this one also demonstrates that my parser can now support ';=' and 'do-od' on account of the changes made to the scanner.

I have also included an incorrect test of my own.  Parsing addtest_fails.cool will initially return syntax errors for missing parentheses in the definition of the 'value' method and an incomplete 'if' statement in the main loop.  If these are fixed, it will return a syntax error for a missing semicolon at the end of the 'add2' method.  If this is fixed, it will parse correctly.

