#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>
#include <fcntl.h>

#ifndef REDIRECTIONEXECUTION_H
#define REDIRECTIONEXECUTION_H

int redirectionExecution(char ** arr1, char ** arr2, char ** arr3, char ** arr4, 
	const int redirectionCounter, const int redirectionPosition1, const int redirectionPosition2, 
	const int pipeCounter, const int pipePos, char red1, char red2, char red3);

#endif
