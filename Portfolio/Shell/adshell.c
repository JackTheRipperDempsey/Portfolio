#define _XOPEN_SOURCE 500
#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>
#include "tokenizer.h"
#include <fcntl.h>
#include "arrayParse.h"
#include "redirectionExecution.h"
#include <sys/wait.h>
#include <sys/types.h>

/* Global variables, including flags for tracking timeouts 
 * and points that can be freed in the event of a timeout 
 * */

// Data structure for storing jobs in linked list
struct listNode
{
int statFlag;
int job;
struct listNode *nextNode;
struct listNode *prevNode;
};

// Flags and character buffers for command line arguments
int cpid = 1;
int foregroundPID = -1;
char * buffer;
char ** argvar;
char ** arr1;
char ** arr2;
char ** arr3;
char ** arr4;
char ** recentBckgrndJob;
char red1 = '\0';
char red2 = '\0';
char red3 = '\0';

// Counters and position markers
int redirectionPosition1 = 0;
int redirectionPosition2= 0;
int redirectionCounter = 0;
int pipeCounter = 0;
int pipePos = 0;
int backGroundCounter  = 0;
int numProcessGroups = 2;

//Process IDs
int recentBckgrndPID;
int recentBckgrndPGID;
int stoppedPID;
struct listNode *statRoot;

// Signal handler for signals sent by the user
void usrHandler(int signum){
	free(buffer);
	redirectionPosition1 = 0;
	redirectionPosition2= 0;
	redirectionCounter = 0;
	backGroundCounter = 0;
	pipePos = 0;
	pipeCounter = 0;
	cpid = 1;
	red1 = '\0';
	red2 = '\0';
	red3 = '\0';
} 

// Signal handler for signals sent by child processes
void childHandler(int signum){
	/*sigset_t hand;
	sigemptyset(&hand);
	sigaddset(&hand,SIGCHLD);
	sigprocmask(SIG_BLOCK,&hand,NULL); */
	signal(SIGCHLD,SIG_IGN);
	signal(SIGUSR1,usrHandler);
	int status;
	int pid;
	if (foregroundPID == -1){
		pid = waitpid(recentBckgrndPID, &status,WNOHANG|WUNTRACED);
	}
	
	if (WIFEXITED(status) || WIFSIGNALED(status)){
		if (pid == recentBckgrndPID){
			--numProcessGroups;
			struct listNode *nextJob = malloc(sizeof(struct listNode));
			nextJob->job = recentBckgrndPID;
			nextJob->statFlag = 1;
			nextJob->prevNode = statRoot;
			if (statRoot->nextNode == NULL){
				statRoot->nextNode = nextJob;
			}
			else {
				statRoot->nextNode->prevNode=nextJob;
				nextJob->nextNode = statRoot->nextNode;
				statRoot->nextNode = nextJob;
			}	
		}
	}
	//sigprocmask(SIG_UNBLOCK,&hand,NULL);
}

// Signal handler for signals killing jobs
void termCatcher(int signum){
		signal(signum,SIG_DFL);
		killpg(2,signum);
}

/* The main shell function
 * */
int main(int argc, char** argv){
	// Declare flags
	int exitCondition;
	int status;
	int execute = 0;
	int errorCode = 0;
	int i;
	int readError = 0;
	TOKENIZER *tokenizer;
	char *tok;
	recentBckgrndPID = -2;
	recentBckgrndPGID = -2;
	stoppedPID = -2;
	int shellPID = getpid();
	
    // Set up signal handlers
	signal(SIGCHLD,childHandler);
	signal(SIGTERM, termCatcher);
	signal(SIGINT, termCatcher);
	signal(SIGSTOP, termCatcher);
	signal(SIGTSTP, SIG_IGN);
	
	statRoot =  (struct listNode *) malloc ( sizeof(struct listNode) );
	statRoot->job = 0;
	statRoot->statFlag = 0;
	statRoot->nextNode = NULL; 
	statRoot->prevNode = NULL;
	// Initiate main infinite loop for the shell
	while(1) {
		signal(SIGUSR1,usrHandler);
        // Check status of current  jobs
		if (statRoot->nextNode != NULL){
			struct listNode *currNode = malloc(sizeof (struct listNode));
			currNode = statRoot;
			while(currNode->nextNode != NULL){
				if (currNode->prevNode!=statRoot){
					free(currNode->prevNode);
				}
				currNode = currNode->nextNode;
				
                // Report status of recently completed or stopped jobs
				if (currNode->statFlag == 1){		
					if (currNode->job == recentBckgrndPID){
						printf("Finished: ");
						i = 0;
						while(recentBckgrndJob[i]!=NULL){
							printf("%s ", recentBckgrndJob[i]);
							++i;
						} 
						printf("\n");
						recentBckgrndPID = -2;
						recentBckgrndPGID = -2;
						
					}

				}
				else if (currNode->statFlag == 2){
					printf("\n");
					printf("Stopped: ");
					i = 0;		

						while(recentBckgrndJob[i]!=NULL){
							printf("%s ", recentBckgrndJob[i]);
							++i;
						}
						printf("\n");
					
				}
				else if (currNode->statFlag == 3){
					i = 0;
					if (currNode->job == recentBckgrndPID){
						printf("Running: ");
						while(recentBckgrndJob[i]!=NULL){
							printf("%s ", recentBckgrndJob[i]);
							++i;
						}
						printf("\n");
					}
					
				}
				
				if (currNode->nextNode != NULL){
					statRoot->nextNode=currNode->nextNode;
					free(currNode);
				}
				else {
					statRoot->nextNode=NULL;
				}
				
			}
		}
        // Read in next command
		write(1,"prompt# ",8);
		buffer = (char *) malloc(1024);
		readError = read(0,buffer,1024);
		if (readError == 1){
			write(2,"Unable to read command successfully\n",36);
		} 
		// Strip out newline character in buffer
		for (i = 0; i < 1024; ++i){
			if (buffer[i] == '\n'){
				buffer[i] = '\0';
				break;
			}
		}

		int counter = 0;
		
		// Count number of tokens
		tokenizer = init_tokenizer( buffer );
		while( (tok = get_next_token( tokenizer )) != NULL ) {
			++counter;
		}

		free_tokenizer(tokenizer);
		int lastToken = counter;
		int flag = 0;
		argvar = (char **) malloc((1+counter) * sizeof (char *));
		// Get tokens, store in array
		counter = 0;
		tokenizer = init_tokenizer( buffer );
		
		while( (tok = get_next_token( tokenizer )) != NULL ) {
			argvar[counter] = (char *) malloc(sizeof tok);				
			argvar[counter] = tok;
			
			// Determine if there is a need for redirection
			if ((*argvar[counter] == '>') || (*argvar[counter] == '<')){
				++redirectionCounter;
				if (redirectionCounter == 1){
					redirectionPosition1 = counter;
				}
				else if (redirectionCounter == 2){
					redirectionPosition2 = counter;
				}
			}	
			else if (*argvar[counter] == '|'){
				++pipeCounter;
				if (pipeCounter == 1){
					pipePos = counter;
				}
			}
			else if(*argvar[counter] == '&'){
				if (counter < (lastToken-1)){
					write(2,"Ampersand only recognized when last token\n",42);
					flag = 1;
				}
				else if (counter == (lastToken-1)){
					backGroundCounter = 1;
				}
			}	
				++counter;
		}

		if (flag == 1){
			kill(shellPID,SIGUSR1);
			continue;
		}
		//free_tokenizer(tokenizer);
		if (backGroundCounter > 0){
			argvar[counter-1] = NULL;
		}
		argvar[counter] = NULL;
        // If command does not have arguments...
		if (argvar[1] == NULL){
            // Report on background jobs
			if ((argvar[0][0]=='b')&&(argvar[0][1]=='g')&&(argvar[0][2]=='\0')){
				if (stoppedPID == -2){
					write(2,"No recently backgrounded job\n",24);
				}
				else {
					recentBckgrndPID = stoppedPID;
					killpg(getpgid(stoppedPID),SIGCONT);
					
					struct listNode *nextJob = malloc(sizeof(struct listNode));
					nextJob->job = recentBckgrndPID;
					nextJob->statFlag = 3;
					nextJob->prevNode = statRoot;
					if (statRoot->nextNode == NULL){
						statRoot->nextNode = nextJob;
					}
					else {
						statRoot->nextNode->prevNode=nextJob;
						nextJob->nextNode = statRoot->nextNode;
						statRoot->nextNode = nextJob;
					}
				}
				
				kill(shellPID,SIGUSR1);
			
				continue;
			}
            // Move most recent background job into foreground
			else if ((argvar[0][0]=='f')&&(argvar[0][1]=='g')&&(argvar[0][2]=='\0')){
				if (recentBckgrndPID == -2){
					write(2,"No recently backgrounded job\n",29);
				}
				else {
					i = 0;
					while(recentBckgrndJob[i]!=NULL){
						printf("%s ",recentBckgrndJob[i]);
						++i;
					}
					printf("\n");
					setpgid(recentBckgrndPID,2);
					foregroundPID = recentBckgrndPID;
					killpg(2,SIGCONT);
					if (stoppedPID != -2){
						stoppedPID = -2;
					}
					tcsetpgrp(0,2);
					signal(SIGTSTP,SIG_IGN);
					cpid = fork();
					if (cpid == 0){
						signal(SIGTSTP,SIG_DFL);
						pause();
					}
					else {
						exitCondition = waitpid(cpid,&status,WUNTRACED);
						if (WIFSTOPPED(status)){
							stoppedPID = recentBckgrndPID;
							struct listNode *nextJob = malloc(sizeof(struct listNode));
							nextJob->job = recentBckgrndPID;
							nextJob->statFlag = 2;
							nextJob->prevNode = statRoot;
							if (statRoot->nextNode == NULL){
							statRoot->nextNode = nextJob;
							}
							else {
								statRoot->nextNode->prevNode=nextJob;
								nextJob->nextNode = statRoot->nextNode;
								statRoot->nextNode = nextJob;
							}	
						}		
					}
					recentBckgrndPID = -2;
					recentBckgrndPGID = -2;
				}
				signal(SIGTSTP,SIG_IGN);
				kill(shellPID,SIGUSR1);
				continue;
			}
		}
        // Determine validity of pipelining and redirection
		if (redirectionCounter > 2){
			write(2, "Too many redirection tokens\n",28);
			kill(shellPID,SIGUSR1);
			continue;
		}
		else if (pipeCounter > 1){
			write(2, "Too many pipeline stages\n",25);
			kill(shellPID,SIGUSR1);
			continue;
		}
		// Divide arguments based on relation to angle bracket(s) or pipelines
		else if ((redirectionCounter > 0) || (pipeCounter > 0)) {
			arrayParse(buffer, &arr1, &arr2, &arr3, &arr4,pipeCounter, pipePos, 
				redirectionCounter, redirectionPosition1, redirectionPosition2,
				 counter, &red1, &red2, &red3, backGroundCounter);
				 
			free_tokenizer(tokenizer); 
		}
		if (redirectionCounter > 1){
			if ((red1 != '\0')&&((red1 == red2) || (red1 == red3))){
				if (red1 == '<'){
					write(2,"Too many redirections of stdin\n",31);
					kill(shellPID,SIGUSR1);
					continue;
				}
				else if (red1 == '>'){
					write(2,"Too many redirections of stdout\n",32);
					kill(shellPID,SIGUSR1);
					continue;
				}
			}
			else if ((red2 != '\0')&&(red2 == red3)){
					if (red2 == '<'){
					write(2,"Too many redirections of stdin\n",31);
					kill(shellPID,SIGUSR1);
					continue;
				}
				else if (red2 == '>'){
					write(2,"Too many redirections of stdout\n",32);
					kill(shellPID,SIGUSR1);
					continue;
				}
			}
			else if((redirectionCounter > 0) && (pipeCounter > 0)){
				if (pipePos > redirectionPosition1){
					if ((red1 == '>') || (pipePos > redirectionPosition2)){
						write(2,"Too many redirections of stdout: file and pipeline\n",51);
						kill(shellPID,SIGUSR1);
						continue;
					}
				}
			}
		}
		tcsetpgrp(0,getpgid(getpid()));
		// Block signals from other child processes
		if (backGroundCounter == 0){
			sigset_t set;
			sigemptyset(&set);
			sigaddset(&set,SIGCHLD);
			sigprocmask(SIG_BLOCK,&set,NULL);
		}
		
		else {
			recentBckgrndJob = (char **) malloc(counter * sizeof(char *));
			i = 0;
			while (argvar[i] != NULL){
				recentBckgrndJob[i] = (char *) malloc(sizeof(argvar[i]));
				recentBckgrndJob[i] = argvar[i];
				++i;
			}
		}
			
        // Fork. make node for job, and execute command
		cpid = fork();
		if (cpid != 0){
            // In parent node, update number of process groups and current job information
			if (backGroundCounter > 0) {
				int pgid = ++numProcessGroups;
				setpgid(cpid,pgid);
				recentBckgrndPGID = pgid;
				recentBckgrndPID = cpid;
				tcsetpgrp(0,getpgid(getpid()));
				struct listNode *nextJob = malloc(sizeof(struct listNode));
				nextJob->job = recentBckgrndPID;
				nextJob->statFlag = 3;
				nextJob->prevNode = statRoot;
				if (statRoot->nextNode == NULL){
					statRoot->nextNode = nextJob;
				}
				else {
					statRoot->nextNode->prevNode=nextJob;
					nextJob->nextNode = statRoot->nextNode;
					statRoot->nextNode = nextJob;
				}	
			}
			else {
				foregroundPID = cpid;
				setpgid(cpid,2);
				tcsetpgrp(0,2);
			}
		}
        // In child node, execute command
		if (cpid == 0) {	
			signal(SIGTSTP,SIG_DFL);
			int pid = getpid();
			int pgid = getpgid(pid);
			setpgid(pid,pgid);
			if (backGroundCounter == 0) {
				tcsetpgrp(0,getpgid(pid));	
			}
			
            // Redirect or set up pipeline if necessary
			if ((redirectionCounter > 0) || (pipeCounter > 0)){
				errorCode = redirectionExecution(arr1, arr2, arr3, arr4, redirectionCounter, redirectionPosition1,
					redirectionPosition2, pipeCounter, pipePos, red1, red2, red3);
			}
			
			else if ((redirectionCounter == 0) && pipeCounter == 0){
				execute = execvp(argvar[0],argvar);
			}
			
			if (execute < 0) {
				perror("Command failed to execute\n");
			}
		}
		else {
			//Wait for child job to finish
            // Add to background job list, setting up job-node if necessary
			if (backGroundCounter == 0){
				exitCondition = waitpid(foregroundPID,&status,WUNTRACED);
				if (WIFSTOPPED(status)){
					recentBckgrndJob = (char **) malloc(counter * sizeof(char *));
					i = 0;
					while (argvar[i] != NULL){
						recentBckgrndJob[i] = (char *) malloc(sizeof(argvar[i]));
						recentBckgrndJob[i] = argvar[i];
						++i;
					}
					struct listNode *nextJob = malloc(sizeof(struct listNode));
					nextJob->job = cpid;
					stoppedPID = cpid;
					nextJob->statFlag = 2;
					nextJob->prevNode = statRoot;
					if (statRoot->nextNode == NULL){
					statRoot->nextNode = nextJob;
					}
					else {
						statRoot->nextNode->prevNode=nextJob;
						nextJob->nextNode = statRoot->nextNode;
						statRoot->nextNode = nextJob;
					}			
				}
                // Unblock child signals
				foregroundPID = -1;
				sigset_t set;
				sigemptyset(&set);
				sigaddset(&set,SIGCHLD);
				sigprocmask(SIG_UNBLOCK,&set,NULL);
			}
            // Wait for child job to finished
			else {
				exitCondition = waitpid(0,&status,WNOHANG|WUNTRACED);
					if (WIFSTOPPED(status)){
					struct listNode *nextJob = malloc(sizeof(struct listNode));
					nextJob->job = cpid;
					stoppedPID = cpid;
					nextJob->statFlag = 2;
					nextJob->prevNode = statRoot;
					if (statRoot->nextNode == NULL){
					statRoot->nextNode = nextJob;
					}
					else {
						statRoot->nextNode->prevNode=nextJob;
						nextJob->nextNode = statRoot->nextNode;
						statRoot->nextNode = nextJob;
					}			
				}	
			}
            // Reset group PID
			tcsetpgrp(0,getpgid(getpid()));	
		} 
		free(buffer);
		
		/* while (argvar[j] != NULL){
			free(argvar[j]);
			++j;
		} */
		
	/*	if (argvar != NULL) {
			free(argvar);
		} */
        
		// Freeing arr1 causes the line allocating memory to buffer
		// to segfault for unknown reasons
		
        if (arr1 != NULL){
			/* j = 0;
			while(arr1[j] != NULL){
				printf("Line 236\n");
				free(arr1[j]);
				++j;
			} 
			printf("Line 240\n"); */
		//	free(arr1);
		} 
		if (arr2 != NULL){
		/*	j = 0;
			while(arr2[j] != NULL){
				printf("Line 246\n");
				free(arr2[j]);
				++j;
			}
			printf("Line 249\n"); */
		//	free(arr2);
		}
		if (arr3 != NULL){
		/*	j = 0;
			while(arr3[j] != NULL){
				printf("Line 256\n");
				free(arr3[j]);
				++j;
			}
			printf("Line 260\n"); */
		//	free(arr3);
		}
        
        // Reset counters
		redirectionPosition1 = 0;
		redirectionPosition2= 0;
		redirectionCounter = 0;
		execute = 0;
		pipePos = 0;
		pipeCounter = 0;
		backGroundCounter = 0;
		cpid = 1;
		red1 = '\0';
		red2 = '\0';
		red3 = '\0';
		fflush(NULL);
	}
	return 0;
}
