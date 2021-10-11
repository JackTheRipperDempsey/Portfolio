/*
 * The ASTNode class is the foundation of our abstract syntax tree. 
 * Here we have the abstract class for a basic ASTNode, as well as 
 * implementations of that for different kinds of nodes in our tree 
 * (e.g., int literal nodes, method nodes, etc).
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;

abstract public class ASTNode {
	
	// typeError records whether there was a type error reported in checking this node
	protected boolean typeError;
	// myClass provides a record for type assigned to the node
	protected Clazz myClass;
	// myClassName is used to return the name of the Clazz attached to a node
	protected String nameOfNodeType;
    	
	// parentNode is set upon creation of the node
    protected ASTNode parentNode = null;

	protected static HashSet<String> restrictedIdentifiers = new HashSet<String>(Arrays.asList("this", "size_t", "fpos_t", "wchar_t", "div_t", "ldiv_t", "stdin", "stdout", "stderr", "char", "bool", "int", "short", "float", "double", "struct", "unsigned", "const", "void", "signed", "long", "for", "break", "continue", "switch", "auto", "volatile", "union", "static", "return", "extern", "do", "default", "enum", "goto", "register", "sizeof", "typedef"));
    
	/* whatKindofNode is different than nameOfNodeType in that
	 it is used to record whether the node is a root, a class, etc.
	 
	 This is needed because of the need for some nodes to trace their 
	 way back to the calling method or class */
	protected String whatKindOfNode;	
    
    public ASTNode(String type){
        nameOfNodeType = type;
        typeError = false;
    }
	
	// This is our method for typechecking our node and by extension all of its children - used on the first walk of the AST
	abstract public Clazz whatsMyType(TypeError t,TableManager manager, HashMap<String,Clazz> table, ErrorReport report);
	
    // This is our method for generating C code - used on the second walk of the AST
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
	}
    
    // This is purely for readability - it shouldn't affect the behavior of the output .c file
    protected StringBuilder addTabs(int scopeDepth){
        StringBuilder tabs = new StringBuilder("");
        for (int i = 0; i < scopeDepth; ++i){
            tabs.append("\t");
        }
    
        return tabs;
    }

    // This flags a node as invalidly typed
	public boolean isError(){
		return typeError;
	}
    
    public Clazz getType(){
        return myClass;
    }
	
	public void setType(){
		myClass = classes.get(nameOfNodeType);
	}
	
	public String getTypeName(){
		return nameOfNodeType;
	}
    
	public String getWhatKindOfNode(){
		return whatKindOfNode;
	}
	
	public void setParent(ASTNode a){
		parentNode = a;
	}
	
	public ASTNode getParent(){
		return parentNode;
	}
	
    // This is used for variable and assignment nodes
	public String getVariableName(){
		return "no variable in this node; this was returned in error";
	}
    
    // This is used for variable and assignment nodes, where the stated type of the variable might not match the actual checked type of the variable node
    // (This can happen even in valid programs, where the declared type is a superclass of the actual type)
    public String getDeclaredTypeName(){
        return "declared type for this node; this was returned in error";
	}

	protected String cValueOrReference = null;
	
	public String getCValueForNode(){
		return cValueOrReference;
	}

	protected static HashMap<String,Clazz> classes = new HashMap<String, Clazz>();

	public void setClassMap(HashMap<String,Clazz> map){
		classes = map;
	}

	protected static int numberOfTempVariables = 0;

	protected static String getNextTempVariable(String whatKind){

		String nextTempVariable = "_temp_" + numberOfTempVariables + "_" + whatKind;
		++numberOfTempVariables;

		return nextTempVariable;
	}

	protected static boolean inConstructor = false;
	protected static String constructorTempVariable = null;

	protected static void setInConstructor(String tempVar){
		inConstructor = true;
		constructorTempVariable = tempVar;
	}

	protected static void setNotInConstructor(){
		inConstructor = false;
	}

	public static String generateInitializationForObject(StringBuilder tabs, String variableName, String typeName){

		StringBuilder initializationText = new StringBuilder();

		initializationText.append("\n\n" + tabs + "_Shell* " + variableName + " = " + typeName + "_" + typeName + "()" + ";");

		return initializationText.toString();
	}

	public static String generateMemoryAllocationOnlyForObject(StringBuilder tabs, String variableName, String typeName){

		StringBuilder initializationText = new StringBuilder();

		initializationText.append("\n\n" + tabs + "_Shell* " + variableName + " = " + " _newShellReference();");

		ArrayList<String> inheritedTypes = new ArrayList<String>();
		inheritedTypes.add(typeName);
		Clazz currentClass = classes.get(typeName);        

		while (currentClass.getParent()!= null){
			currentClass = currentClass.getParent();
			inheritedTypes.add(currentClass.getClassName());
		}

		initializationText.append("\n" + tabs + variableName + "->numTypes" + " = " + inheritedTypes.size()+ ";");

		initializationText.append("\n" + tabs + variableName + "->type" + " = malloc(" + inheritedTypes.size() + "* sizeof(_TYPE));");

		// Append class type and all inherited types
		for (int i = 0; i < inheritedTypes.size(); ++i){
			initializationText.append("\n" + tabs + variableName + "->type[" + i + "] = _" + inheritedTypes.get(i).toUpperCase() + ";");			
		}

		initializationText.append("\n\n" + tabs + variableName + "->reference" + " = " + " malloc(sizeof(" + typeName + "));");
		initializationText.append("\n\n" + tabs + variableName + "->init" + " = " + " false;");
		initializationText.append("\n\n" + tabs + variableName + "->numMethods" + " = _" + typeName.toUpperCase() + "_NumMethods;");
		initializationText.append("\n\n" + tabs + variableName + "->methodNames" + " = _" + typeName.toUpperCase() + "_MethodNames;");
		initializationText.append("\n\n" + tabs + variableName + "->methodPointers" + " = _" + typeName.toUpperCase() + "_MethodPointers;");
		initializationText.append("\n\n" + tabs + variableName + "->defaultCopyMethod" + " = _" + typeName.toUpperCase() + "_DefaultCopyMethod;");

		return initializationText.toString();
	}

	public static String generateReferenceForObject(String variableName, String typeName){

		StringBuilder generatedText = new StringBuilder();

		generatedText.append("((" + typeName + " *) (" + variableName + "->reference))");

		return generatedText.toString();
	}

	public static String generateDereferencedReferenceForObject(String variableName, String typeName){

		StringBuilder generatedText = new StringBuilder();

		generatedText.append("*(((" + typeName + " *) (" + variableName + "->reference))->_data)");

		return generatedText.toString();
	}

	public static String generateTabbedAssignmentLine(StringBuilder tabs, String lValue, String lValueType, boolean lValueDereference, String rValue, String rValueType, boolean rValueDereference, boolean assignType){

		String tabString = tabs.toString();

		String insertLValue;
		String insertRValue;

		if (rValueDereference){
			insertLValue = lValue + "->reference";
			insertRValue = rValue + "->reference";
		}

		else {
			insertLValue = generateDereferencedReferenceForObject(lValue, lValueType);
			insertRValue = rValue;
		}

		StringBuilder generatedText = new StringBuilder();

		generatedText.append("\n\n" + tabString + insertLValue  + " = " + insertRValue + ";");

		if (assignType){
			generatedText.append("\n" + tabString + lValue + "->numTypes"  + " = " + rValue + "->numTypes" + ";");
			generatedText.append("\n" + tabString + lValue + "->type"  + " = " + rValue + "->type" + ";");	
			generatedText.append("\n" + tabString + lValue + "->init"  + " = " + rValue + "->init" + ";");
			generatedText.append("\n" + tabString + lValue + "->numMethods"  + " = " + rValue + "->numMethods" + ";");
			generatedText.append("\n" + tabString + lValue + "->methodNames"  + " = " + rValue + "->methodNames" + ";");
			generatedText.append("\n" + tabString + lValue + "->methodPointers"  + " = " + rValue + "->methodPointers" + ";");
			generatedText.append("\n" + tabString + lValue + "->defaultCopyMethod"  + " = " + rValue + "->defaultCopyMethod" + ";");
		}

		return generatedText.toString();
	}

	public static String getValidIdentifier(String identifier, HashMap<String, ArrayList<String>> resolvedCollisions, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, boolean silenceIfNecessary){
		String validIdentifier;
		if (restrictedIdentifiers.contains(identifier)){
			validIdentifier = "_" + identifier;
		}
		else {
			validIdentifier = identifier; 
		}

		if (silenceIfNecessary)
			silenceIdentifier(validIdentifier, resolvedCollisions, classMemberVariables, scopeVariables);

		if (resolvedCollisions.containsKey(validIdentifier)){
			ArrayList<String> silencedChain = resolvedCollisions.get(identifier);
			validIdentifier = silencedChain.get(silencedChain.size()-1);
		} 

		return validIdentifier;
	}

	public static void silenceIdentifier(String identifier, HashMap<String, ArrayList<String>> resolvedCollisions, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables){

		ArrayList<String> allVariables = new ArrayList<String>();
		allVariables.addAll(classMemberVariables);
		for (ArrayList<String> scope : scopeVariables){
			allVariables.addAll(scope);
		}

		int numInstances = 0;

	//	System.out.println("All variables in scope: ");
		for (String variable : allVariables){
	//		System.out.println(variable);
			if (variable.equals(identifier)){
				++numInstances;
			}
		}
		
		if (numInstances <= 1){
			return;
		}

	//	System.out.println("silencing " + identifier);

		if (resolvedCollisions.containsKey(identifier)){
	//		System.out.println("adding to silenced chain " + identifier);

			ArrayList<String> silencedChain  = resolvedCollisions.get(identifier);
			String lastInChain = silencedChain.get(silencedChain.size()-1);
			silencedChain.add("_tag" + lastInChain);
			resolvedCollisions.put(identifier, silencedChain);
		}

		else {
			ArrayList<String> silencedChain = new ArrayList<String>();
			silencedChain.add("_tag_" + identifier);
			resolvedCollisions.put(identifier, silencedChain);
		}
	}

	public static void retrieveSilencedIdentifier(String identifier, HashMap<String, ArrayList<String>> resolvedCollisions){
		if (resolvedCollisions.containsKey(identifier)){

		//	System.out.println("retrieving silenced version of " + identifier);

			ArrayList<String> silencedChain = resolvedCollisions.get(identifier);
			if (silencedChain.size() > 1) {
				silencedChain.remove(silencedChain.size()-1);
				resolvedCollisions.put(identifier, silencedChain);
			}
			else {
				resolvedCollisions.remove(identifier);
			}
			
		}
	}

}