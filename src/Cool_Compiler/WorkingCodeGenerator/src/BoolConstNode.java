import java.util.ArrayList;
import java.util.HashMap;

// Implementation for boolean literal node
class BoolConstNode extends ASTNode{
    
    // The actual boolean value held by the node
	private boolean val;
    
	public BoolConstNode(String type, boolean bool) {
		super(type);
		
		//System.out.println("New bool const node");

		val = bool;
		whatKindOfNode = "boolconst";
	}
	
    // The type of an boolean literal node is always Bool - if it is not, there has been an error
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking bool constant node");

		setType();

		if (!myClass.equals(classes.get("Bool"))){
			typeError = true;
		}
		
		t.log_error(typeError);

		System.out.println("Bool constant node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // Add the boolean value
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){

		StringBuilder tabs = super.addTabs(scopeDepth);

		String tempVariable = getNextTempVariable(whatKindOfNode);
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariable, nameOfNodeType));

		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariable, nameOfNodeType, true, Boolean.toString(val), "", false, false));		

		cValueOrReference = tempVariable;
	}
}
