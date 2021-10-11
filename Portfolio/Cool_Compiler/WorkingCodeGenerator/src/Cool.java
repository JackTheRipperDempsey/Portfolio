/* 
 * Driver for Cool compiler.  We'll keep adding to this as we go. 
 *
 */

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory; 
import java_cup.runtime.ComplexSymbolFactory; 

import org.apache.commons.cli.*; // Command line parsing package

import java.nio.file.Files;

public class Cool {
    
    // Command line options
    String sourceFile = ""; 
    static String destinationFile = "";

    // Internal state
    ErrorReport report; 

    boolean DebugMode = false; // True => parse in debug mode 

    // Use this to store the result of the parser - it's our AST
    static RootNode root;
    
    // Run the compiler
    static public void main(String args[]) {
        Cool cool = new Cool();
        
        // Run scanner and parser and collect the root node for the program (sets the static 'root' variable)
        cool.go(args);
        
        // Set up maps of names of classes and variables to their class definition and type
        HashMap<String, Clazz> clazzes = new HashMap<String,Clazz>();
        HashMap<String, Clazz> variables = new HashMap<String,Clazz>();
        HashMap<String, ArrayList<String>> methods = new HashMap<String, ArrayList<String>>();
        
        // Set up symbol tables and names of classes that will get passed between nodes during type checking
        TableManager tableM = new TableManager();
        TypeChecker typeCheck = new TypeChecker(tableM);
        
        // Perform typechecking on abstract syntax tree
        typeCheck.checkTypes(root, clazzes, variables, methods, new ErrorReport());
        
        // Perform code generation on second walk of the AST
        CodeGenerator generator = new CodeGenerator();
        generator.generateCode((RootNode) root, clazzes, methods, destinationFile);
        cool.runGcc(destinationFile);
    }

    // Run scanner and parser after getting files from command line
    public void go(String[] args) {
        report = new ErrorReport(); 
        parseCommandLine(args);
        parseProgram();
    }

    // Preliminary parsing of command line to check for valid input filename and correct number of arguments
    void parseCommandLine(String args[]) {
        try {
            // Command line parsing
            Options options = new Options();
            options.addOption("d", false, "debug mode (trace parse states)");
            
            CommandLineParser  cliParser = new GnuParser();
            CommandLine cmd = cliParser.parse( options, args);
            DebugMode = cmd.hasOption("d");
            
            String[] remaining = cmd.getArgs();
            int argc = remaining.length;
            
            if (argc == 0) {
                report.err("Input file name required");
                System.exit(1);
            }
            
            else {
                sourceFile = remaining[0];
                
                // If an output filename is explicitly provided, check to see if a previous output file of the same name exists
                // If it does, move the old file to a new marked location for comparative purposes
                if (argc > 1){
                    
                    // Inform user that extra command line arguments will be ignored
                    if (argc > 2){
                        report.err("Only 1 input file name and optionally 1 output file name can be given;"+
                                   " ignoring other(s)");
                    }
                    
                    destinationFile = remaining[1];
                    
                    int extensionIndex = destinationFile.lastIndexOf('.');
                    String extensionRemoved = destinationFile.substring(0,extensionIndex);
                    
                    // Ensure that output gets put into the designated folder
                    destinationFile = "./outputs/" + extensionRemoved + ".c";
                    File oldFile = new File(destinationFile);
                    
                    if (oldFile.exists()){
                        Files.move(oldFile.toPath(), new File("./outputs/" + extensionRemoved + "_old.c").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                
                // If an output filename is not explicitly provided, use the name of the source file with a modified extension
                // If an output file with this name already exists, move the old file to a new marked location for comparative purposes
                else {
                    // Need to remove the directory part of the string or else the output file might be created somewhere undesirable (e.g., the 'tests' folder)
                    int directoryIndex = sourceFile.lastIndexOf('/');
                    int extensionIndex = sourceFile.lastIndexOf('.');
                    
                    String extensionRemoved = sourceFile.substring(directoryIndex+1,extensionIndex);
                    
                    // Ensure that output gets put into the designated folder
                    destinationFile = "./outputs/" + extensionRemoved + ".c";
                    File oldFile = new File(destinationFile);
                    
                    if (oldFile.exists()){
                        Files.move(oldFile.toPath(), new File("./outputs/" + extensionRemoved + "_old.c").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
        
        catch (Exception e) {
            report.err("Argument parsing problem");
            report.err(e.toString());
            System.exit(1);
        }
    }

    // Drives main scanning and parsing of the input COOL program
    void parseProgram() { 
        System.out.println("Beginning parse ..."); 
        try {
            ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();
            Scanner scanner = new Scanner (new FileReader ( sourceFile ), symbolFactory);
            parser p = new parser( scanner, symbolFactory);
            
            p.setErrorReport(report); 
            Symbol result;
            
            if (DebugMode) {
                result =  p.debug_parse();
            }
            
            else {
                result = p.parse();
            }
            
            // This assigns the returned root node of a program that has been parsed successfully to the static root variable of this class
            root = (RootNode) result.value;
            System.out.println("Done parsing");
        }
        
        catch (Exception e) {
            report.err("Yuck, blew up in parse/validate phase");
            e.printStackTrace(); 
            System.exit(1);
        }
    }

    void runGcc(String cFile){
        int extensionIndex = cFile.lastIndexOf('.');
        String outputFile = cFile.substring(0,extensionIndex);
        try {
            Runtime sys = Runtime.getRuntime();
            String com = "gcc -o " + outputFile + " " + cFile;
            Process p = sys.exec(com);
        }
        catch (Exception e) {
            report.err("Final compilation of intermediate C code failed");
            e.printStackTrace(); 
            System.exit(1);
        }       
    } 
}
