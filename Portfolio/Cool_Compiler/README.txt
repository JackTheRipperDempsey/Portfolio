This is the beginning of a compiler project that I started for the language Cool, whose manual can be found at

https://theory.stanford.edu/~aiken/software/cool/cool-manual.pdf

In order to compile the working files, simply use the 'make' command inside the WorkInProgressCodeGenerator directory.

Then, if the desired Cool file is addtest_works.cool, we would run the compiler as follows:

java -cp .:./lib/commons-cli-1.2.jar:./lib/java-cup-11b-runtime.jar:./lib/java-cup-11b.jar/ Cool ./tests/addtest_works.cool

(The bash script run_all_tests in the src folder is also useful here.)

Lexing, parsing, type checking, and code generation all currently work for valid COOL programs.

The only feature from the COOL manual not currently implemented is garbage collection.