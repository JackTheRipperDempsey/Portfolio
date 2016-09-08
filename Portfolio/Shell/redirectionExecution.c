#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <unistd.h>
#include <fcntl.h>
#include "redirectionExecution.h"

// Code consisting of a single function that determines creates the forks necessary for any pipelining and redirection required by a command
int redirectionExecution(char ** arr1, char ** arr2, char ** arr3, char ** arr4, 
	const int redirectionCounter, const int redirectionPosition1, const int redirectionPosition2, 
	const int pipeCounter, const int pipePos, char red1, char red2, char red3){
	int outputfd;
	int inputfd;
	int pipefd[2];
	int cpid = 1;
	int execute  = 0;
	int status;
	if ((redirectionCounter > 0) && (pipeCounter == 0)){
		if (redirectionCounter == 1){
			if (red1 == '>'){
				outputfd = open(arr2[0], O_WRONLY | O_CREAT, 0644); 
				dup2(outputfd,1);
				close(outputfd);
				}
			else if (red1 == '<'){
				inputfd = open(arr2[0], O_RDONLY);
				dup2(inputfd,0);
				close(inputfd);
			}
		}
		else if (redirectionCounter == 2){
			if ((red1 == '>') && (red2 == '<')){
				outputfd = open(arr2[0], O_WRONLY | O_CREAT, 0644); 
				dup2(outputfd,1);
				close(outputfd);
				inputfd = open(arr3[0], O_RDONLY);
				dup2(inputfd,0);
				close(inputfd);
			}
			else if ((red1 == '<') && (red2 == '>')){
				outputfd = open(arr3[0], O_WRONLY | O_CREAT, 0644); 
				dup2(outputfd,1);
				close(outputfd);
				inputfd = open(arr2[0], O_RDONLY);
				dup2(inputfd,0);
				close(inputfd);
			}
		}
		execute = execvp(arr1[0],arr1);
	}
	else if ((pipeCounter == 1) && (redirectionCounter == 0)){
		if (pipe(pipefd)==-1){
			perror("Error in creating pipe\n");
			return -1;
		}
		cpid = fork();
		if (cpid == 0){
			close(pipefd[0]);
			dup2(pipefd[1],1);
			execute = execvp(arr1[0],arr1);
		}
		else {
			wait(&status);
			close(pipefd[1]);
			dup2(pipefd[0],0);
			execute = execvp(arr2[0],arr2);
		}
	}
			
	else if ((pipeCounter == 1) && (redirectionCounter > 0)){
		if (redirectionPosition1 < pipePos){
			if (red1 == '<'){
				inputfd = open(arr2[0], O_RDONLY);
				dup2(inputfd,0);
				close(inputfd);
			}
			else if (red1 == '>'){
				write(2,"Too many redirections of stdout: file and pipeline\n",51);
				return 0;
			}
			if (redirectionCounter == 2){
				if (pipePos > redirectionPosition2){
					write(2,"Too many redirections of stdout: file and pipeline\n",51);
					return 0;
				}
				else if (redirectionPosition2 > pipePos){
					if (red3 == '<'){
						if (pipe(pipefd)==-1){
							perror("Error in creating pipe\n");
							return -1;
						}
						cpid = fork();
						if (cpid == 0){
							inputfd = open(arr4[0], O_RDONLY);
							dup2(inputfd,0);
							close(inputfd);
							close(pipefd[0]);
							dup2(pipefd[1],1);
							execute = execvp(arr1[0],arr1);
						}
						else {
							wait(&status);
							close(pipefd[1]);
							dup2(pipefd[0],0);
							execute = execvp(arr3[0],arr3);
						}
					}
							
					else if (red3 == '>'){
						if (pipe(pipefd)==-1){
							perror("Error in creating pipe\n");
							return -1;
						}
						cpid = fork();
						if (cpid == 0){
							close(pipefd[0]);
							dup2(pipefd[1],1);
							execute = execvp(arr1[0],arr1);
						}
						else {
							wait(&status);
							outputfd = open(arr4[0], O_WRONLY | O_CREAT, 0644); 
							dup2(outputfd,1);
							close(outputfd);
							close(pipefd[1]);
							dup2(pipefd[0],0);
							execute = execvp(arr3[0],arr3);
						}
					}
				}
			}
		}
		else if (pipePos < redirectionPosition1){
			if (redirectionCounter == 1){
				if (red2 == '<'){
					cpid = fork();
					if (cpid == 0){
						inputfd = open(arr3[0], O_RDONLY);
						dup2(inputfd,0);
						close(inputfd);
						close(pipefd[0]);
						dup2(pipefd[1],1);
						execute = execvp(arr1[0],arr1);
					}
					else {
						wait(&status);
						close(pipefd[1]);
						dup2(pipefd[0],0);
						execute = execvp(arr2[0],arr2);
					}
				}
				else if (red2 == '>'){
					cpid = fork();
					if (cpid == 0){
						close(pipefd[0]);
						dup2(pipefd[1],1);
						execute = execvp(arr1[0],arr1);
					}
					else {
						wait(&status);
						outputfd = open(arr3[0], O_WRONLY | O_CREAT, 0644); 
						dup2(outputfd,1);
						close(outputfd);
						close(pipefd[1]);
						dup2(pipefd[0],0);
						execute = execvp(arr2[0],arr2);
					}
				}
			}
			else if (redirectionCounter == 2){
				if (red2 == '<' && red3 == '>'){
					cpid = fork();
					if (cpid == 0){
						inputfd = open(arr3[0], O_RDONLY);
						dup2(inputfd,0);
						close(inputfd);
						close(pipefd[0]);
						dup2(pipefd[1],1);
						execute = execvp(arr1[0],arr1);
					}
					else {
						wait(&status);
						outputfd = open(arr4[0], O_WRONLY | O_CREAT, 0644); 
						dup2(outputfd,1);
						close(outputfd);
						close(pipefd[1]);
						dup2(pipefd[0],0);
						execute = execvp(arr2[0],arr2);
					}
				}
				if (red2 == '>' && red3 == '<'){
					cpid = fork();
					if (cpid == 0){
						inputfd = open(arr4[0], O_RDONLY);
						dup2(inputfd,0);
						close(inputfd);
						close(pipefd[0]);
						dup2(pipefd[1],1);
						execute = execvp(arr1[0],arr1);
					}
					else {
						wait(&status);
						outputfd = open(arr3[0], O_WRONLY | O_CREAT, 0644); 
						dup2(outputfd,1);
						close(outputfd);
						close(pipefd[1]);
						dup2(pipefd[0],0);
						execute = execvp(arr2[0],arr2);
					}
				}
			}
		}
	}

	if (execute < 0) {
		perror("Command failed to execute\n");
		return -1;
	}
	return 0;
}
