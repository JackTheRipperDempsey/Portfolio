import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang.*;

// Implementation for string literal node
class StringConstNode extends ASTNode{
    
    // The actual string value held by the node
	private String string;
    
	public StringConstNode(String type, String str) {
		super(type);
		
		//System.out.println("New string const node");

		string = str;
		whatKindOfNode = "stringconst";
	}
	
    // The type of an string literal node is always String - if it is not, there has been an error
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking string constant node");

		setType();

		if (!myClass.equals(classes.get("String"))){
			typeError = true;
		}

		t.log_error(typeError);

		System.out.println("String constant node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // Add the string
    // Escape the quotation marks to make this valid in C
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
		String newString = "\"" + StringEscapeUtils.escapeJava(string) + "\"";   

		StringBuilder tabs = super.addTabs(scopeDepth);

		String tempVariable = getNextTempVariable(whatKindOfNode);
		text.append(ASTNode.generateInitializationForObject(tabs, tempVariable, nameOfNodeType));

		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariable, nameOfNodeType, true, newString, "", false, false));		

		cValueOrReference = tempVariable;
	}
}