#
# Makefile for Cool compiler components 
#

LIB = ../lib
CUPHOME = $(LIB)
CUP = java -cp $(CUPHOME)/java-cup-11b.jar java_cup.Main 
CUPLIB = ../lib/java-cup-11b-runtime.jar
JFLEX = ../tools/jflex-1.4.3/bin/jflex
CLI = ../lib/commons-cli-1.2.jar
LANG = ../lib/commons-lang-2.6.jar

## Eventually we'll use all of these 
LIBS = $(CUPLIB):$(CLI):$(LANG)

# JAVACOPT =  -Xlint:unchecked 
JAVACOPT =  

all:	Cool.class ScanDriver.class

Cool.class:	Cool.java Scanner.class parser.class ASTNode.class TypeChecker.class TableManager.class Clazz.class Pair.class TypeError.class
	javac -classpath .:$(LIBS) $< 

%.class:	%.java
	javac -classpath .:$(LIBS) $< 

sym.java parser.java: cup_fake

cup_fake: Cool.cup ASTNode.class
	$(CUP) -locations Cool.cup

tables:	Cool.cup
	$(CUP) -dump -locations Cool.cup  &> tables

Scanner.java:	Cool.jflex  sym.java
	$(JFLEX) --skel $(LIB)/jflex-skeleton-nested Cool.jflex

ScanDriver.class:  Scanner.class

ASTNode.class: Clazz.class TypeError.class TableManager.class

TypeChecker.class: ASTNode.class Clazz.class 

TableManager.class: Pair.class Clazz.class

scanner: ScanDriver.class Scanner.class

parser: parser.class Cool.class ASTNode.class



#=================

clean: ; rm *.class parser.java Scanner.java *~




