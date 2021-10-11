# 
#  Separated from main Makefile because it may need to be altered 
#  for different system environments (e.g., my MacOS machine vs. 
#  shared server ix).  Packaging them up in the project directory to minimize 
#  that.  (Maybe it should be ../lib, ../tools, etc?)
# 
CUPHOME = ./lib  
CUP = $(CUPHOME)/java-cup-11b.jar
CUPRT = $(CUPHOME)/java-cup-11b-runtime.jar
JFLEX = ./tools/jflex-1.4.3/bin/jflex
