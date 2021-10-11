import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// Implementation for loop node
class LoopNode extends ASTNode {
	
    // Child nodes representing the boolean expression controlling the loop (i.e., the 'while' statement in C and Java syntax), and the child statements (i.e., the body of the loop) executed on each iteration of the loop
	private ASTNode whileNode;
	private ArrayList<ASTNode> loopExpressions;
	
	public LoopNode(String type, ASTNode wNode, ArrayList<ASTNode> nodes){
		super(type);
		
		//System.out.println("New loop node");
		
		whileNode = wNode;
		loopExpressions = nodes;
		whatKindOfNode = "loop";

	}
	
    // Add a new statement to the body of the loop
	public void addExpression(ASTNode expr){
		loopExpressions.add(expr);
	}
	
    // The type of the loop expression/node itself is Object
    // However, we still use this method to typecheck the child statements as a side effect
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking loop node");

		setType();
		whileNode.setParent(this);
        
		// Check the loop condition, and ensure that it resolves to a boolean expression
		if (!whileNode.whatsMyType(t, manager, table, report).equals(classes.get("Bool"))){
			typeError = true;
			report.err("\n ERROR: Loop condition of type other than bool \n");
		}

		// Check each statement inside of the loop
		for (int i = 0; i < loopExpressions.size(); ++i){
            ASTNode nextExpression = loopExpressions.get(i);
			nextExpression.setParent(this);
			Clazz loopClass = nextExpression.whatsMyType(t, manager, table, report);
		}
		
        t.log_error(typeError);
		System.out.println("Loop node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // Generate C code for the loop
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
        // Generate the governing while-statement
		StringBuilder tabs = super.addTabs(scopeDepth);
		
		// Loops have a value of void and a type of Object
		String tempVariableLoop = getNextTempVariable(whatKindOfNode);
		text.append(ASTNode.generateMemoryAllocationOnlyForObject(tabs, tempVariableLoop, nameOfNodeType));

		cValueOrReference = tempVariableLoop;

		String tempVariableWhile = getNextTempVariable(whileNode.getTypeName());
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableWhile, whileNode.getTypeName()));

		whileNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableWhile, whileNode.getTypeName(), true, whileNode.getCValueForNode(), whileNode.getTypeName(), true, true));

        text.append("\n\n" + tabs);
		text.append("while (");

		text.append(generateDereferencedReferenceForObject(tempVariableWhile,whileNode.getTypeName()));
		text.append("){\n");
        
        // Open a new scope, and generate code for each expression within the body of the loop
		scopeVariables.add(new ArrayList<String>());
		Collections.reverse(loopExpressions);
		for (ASTNode expr: loopExpressions){
			expr.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth+1);
		}


		// Reevaluate loop condition
		whileNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableWhile, whileNode.getTypeName(), true, whileNode.getCValueForNode(), whileNode.getTypeName(), true, true));
        
        // Handle any needed cleanup involved in removing loop-scope variables from the symbol table and closing brackets for the loop
		scopeVariables.remove(scopeVariables.size()-1);
        text.append("\n");
        text.append(tabs);
		text.append("}\n");
	}
	
}
