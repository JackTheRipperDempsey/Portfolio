import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashMap;

// Implementation for method node
class MethodNode extends ASTNode {
	
	// Here, myClass doubles as the return type for the method
	
	private LinkedHashMap<String,String> arguments;
    private ArrayList<String> argNames;
    private ArrayList<String> argTypeNames;
	private ArrayList<ASTNode> body;
	private String methodName;
	private ArrayList<String> thisVariables;
	
	public String getMethodName(){
		return methodName;
	}
    
    // Get the names of the arguments for our method
    public ArrayList<String> getArgNames(){
        return argNames;
    }
    
    // Get the type names of the arguments for our method
    public ArrayList<String> getArgTypes(){
        return argTypeNames;
    }
	
	public MethodNode(String type, ArrayList<ASTNode> statements, 
			ArrayList<String> argPackedList, String name){
		super(type);
		
		// System.out.println("New method node");
        
        arguments = new LinkedHashMap<String, String>();
        argNames = new ArrayList<String>();
        argTypeNames = new ArrayList<String>();
        
        // The reason that we do not simply build the map with CUP in the parser is that that prevents from checking against the reuse of identifiers in the argument list
        // i.e., the entry in the map would simply be rewritten for each use of that identifier
        for (int i = 0; (i+1) < argPackedList.size(); i+=2){
            String argName = argPackedList.get(i);
            String argTypeName = argPackedList.get(i+1);
            argNames.add(argName);
            argTypeNames.add(argTypeName);
            arguments.put(argName,argTypeName);
        }
        
		body = statements;
		methodName = name;
		whatKindOfNode = "method";

	}
    
    // Typecheck method node
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking method node");
        
        // Open a new inner scope in the symbol table and add the arguments for the method
		manager.newScope();
		for (String arg : argNames){

			if (arg.equals("self")){
				report.err("\n ERROR: identifier self cannot be redefined as method argument\n");
                System.exit(1);
			}

			String argTypeName = arguments.get(arg);
			Clazz argClass = classes.get(argTypeName);
			manager.addSymbol(table,arg,argClass);
		}
		
		setType();
		
		// Make sure that arguments are typed correctly
		for (int i = 0; i < body.size(); ++i){
		
			ASTNode statementNode = body.get(i);
			statementNode.setParent(this);
            
            // Typecheck child node
			Clazz statementClass = statementNode.whatsMyType(t, manager, table, report);
			
			// Address errors relating to the  return type for the method
			if (i == (body.size()-1)) {
				
				Clazz compareType;
				// Handle case that return type is SELF_TYPE - ensure that final statement of the method (i.e., the value that gets returned) has a class matching the parent class of the method
				if (myClass.getClassName().equals("SELF_TYPE")){
					compareType = parentNode.getType();
				}
				// Ensure that stated return type of method matches the final statement of the method (i.e., the value that gets returned)
				else {
					compareType = myClass;
				}

				// Check if the declared type is the actual type or a parent type of the actual type
				// (i.e., implicit upcasting is allowed)
				boolean validSubtype = false;
				
				if (compareType.equals(statementClass)){
					validSubtype = true;
				}
				else {
					Clazz currentClass = statementClass;
					while((currentClass != null)){
					
						if (currentClass.equals(compareType)){
							validSubtype = true;
							break;
						}
						currentClass = currentClass.getParent();
					}
				}
				
				// Report error if return type is not a the declared type
				if (!validSubtype){
					report.err("\n ERROR: Type mismatch between stated and " +
							"actual return values of method " + methodName + "\n");
					report.err("Stated type: " + nameOfNodeType);
					report.err("Actual type: " + statementClass.getClassName() + "\n");
					typeError = true;
				}					
			}
		}
				
        // Clear inner scope from table; restore outer scope if it exists
		manager.restorePrevious(table);
		
        t.log_error(typeError);
		System.out.println("Method node: Type = " + nameOfNodeType);
		return myClass;
	}
	
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
		Collections.reverse(body);
        
        // Use these to contain the generated code under construction
		StringBuilder methodC = new StringBuilder("");
        StringBuilder tabs = super.addTabs(scopeDepth);
        
        // Handle declaration of method and adjust case for different syntax conventions between COOL and C for built-in types

		text.append("_Shell*");
        
        // Continue declaration and add arguments
		methodC.append(" " + parentNode.getTypeName() + "_" + methodName + "(_Shell* this, _Shell** args)\n{");
                
        // Add additional list of known scope variable names, corresponding to the local scope for this method
		scopeVariables.add(new ArrayList<String>());

        // Iterate over argument names for special processing
        Collections.reverse(argNames);
		for (int i = 0; i < argNames.size(); ++i){   

			String arg = argNames.get(i);
            // Add argument name to list of known variables for the method scope, and adjust case for keywords permitted in COOL but not in C
			scopeVariables.get(scopeVariables.size()-1).add(arg);
			String validArgName = getValidIdentifier(arg, resolvedCollisions, classMemberVariables, scopeVariables, true);

			// Arguments are passed in as array, so break it into individual arguments at start of method body
			methodC.append(tabs + "_Shell* " + validArgName + " = args[" + i + "];\n");
		}
        
		text.append(methodC);
				
		String finalStatementReference = "";

        // Generate code for each statement in the method
		for (ASTNode nextStatement: body){
			text.append("\n");
			nextStatement.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
			text.append("\n");
			finalStatementReference = nextStatement.getCValueForNode();
		}

		text.append(tabs + "return " + finalStatementReference + ";");
		
		text.append("\n}\n\n");
        
        // Remove the last (innermost) list of scope variables, corresponding to the local scope for this method
		for (String argName : argNames){   
			String validArgName = getValidIdentifier(argName, resolvedCollisions, classMemberVariables, scopeVariables, false);
			retrieveSilencedIdentifier(argName, resolvedCollisions);
		}
		scopeVariables.remove(scopeVariables.size()-1);
	}
	
}