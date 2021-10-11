#!/bin/bash
for file in ./outputs/redirected_std_out_and_err/*
do
	oldfile="$(basename -- $file)"
	if [[ "${oldfile}" =~ _pre_mod_std_err ]]
	then
		length=${#oldfile}
		newfile="${oldfile:0:($length-16)}_std_err"
        echo "Differences between ${oldfile} and ${newfile}"
		diff ./outputs/redirected_std_out_and_err/$oldfile ./outputs/redirected_std_out_and_err/$newfile
	fi
done
