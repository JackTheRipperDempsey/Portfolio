#!/bin/bash
for file in ./outputs/*
do
	oldfile="$(basename -- $file)"
	if [[ "${oldfile}" =~ _old.c$ ]]
	then
		length=${#oldfile}
		newfile="${oldfile:0:($length-6)}.c"
        echo "Differences between ${oldfile} and ${newfile}"
		diff ./outputs/$oldfile ./outputs/$newfile
	fi
done
