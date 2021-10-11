#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>
#include "tokenizer.h"
#include <fcntl.h>
#include "arrayParse.h"

void arrayParse(char * buffer, char *** arr1, char *** arr2, char *** arr3, char *** arr4, 
	const int pipeCounter, const int pipePos, const int redirectionCounter, 
	const int redirectionPosition1,const int redirectionPosition2, 
	const int counter, char * red1, char * red2, char * red3, int backGroundCounter){
	
	// Redeclare flags and variables
	char * tok;
	TOKENIZER *tokenizer;	

	/* This section creates arrays for each division
	 * created by the redirection arrows, arr1, arr2,
	 * and arr3 if it's needed
	 * */
	// Fill in commands on left side of angle bracket
	tokenizer = init_tokenizer( buffer );
	int j = 0;
	if ((pipeCounter == 0) || ((redirectionCounter > 0) && (pipePos > redirectionPosition1))){
		*arr1 = (char **) malloc(redirectionPosition1 * sizeof (char *));
		for (j = 0; j < redirectionPosition1; ++j){
			tok = get_next_token(tokenizer);
			(*arr1)[j] = (char *) malloc(sizeof tok); 
			(*arr1)[j] = tok;
		}
		tok = get_next_token(tokenizer);
		*red1 = *tok;
		j = 0;
		if (redirectionCounter == 1) {
			// Fill in right side of  first angle bracket
			if (pipeCounter == 0){
				if (backGroundCounter == 0){
					*arr2 = (char **) malloc((counter-redirectionPosition1-1)*sizeof(char *));	
					while((tok = get_next_token(tokenizer)) != NULL){
						(*arr2)[j] = (char *) malloc(sizeof(tok));
						(*arr2)[j] = tok;
						++j;
					}
				}
				else {
					*arr2 = (char **) malloc((counter-redirectionPosition1-2)*sizeof(char *));	
					for (j = 0; j < counter-redirectionPosition1-2; ++j){
						(*arr2)[j] = (char *) malloc(sizeof(tok));
						tok = get_next_token(tokenizer);
						(*arr2)[j] = tok;
					}
				}
			}
			else if (redirectionPosition1 < pipePos){
				*arr2 = (char **) malloc((pipePos-redirectionPosition1-1)*sizeof(char *));	
				for (j = 0; j < (pipePos-redirectionPosition1-1); ++j){
					tok = get_next_token(tokenizer);
					(*arr2)[j] = (char *) malloc(sizeof(tok));
					(*arr2)[j] = tok;
				}
				tok = get_next_token(tokenizer);
				*red2 = *tok;
				if (backGroundCounter == 0){
					*arr3 = (char **) malloc((counter-pipePos-1)*sizeof(char *));
					while((tok = get_next_token(tokenizer)) != NULL){
						(*arr3)[j] = (char *) malloc(sizeof(tok));
						(*arr3)[j] = tok;
						++j;
					}
				}
				else {
					*arr3 = (char **) malloc((counter-pipePos-2)*sizeof(char *));
					for (j = 0; j < counter-pipePos-2; ++j){
						(*arr3)[j] = (char *) malloc(sizeof(tok));
						tok = get_next_token(tokenizer);
						(*arr3)[j] = tok;
					}
				}
			}
		}
	// If more than one angle bracket
		else if (redirectionCounter == 2) {

			if (pipeCounter == 0){		
				*arr2 = (char **) malloc((redirectionPosition2-redirectionPosition1-1)*sizeof(char *));	
				for (j = 0; j < redirectionPosition2-redirectionPosition1-1; ++j){
					tok = get_next_token(tokenizer);
					(*arr2)[j] = (char *) malloc(sizeof(tok));
					(*arr2)[j] = tok;
				}
				
				j = 0;
				// Disgard next angle bracket
				tok = get_next_token(tokenizer);
				*red2 = *tok;
				if (backGroundCounter == 0){
					*arr3 = (char **) malloc((counter-redirectionPosition2-1)*sizeof(char *));	
					while((tok = get_next_token(tokenizer)) != NULL){
						(*arr3)[j] = (char *) malloc(sizeof(tok));
						(*arr3)[j] = tok;
						++j;
					}
				}
				else {
					*arr3 = (char **) malloc((counter-redirectionPosition2-2)*sizeof(char *));
					for (j = 0; j < counter-redirectionPosition2-2; ++j){
						(*arr3)[j] = (char *) malloc(sizeof(tok));
						tok = get_next_token(tokenizer);
						(*arr3)[j] = tok;
					}
				}
			}
			else if (redirectionPosition2 < pipePos){
				*arr2 = (char **) malloc((redirectionPosition2-redirectionPosition1-1)*sizeof(char *));	
				for (j = 0; j < redirectionPosition2-redirectionPosition1-1; ++j){
					tok = get_next_token(tokenizer);
					(*arr2)[j] = (char *) malloc(sizeof(tok));
					(*arr2)[j] = tok;
				}
				
				j = 0;
				// Disgard next angle bracket
				tok = get_next_token(tokenizer);
				*red2 = *tok;
				*arr3 = (char **) malloc((pipePos - redirectionPosition2 - 1)*sizeof(char *));	
				for (j = 0; j < pipePos-redirectionPosition2-1; ++j){
					tok = get_next_token(tokenizer);
					(*arr3)[j] = (char *) malloc(sizeof(tok));
					(*arr3)[j] = tok;
				}
				tok = get_next_token(tokenizer);
				*red3 = *tok;
				if (backGroundCounter == 0){
					*arr4 = (char **) malloc((counter-pipePos-1)*sizeof(char *));
					while((tok = get_next_token(tokenizer)) != NULL){
						(*arr4)[j] = (char *) malloc(sizeof(tok));
						(*arr4)[j] = tok;
						++j;
					}
				}
				else {
					*arr4 = (char **) malloc((counter-pipePos-2)*sizeof(char *));
					for (j = 0; j < counter-pipePos-2; ++j){
						(*arr4)[j] = (char *) malloc(sizeof(tok));
						tok = get_next_token(tokenizer);
						(*arr4)[j] = tok;
					}
				}
			}
			else if (pipePos < redirectionPosition2){
				*arr2 = (char **) malloc((pipePos-redirectionPosition1-1)*sizeof(char *));	
				for (j = 0; j < pipePos-redirectionPosition1-1; ++j){
					tok = get_next_token(tokenizer);
					(*arr2)[j] = (char *) malloc(sizeof(tok));
					(*arr2)[j] = tok;
				}
				
			
				// Disgard next angle bracket
				tok = get_next_token(tokenizer);
				*red2 = *tok;
				*arr3 = (char **) malloc((redirectionPosition2 - pipePos - 1)*sizeof(char *));	
				for (j = 0; j < redirectionPosition2-pipePos-1; ++j){
					tok = get_next_token(tokenizer);
					(*arr3)[j] = (char *) malloc(sizeof(tok));
					(*arr3)[j] = tok;
				}
				tok = get_next_token(tokenizer);
				*red3 = *tok;
				j = 0;
				if (backGroundCounter == 0){
					*arr4 = (char **) malloc((counter -redirectionPosition2-1)*sizeof(char *));	
					while((tok = get_next_token(tokenizer)) != NULL){
						(*arr4)[j] = (char *) malloc(sizeof(tok));
						(*arr4)[j] = tok;
						++j;
					}
				}
				else {
					*arr4 = (char **) malloc((counter-redirectionPosition2-2)*sizeof(char *));
					for (j = 0; j < counter-redirectionPosition2-2; ++j){
						(*arr4)[j] = (char *) malloc(sizeof(tok));
						tok = get_next_token(tokenizer);
						(*arr4)[j] = tok;
					}
				}
			}
		}
	}
	else {
		*arr1 = (char **) malloc(pipePos * sizeof (char *));
		for (j = 0; j < pipePos; ++j){
			tok = get_next_token(tokenizer);
			(*arr1)[j] = (char *) malloc(sizeof tok); 
			(*arr1)[j] = tok;
		}
		tok = get_next_token(tokenizer);
		*red1 = *tok;
		j = 0;
		// Fill in right side of  first angle bracket
		if (redirectionCounter == 0){
			*arr2 = (char **) malloc((counter-pipePos-1)*sizeof(char *));	
			while((tok = get_next_token(tokenizer)) != NULL){
				(*arr2)[j] = (char *) malloc(sizeof(tok));
				(*arr2)[j] = tok;
				++j;
			}
		}
		else if (pipePos < redirectionPosition1){
			*arr2 = (char **) malloc((redirectionPosition1-pipePos-1)*sizeof(char *));	
			for (j = 0; j < (redirectionPosition1-pipePos-1); ++j){
				tok = get_next_token(tokenizer);
				(*arr2)[j] = (char *) malloc(sizeof(tok));	
				(*arr2)[j] = tok;
			}
			tok = get_next_token(tokenizer);
			*red2 = *tok;
			if (redirectionCounter == 1){
				if (backGroundCounter == 0){
					*arr3 = (char **) malloc((counter-redirectionPosition1-1)*sizeof (char*));
					while((tok = get_next_token(tokenizer)) != NULL){
						(*arr3)[j] = (char *) malloc(sizeof(tok));
						(*arr3)[j] = tok;
						++j;
					}
				}
				else {
					*arr3 = (char **) malloc((counter-redirectionPosition1-2)*sizeof(char *));
					for (j = 0; j < counter-redirectionPosition2-2; ++j){
						(*arr3)[j] = (char *) malloc(sizeof(tok));
						tok = get_next_token(tokenizer);
						(*arr3)[j] = tok;
					}
				}
			}
			else if (redirectionCounter == 2){
				*arr3 = (char **) malloc((redirectionPosition2-redirectionPosition1-1)*sizeof(char *));	
				for (j = 0; j < (redirectionPosition2-redirectionPosition1-1); ++j){
					tok = get_next_token(tokenizer);
					(*arr3)[j] = (char *) malloc(sizeof(tok));
					(*arr3)[j] = tok;
				}
				tok = get_next_token(tokenizer);
				*red3 = *tok;
				if (backGroundCounter == 0){
					*arr4 = (char **) malloc((counter-redirectionPosition2-1)*sizeof (char*));
					while((tok = get_next_token(tokenizer)) != NULL){
						(*arr4)[j] = (char *) malloc(sizeof(tok));
						(*arr4)[j] = tok;
						++j;
					}
				}
				else {
					*arr4 = (char **) malloc((counter-redirectionPosition2-2)*sizeof(char *));
					for (j = 0; j < counter-redirectionPosition2-2; ++j){
						(*arr4)[j] = (char *) malloc(sizeof(tok));
						tok = get_next_token(tokenizer);
						(*arr4)[j] = tok;
					}
				}
			}
		}
	}
	
	if (pipeCounter == 0){
		free_tokenizer(tokenizer);
	}
}
