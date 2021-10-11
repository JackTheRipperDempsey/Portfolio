import java.util.ArrayList;
import java.util.HashMap;

// Implementation for variable node
class VarNode extends ASTNode{
    
    // The name of the variable
	private String variable;
    
    // The declared type of the variable
    private String declaredTypeName;
	
	public VarNode(String type, String name){
		super(type);
		//System.out.println("New variable node");

        declaredTypeName = type;
		variable = name;
		whatKindOfNode = "variable";
	}
	
	@Override
	public String getVariableName(){
		return variable;
	}
    
    @Override
    public String getDeclaredTypeName(){
        return declaredTypeName;
    }
	
    // The type of a variable node is the declared type of the variable as registered in the symbol table
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
        
        setType();

		// Handle references to governing class (i.e., the 'self' or 'this' pointer)
		if (variable.equals("self")){
			
            // We do not want to store an actual 'self' variable - it is a builtin keyword
			if (table.containsKey(variable)){
				manager.removeSymbol(table,variable);
			}
            
            // This could be more efficient, but the idea is that we want to find the governing class in order to apply the 'this' reference correctly and assign that class as the node's type
			ASTNode findClassNode = this.getParent();
			while(!findClassNode.getWhatKindOfNode().equals("class")){
				findClassNode = findClassNode.getParent();
			}
			
			myClass = classes.get(findClassNode.getTypeName());
			
		}
		
		// Otherwise, we look for the variable name in the symbol table
        
        // If it is not there, we add it
		if (!table.containsKey(variable)){
			manager.addSymbol(table, variable, myClass);
		}
        
		// Otherwise, we take its type
		else {
			myClass = table.get(variable);
		}
		
        nameOfNodeType = myClass.getClassName();
		t.log_error(typeError);
		System.out.println("Variable node: Type = " + nameOfNodeType);
		return myClass;
	}
    
    // Generate code for the variable - main considerations are where it was defined, and whether that means we need to put in any extra syntax
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
        StringBuilder tabs = super.addTabs(scopeDepth);
        
        // NOTE: The order of these blocks was switched 4/17/2020 - watch this space for errors and see if there is any further refinement needed
        // (e.g., is variable redefinition possible?)
        // UPDATE: 4/19/2020 - Upon consultation with the COOL manual, it has been determined that yes, redeclaration of variables in a local scope is possible, and that this hides the definition of the class attribute with the same identifier; as such, these blocks have been reverted to their original order, and a correction has been made to AssignNode to prevent class variables from inadvertently being class as local ones as well
        // I am leaving the NOTE in place for now as a watch-item in case further problems with this arise
        
        // Check if it comes from a local scope - if it does, referencing it is very simple
		boolean fromScope = false;
        
        for (int i = 0; i< scopeVariables.size(); ++i){
            if (scopeVariables.get(i).contains(variable)){
                fromScope = true;
                break;
            }
        }
        
        // If not declared in the local scope, check if the variable comes from a governing class
        boolean fromClass = false;
        
        if (!fromScope && classMemberVariables.contains(variable)){
            fromClass = true;
		}
		
		String tempVariableVar = getNextTempVariable(whatKindOfNode);
        cValueOrReference = tempVariableVar;
		text.append(ASTNode.generateMemoryAllocationOnlyForObject(tabs, tempVariableVar, nameOfNodeType));

		StringBuilder referenceText = new StringBuilder();
		String validVarName = getValidIdentifier(variable, resolvedCollisions, classMemberVariables, scopeVariables, false);

		if (fromClass){
			referenceText.append("((" + governingClassName + " *) (");
			if (inConstructor){
				referenceText.append(constructorTempVariable);
			}
			else {
				referenceText.append("this");
			}
			referenceText.append("->reference))->");
		}

		if (variable.equals("self")){
			if (inConstructor){
				referenceText.append(constructorTempVariable);
			}
			else {
				referenceText.append("this");
			}
		}
		else {
			referenceText.append(validVarName);
		}

        text.append("\n\n" + tabs.toString() + tempVariableVar + "->reference");
		text.append(" = ");
		
		text.append(referenceText);
		text.append("->reference");

		text.append(";\n");

        text.append("\n\n" + tabs.toString() + tempVariableVar + "->numTypes");
		text.append(" = ");
		
		text.append(referenceText);
		text.append("->numTypes");

		text.append(";\n");		
		
        text.append("\n\n" + tabs.toString() + tempVariableVar + "->type");
		text.append(" = ");
		
		text.append(referenceText);
		text.append("->type");

		text.append(";\n");

        text.append("\n\n" + tabs.toString() + tempVariableVar + "->init");
		text.append(" = ");
		
		text.append(referenceText);
		text.append("->init;");

        text.append("\n\n" + tabs.toString() + tempVariableVar + "->numMethods");
		text.append(" = ");
		
		text.append(referenceText);
		text.append("->numMethods;");

        text.append("\n\n" + tabs.toString() + tempVariableVar + "->methodNames");
		text.append(" = ");
		
		text.append(referenceText);
		text.append("->methodNames;");

        text.append("\n\n" + tabs.toString() + tempVariableVar + "->methodPointers");
		text.append(" = ");
		
		text.append(referenceText);
		text.append("->methodPointers;\n");

        text.append("\n\n" + tabs.toString() + tempVariableVar + "->defaultCopyMethod");
		text.append(" = ");
		
		text.append(referenceText);
		text.append("->defaultCopyMethod;\n");

	}
}