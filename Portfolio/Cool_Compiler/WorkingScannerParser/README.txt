The only file I have significantly modified in this assignment is Cool.cup though minor changes have been made to Cool.jflex as well (namely, I have fixed the typo identified in the instructor comment on Canvas and I have enabled both 'do-od' and 'loop-pool' syntax by having both return LOOP and POOL tokens - ':=' and '<-' both return ASSIGN tokens, too).

As such, I have not included a script this time, as it would simply have the line 'make'.  To run the parser, use the following command as a template where the path to the desired file should be changed as needed:

java -cp .:./lib/commons-cli-1.2.jar:./lib/java-cup-11b-runtime.jar:./lib/java-cup-11b.jar/ Cool ./tests/addtest.cool

Note that 'make' and the command above both assumed that the folders 'lib' and 'tools' folders from the Scanner starter code are one level up in the directory hierarchy.

With regards to tests, the following provided files parse without error:

arith.cool
tiny.cool
hairyscary.cool
typecase.cool

The other tests fail on syntax errors, mainly due to there being no class definition or declaration.

In addition, I have fixed my own file coolBasics.cool to parse correctly by adding and removing semicolons where needed, fixing an if statement to terminate correctly, formatting dispatch statements correctly, and removing some incorrect and redundant variable declarations.  While the other files in 'tests' folder here have been changed to '<-' assignments and 'loop-pool' syntax, this one also demonstrates that my parser can now support ';=' and 'do-od' on account of the changes made to the scanner.

Finally, I have included an incorrect test of my own.  Parsing addtest.cool will initially return syntax errors for missing parentheses in the definition of the 'value' method and an incomplete 'if' statement in the main loop.  If these are fixed, it will return a syntax error for a missing semicolon at the end of the 'add2' method.  If this is fixed, it will parse correctly.

