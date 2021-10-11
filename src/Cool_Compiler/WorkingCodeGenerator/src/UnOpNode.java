import java.util.ArrayList;
import java.util.HashMap;

// Implementation for unary operator node
class UnOpNode extends ASTNode {
	
    // This represents the expression to the right of the unary operator (e.g., the boolean value being inverted in a 'not' statement)
	private ASTNode childNode;
	
    // The unary operator itself (i.e., 'not' or '~')
	private String op;
	
	public UnOpNode(String type, ASTNode child, 
			String token) {
		super(type);
		
		//System.out.println("New unop node");

		childNode = child;
		op = token;
		whatKindOfNode = "unop";

	}

    // The type of a unary expression is either Int (in the case of a '~' arithmetic inversion), or Bool (in the case of a 'not' logical inversion)
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){

		System.out.println("Typechecking unop node");

		setType();
		
		childNode.setParent(this);
		
		// Check type of node being operated on
		Clazz childType = childNode.whatsMyType(t, manager, table, report);
		
		// Determine if unary operator is typed as an integer or as a boolean
		if (myClass.equals(classes.get("Int"))){

            // If we have a '~' statement (the arithmetic inversion, equivalent to multiplying by -1), then the child expression being inverted must also be typed as an Int
			if (!childType.equals(classes.get("Int"))){
				typeError = true;
				report.err("\n ERROR: Non-integer argument to integer negation operator \n");
			}
		}
		
        // If we have a 'not' statement, then the child expression being inverted must also be typed as a Bool
		else if (myClass.equals(classes.get("Bool"))){
				if (!childType.equals(classes.get("Bool"))){
					typeError = true;
					report.err("\n ERROR: Non-boolean argument to NOT statement \n");
				}			
		}
		
		t.log_error(typeError);
		System.out.println("UnOp node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // The code generation for a unary operation is fairly simple:
    // First we insert the operator, and then we generate code for the child statement
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
		
		StringBuilder tabs = super.addTabs(scopeDepth);

		String tempVariableUnop = getNextTempVariable(whatKindOfNode);
        cValueOrReference = tempVariableUnop;
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableUnop, nameOfNodeType));

		String tempVariableChild = getNextTempVariable(whatKindOfNode);
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableChild, nameOfNodeType));

		childNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableChild, childNode.getTypeName(), true, childNode.getCValueForNode(), childNode.getTypeName(), true, true));
		
		text.append("\n\n" + tabs.toString() + ASTNode.generateDereferencedReferenceForObject(tempVariableUnop, nameOfNodeType));
		text.append(" = ");
		text.append("(");

		// Handle logical negation
		if (op.trim().equals("not")){
			text.append("!");
		}
		
		// Handle integer inversion
		else if (op.trim().equals("~")){
			text.append("-1*");
		}
        
        text.append("(");

		text.append(ASTNode.generateDereferencedReferenceForObject(tempVariableChild, childNode.getTypeName()));

        text.append("));\n");
	}
}
