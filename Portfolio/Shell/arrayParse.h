#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>
#include "tokenizer.h"
#include <fcntl.h>

#ifndef ARRAYPARSE_H
#define ARRAYPARSE_H

void arrayParse(char * buffer, char *** arg1, char *** arr2, char *** arr3, char *** arr4, 
	const int pipeCounter, const int pipePos, const int redirectionCounter, 
	const int redirectionPosition1, const int redirectionPosition2, 
	const int counter, char * red1, char * red2, char * red3, int backGroundCounter);

#endif
