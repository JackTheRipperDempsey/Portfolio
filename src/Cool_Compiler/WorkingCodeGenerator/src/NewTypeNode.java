import java.util.ArrayList;
import java.util.HashMap;

// Implementation for new variable node
class NewTypeNode extends ASTNode{
	
	public NewTypeNode(String type){
		super(type);
		//System.out.println("New new type node");
		whatKindOfNode = "new_var";
	}
	
	// The type of a new variable statement is simply the declared type of the new variable
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking new type node");

		setType();
		
		t.log_error(typeError);
		
		System.out.println("New type node: Type = " + nameOfNodeType);
		return myClass;
	}
    
    // Call the appropriate constructor for the type of the node
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth)
	{
        StringBuilder tabs = super.addTabs(scopeDepth);
		String tempVariableNew = getNextTempVariable(whatKindOfNode);
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariableNew, nameOfNodeType));
		cValueOrReference = tempVariableNew;
	}
}