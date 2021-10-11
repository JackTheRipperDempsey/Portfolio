import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// Implementation for sequence node
class SequenceNode extends ASTNode {
	
    // Each statement in the sequence is a child node of the sequence node
	private ArrayList<ASTNode> statements;
	
	public SequenceNode(String type, ArrayList<ASTNode> nodes){
		super(type);
		
		//System.out.println("New sequence node");
		statements = nodes;
		whatKindOfNode = "sequence";

	}
	
    // Add a new statement to the sequence
	public void addStatement(ASTNode newStatement){
		statements.add(newStatement);
	}
	
    // The type of the sequence node is the type of the last statement in the sequence
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking sequence node");

		setType();
		
		// Typecheck each node in the sequence
		Clazz lastClass = classes.get("Object");
        // Note: We do this in reverse because the type of the sequence is defined by the final statement
		for (int i = (statements.size()-1); i >= 0; --i){
            ASTNode nextStatement = statements.get(i);
			nextStatement.setParent(this);
			Clazz statementClass = nextStatement.whatsMyType(t, manager, table, report);
			if (nextStatement.isError()){
				typeError = true;
				report.err("\n ERROR: Type error in sequence of statements \n");
			}
			lastClass = statementClass;
		}
		
        // Assign type based on the last statement
		myClass = lastClass;
		
        nameOfNodeType = myClass.getClassName();
		t.log_error(typeError);
		System.out.println("Sequence node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // Generate C code for each statement in the sequence
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
		Collections.reverse(statements);
        
        // Generate code for each child node
		for (ASTNode statement: statements){
			statement.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
            if ((text.charAt(text.length()-1) != ';') && (text.charAt(text.length()-1) != '\n')){
				text.append(";");
			}
			text.append("\n");
			cValueOrReference = statement.getCValueForNode();
		}
	}
}