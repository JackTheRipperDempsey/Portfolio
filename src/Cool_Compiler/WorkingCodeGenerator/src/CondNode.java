import java.util.ArrayList;
import java.util.HashMap;

// Implementation of conditional node
class CondNode extends ASTNode {
	
    // Three child nodes representing different parts of the conditional structure
	private ASTNode ifNode;
	private ASTNode thenNode;
	private ASTNode elseNode;
	
	public CondNode(String type, ASTNode ifN, ASTNode thenN, ASTNode elseN) {
		super(type);
		
		//System.out.println("New cond node");

		ifNode = ifN;
		thenNode = thenN;
		elseNode = elseN;
		whatKindOfNode = "cond";
	}
	
    // The nearest common ancestor/lowest-order superclass shared by the two branches of the conditional defines the type of the conditional node itself
    // e.g., if the then-branch has a type of Type X and the else-branch has a type of Type Y, where Type X and Type Y both extend Object, the type of the conditional as a whole would be Object
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking conditional node");
        
        // Set up child nodes for conditional structure
		ifNode.setParent(this);
		thenNode.setParent(this);
		elseNode.setParent(this);

		setType();

		// Typecheck the if-node and ensure that it is a boolean expression
		Clazz ifClass = ifNode.whatsMyType(t, manager, table, report);
		if (!ifClass.equals(classes.get("Bool"))){
			typeError = true;
			report.err("\n ERROR: Conditional statement not of type Bool \n");
		}
		
		// Find lowest-order superclass shared by the then-node and the else-node
        // This defines the type of the conditional node
		
		Clazz thenClass = thenNode.whatsMyType(t, manager, table, report);
		Clazz elseClass = elseNode.whatsMyType(t, manager, table, report);

		Clazz commonClass = thenClass.getCommonParent(elseClass);
		myClass = commonClass;
		
        nameOfNodeType = myClass.getClassName();
		t.log_error(typeError);
		System.out.println("Cond node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // The main complications to generating the code for the conditional involve determning whether or not we have a nested conditional structure, and whether we need to generate conventional if-then-else code or ternary code
    // (The main difference between the two is that a ternary statement is a valid r-value and can be used in an assignment)
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
        // Add any necessary tabs
        StringBuilder tabs = super.addTabs(scopeDepth);
        
        text.append(tabs);
        String tempVariableBody = getNextTempVariable(whatKindOfNode + "_body");
        cValueOrReference = tempVariableBody;

        text.append(ASTNode.generateMemoryAllocationOnlyForObject(tabs, tempVariableBody, nameOfNodeType));

        String tempVariableCond = getNextTempVariable(whatKindOfNode);
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableCond, ifNode.getTypeName()));

        ifNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

        text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableCond, ifNode.getTypeName(), true, ifNode.getCValueForNode(), ifNode.getTypeName(), true, true));
            
        // Generate code for the governing boolean statement
        text.append("\n\n" + tabs + "if (");
        text.append(generateDereferencedReferenceForObject(tempVariableCond,ifNode.getTypeName()));
        text.append("){\n");
    
        // Generate code for the then-block
        scopeVariables.add(new ArrayList<String>());

        String tempVariableThen = getNextTempVariable(whatKindOfNode + "_then");

        text.append(ASTNode.generateMemoryAllocationOnlyForObject(new StringBuilder(tabs.toString() + "\t"), tempVariableThen, thenNode.getTypeName()));

        thenNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth+1);

        text.append(ASTNode.generateTabbedAssignmentLine(new StringBuilder(tabs.toString() + "\t"), tempVariableThen, thenNode.getTypeName(), true, thenNode.getCValueForNode(), thenNode.getTypeName(), true, true));
        
        text.append(ASTNode.generateTabbedAssignmentLine(new StringBuilder(tabs.toString() + "\t"), tempVariableBody, nameOfNodeType, true, tempVariableThen, thenNode.getTypeName(), true, true));

        scopeVariables.remove(scopeVariables.size()-1);
        
        // Remove extraneous semicolons
        if (text.charAt(text.length()-1) != ';' && text.charAt(text.length()-1) != '\n'){
            text.append(";");
        }
        
        // Handle bracket closure
        text.append("\n");
        text.append(tabs);
        text.append("}\n");
    
        // We handle and generate code for the else-block here
        scopeVariables.add(new ArrayList<String>());
  
        text.append(tabs);
        text.append("else {\n");

        String tempVariableElse = getNextTempVariable(whatKindOfNode  + "_else");

        text.append(ASTNode.generateMemoryAllocationOnlyForObject(new StringBuilder(tabs.toString() + "\t"), tempVariableElse, elseNode.getTypeName()));

        elseNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth+1);

        text.append(ASTNode.generateTabbedAssignmentLine(new StringBuilder(tabs.toString() + "\t"), tempVariableElse, elseNode.getTypeName(), true, elseNode.getCValueForNode(), elseNode.getTypeName(), true, true));
        
        text.append(ASTNode.generateTabbedAssignmentLine(new StringBuilder(tabs.toString() + "\t"), tempVariableBody, nameOfNodeType, true, tempVariableElse, elseNode.getTypeName(), true, true));
        
        // Add a closing semicolon and close brackets as needed
        if (text.charAt(text.length()-1) != ';'){
            text.append(";");
        }
        text.append("\n");
        text.append(tabs);
        text.append("}\n");
        
        scopeVariables.remove(scopeVariables.size()-1);
    }
    
}