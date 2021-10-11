import java.util.ArrayList;
import java.util.HashMap;

// Implementation for isvoid node
class IsVoidNode extends ASTNode{
    
    // This is the child node being checked for nullitys
	private ASTNode expr;
    
	public IsVoidNode(String type, ASTNode e){
		super(type);
		System.out.println("New isvoid node");

		expr = e;
		whatKindOfNode = "isvoid";
	}
	
    // The type of an isvoid node is Bool (not assigned explicitly here because it is the default type assigned through the setType call)
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){

		System.out.println("Typechecking isvoid node");

		setType();
		
		expr.setParent(this);
		
		// Typecheck child expression
		Clazz expressionClass = expr.whatsMyType(t, manager, table, report);
        
		if ((expressionClass.equals(classes.get("Int"))) || (expressionClass.equals(classes.get("Bool")))
				|| (expressionClass.equals(classes.get("String")))){
			typeError = true;
			report.err("\n ERROR: Isvoid function cannot accept arguments of type int, bool, or string \n");
		}
		
		t.log_error(typeError);
		System.out.println("Isvoid node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // Check whether the shell struct of the variable being void-checked's init flag has been flipped
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
		StringBuilder tabs = super.addTabs(scopeDepth);

		String tempVariableIsvoid = getNextTempVariable(whatKindOfNode);
		cValueOrReference = tempVariableIsvoid;
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableIsvoid, nameOfNodeType));
		expr.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

		String voidCondString = "!" + expr.getCValueForNode() + "->init";
		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableIsvoid, nameOfNodeType, true, voidCondString, "", false, false));		
	}
}
