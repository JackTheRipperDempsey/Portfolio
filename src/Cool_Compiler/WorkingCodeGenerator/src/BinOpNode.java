import java.util.ArrayList;
import java.util.HashMap;

// Implementation for binary operation node
class BinOpNode extends ASTNode {
	
    // These represent the two child expressions on each side of the binary operator
	private ASTNode leftChild;
	private ASTNode rightChild;
		
    // Exactly which binary operator it is, e.g. '+' vs. '<'
	private String op;
    
	public BinOpNode(String type, ASTNode lchild, ASTNode rchild, 
			String token) {
		super(type);
		
		//System.out.println("New binop node");
		
		leftChild = lchild;
		rightChild = rchild;
		op = token;
		whatKindOfNode = "binop";

	}
	
    // The type of the binary operation will be either Int or Bool depending on the operator
    // An arithmetic operator like '+' has a type of Int, while a comparative operator like '<' has a type of Bool
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking binop node");

		setType();
		
		leftChild.setParent(this);
		rightChild.setParent(this);
		
		// Check the type of the child expression on each side of the operator
		Clazz leftType = leftChild.whatsMyType(t, manager, table, report);
		Clazz rightType = rightChild.whatsMyType(t, manager, table, report);
		
		// Determine if the operator is typed as an integer or as a boolean:
        
        // 1. If we have an arithmetic operator typed as an integer, the expression on each side of the operator must also be of type Int
		if (myClass.equals(classes.get("Int"))){

			if ((!leftType.equals(classes.get("Int"))) ||
                (!rightType.equals(classes.get("Int")))){
				typeError = true;
				report.err("\n ERROR: Non-integer arguments to arithmetic operation \n");
			}
		}
		
        // 2. If we have a comparative operator typed as a bool, we have two cases to consider (equality vs. less-than or less-than-or-equal-to)
		else if (myClass.equals(classes.get("Bool"))){
            // Direct equality is considered a valid comparison across all object types, but only if the left and right child expressions have the same type
			if (op.equals("=")) {
				if (!leftType.equals(rightType)){
					report.err("\n ERROR: Argument types don't match for equality operator \n");
					typeError = true;
				}
			}
            // Otherwise, the less-than and less-than-or-equal-to comparisons are only considered valid if both left and right child expressions have type Int
			else {
				if ((!leftType.equals(classes.get("Int"))) ||
						(!rightType.equals(classes.get("Int")))){
					typeError = true;
					report.err("\n ERROR: Non-integer arguments to less-than comparison \n");
				}
			}
		}
		
		t.log_error(typeError);
		System.out.println("Binop node: Type = " + nameOfNodeType);
		return myClass;
		
	}
	
    // The code generation for binary operations is fairly simple:
    // We simply generate code for the left child expression, then insert the operator, and then generate code for the right child expression
    // The only special cases involve whether the expression takes place within a conditional or the argument list for a dispatch statement
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){

		StringBuilder tabs = super.addTabs(scopeDepth);
		
		String tempVariableBinop = getNextTempVariable(whatKindOfNode);
        cValueOrReference = tempVariableBinop;
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableBinop, nameOfNodeType));

		String tempVariableLeft = getNextTempVariable(leftChild.getWhatKindOfNode());
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableLeft, leftChild.getTypeName()));
	
		leftChild.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableLeft, leftChild.getTypeName(), true, leftChild.getCValueForNode(), leftChild.getTypeName(), true, true));

		String tempVariableRight = getNextTempVariable(rightChild.getWhatKindOfNode());
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableRight, rightChild.getTypeName()));

		rightChild.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableRight, rightChild.getTypeName(), true, rightChild.getCValueForNode(), rightChild.getTypeName(), true, true));

		text.append("\n\n" + tabs.toString() + ASTNode.generateDereferencedReferenceForObject(tempVariableBinop, nameOfNodeType));
		text.append(" = ");

		// Part of the binop typechecking is making sure that the left and right child nodes have the same type, so by checking if the left child is of type String, we are checking that the right is as well
		// 1. Equality comparison
		if (op.trim().equals("=")) {

			// 1. String comparison
			if ((leftChild.getTypeName().equals("String"))){
				// This is a hack to cast to bool in a way that will satisfy particularly picky systems
				text.append("!strcmp(" + "(char*) (*(((String *)" +  tempVariableLeft + "->reference)->_data))" + ", " + "(char*) (*(((String *)" + tempVariableRight + "->reference)->_data))" + ");\n");
			}
			// 2. Int or bool comparison
			else if (leftChild.getTypeName().equals("Int") || leftChild.getTypeName().equals("Bool")){

				text.append("(");

				text.append(ASTNode.generateDereferencedReferenceForObject(tempVariableLeft, leftChild.getTypeName()));


				// COOL uses '=' as its equality operator, so we convert that to the classic '==' in the output C code

				text.append(" == ");
				

				text.append(ASTNode.generateDereferencedReferenceForObject(tempVariableRight, rightChild.getTypeName()));

				text.append(");\n");
			}
			// 3. Object comparison
			else {
				text.append("(");

				text.append(ASTNode.generateReferenceForObject(tempVariableLeft, leftChild.getTypeName()));

				// COOL uses '=' as its equality operator, so we convert that to the classic '==' in the output C code

				text.append(" == ");

				text.append(ASTNode.generateReferenceForObject(tempVariableRight, rightChild.getTypeName()));
				text.append(");\n");
			}

		}
		// 2. Any other binary operation (i.e., one other than equality comparison)
		else {

			text.append("(");

			text.append(ASTNode.generateDereferencedReferenceForObject(tempVariableLeft, leftChild.getTypeName()));

			text.append(" " + op + " ");

			text.append(ASTNode.generateDereferencedReferenceForObject(tempVariableRight, rightChild.getTypeName()));

			text.append(");\n");
		}
	}

}
