#!/bin/bash
set_base=false
while getopts ":t" opt;
do
    case $opt in
        t)
            set_base=true;
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            exit 1
            ;;
    esac
done

for test in ./tests/*
do
	if [[ "${test}" =~ .cool$ ]]
	then
        test_name=$(basename -- "$test")
        test_name="${test_name%.*}"
        output_redir="./outputs/redirected_std_out_and_err/${test_name}_std_out"
        err_redir="./outputs/redirected_std_out_and_err/${test_name}_std_err"
        if [ "$set_base" = true ]
        then
            output_redir="./outputs/redirected_std_out_and_err/${test_name}_pre_mod_std_out"
            err_redir="./outputs/redirected_std_out_and_err/${test_name}_pre_mod_std_err"
        fi
        java -cp .:../lib/commons-cli-1.2.jar:../lib/java-cup-11b-runtime.jar:../lib/commons-lang-2.6.jar:../lib/java-cup-11b.jar/ Cool "${test}" > "${output_redir}" 2> "${err_redir}"
	fi
done
