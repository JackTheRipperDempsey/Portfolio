import java.util.ArrayList;
import java.util.HashMap;

// Implementation for let node without initialization
class LetNoInitNode extends ASTNode{
	
    // Here, we only have a declared variable name, the body/'in' statement in which it is visible in the symbol table, and a declared type
    // There is no initialized value
	private String variable;
	private ASTNode inExpr;
	private String varType;
	private ASTNode caseValueNode;
	
	public LetNoInitNode(String type, String name, ASTNode e, String vType){
		super(type);
		
		//System.out.println("New let no init node");

		variable = name;
		inExpr = e;
		varType = vType;
		whatKindOfNode = "letnoinit";

	}

	protected void setCaseAssignedValue(ASTNode valueNode){
		caseValueNode = valueNode;
	}

	protected String getVarType(){
		return varType;
	}
	
    // As with the let-with-initialization, the type is the type of the body/'in' expression
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){

		System.out.println("Typechecking let-no-init node");

        if (variable.equals("self")){
            report.err("\n ERROR: identifier self cannot be redefined as local variable\n");
			System.exit(1);
        }

        // Get the actual declared Clazz of the variable from its name
		Clazz assignType = classes.get(varType);
        
        // Open a new nested scope for the let statement, and add the new variable to that new scope
		manager.newScope();
		manager.addSymbol(table, variable, assignType);
		
		setType();
		
		inExpr.setParent(this);
		
        // Typecheck 'in' expression/body of the let
		myClass = inExpr.whatsMyType(t, manager, table, report);
		
		t.log_error(typeError);
		
		manager.restorePrevious(table);
		
        nameOfNodeType = myClass.getClassName();
		System.out.println("Let-no-init node: Type = " + nameOfNodeType);
		return myClass;
	}
    
    @Override
    public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
        // Add a new scope for the let, and add the variable to the symbol table
        scopeVariables.add(new ArrayList<String>());
        scopeVariables.get(scopeVariables.size()-1).add(variable);
        
        StringBuilder tabs = super.addTabs(scopeDepth);
		
		// Handle uppercase-lowercase conversion between COOL and C, based on whether the capitalized or the lowercase version of certain type words is prohibited and reserved
		String validVarName = getValidIdentifier(variable, resolvedCollisions, classMemberVariables, scopeVariables, true);

		if (varType.equals("Bool") || varType.equals("Int") || varType.equals("String")){
			text.append("\n\n" + tabs + "_Shell* ");
			text.append(validVarName + " = " + varType + "_" + varType + "()" + ";\n");	
		}
		else {
			text.append(generateMemoryAllocationOnlyForObject(tabs, validVarName, varType));			
		}

		if (caseValueNode != null){
			text.append("\n" + ASTNode.generateTabbedAssignmentLine(tabs, validVarName, nameOfNodeType, true, caseValueNode.getCValueForNode(), caseValueNode.getTypeName(), true, true) + "\n");
		}
		
        // Generate code for the let's in-expression
        inExpr.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
		
		cValueOrReference = inExpr.getCValueForNode();
		
        // Remove the added scope for the let
		retrieveSilencedIdentifier(variable, resolvedCollisions);
        scopeVariables.remove(scopeVariables.size()-1);
    }

}