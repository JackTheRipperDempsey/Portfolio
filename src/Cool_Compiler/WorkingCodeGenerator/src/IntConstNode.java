import java.util.ArrayList;
import java.util.HashMap;

// Implementation for integer literal node
class IntConstNode extends ASTNode{
    
    // The actual integer value held by the node
	private Integer value;
    
	public IntConstNode(String type, Integer val) {
		super(type);
		
		//System.out.println("New int const node");

		value = val;
		whatKindOfNode = "intconst";
	}
	
    // The type of an integer literal node is always Int - if it is not, there has been an error
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking int constant node");

		setType();

		if (!myClass.equals(classes.get("Int"))){
			typeError = true;
		}

		t.log_error(typeError);

		System.out.println("Int constant node: Type = " + nameOfNodeType);
		return myClass;
	}
    
    // Add the integer literal
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){

		StringBuilder tabs = super.addTabs(scopeDepth);

		String tempVariable = getNextTempVariable(whatKindOfNode);
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariable, nameOfNodeType));

		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariable, nameOfNodeType, true, Integer.toString(value), "", false, false));	
		
		cValueOrReference = tempVariable;
	}
}