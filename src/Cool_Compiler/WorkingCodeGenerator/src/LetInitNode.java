import java.util.ArrayList;
import java.util.HashMap;

// Implementation for let node with initialization
class LetInitNode extends ASTNode{
	
    // As with an assignment node, we have a child representing an expression with a value, and a variable being assigned that value for the scope of the let (as defined by the 'in' statement)
	private ASTNode valueNode;
	private String variable;
    
    // The 'in' statement forms the equivalent of the let's body, and defines the scope under which the variables given value by the let are visible
	private ASTNode inExpr;
    
    // The declared type of the variable
	private String varType;
	
	public LetInitNode(String type, ASTNode valNode,
			String name, ASTNode e, String vType){
		super(type);
		
		//System.out.println("New let init node");

		valueNode = valNode;
		variable = name;
		inExpr = e;
		varType = vType;
		whatKindOfNode = "letinit";
    }
		
    // The type of the let statement is the type of its body/'in' expression, as defined by the final line of that block
    // (Keeping with COOL's normal convention)
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking let-init node");

        if (variable.equals("self")){
            report.err("\n ERROR: identifier self cannot be redefined as local variable\n");
            System.exit(1);
        }

        // Get the actual declared Clazz of the variable from its name
		Clazz assignType = classes.get(varType);
        
        // Open a new nested scope for the let statement
		manager.newScope();
        
		setType();
		
		valueNode.setParent(this);
		inExpr.setParent(this);
		
		// Make sure that valueNode's class is a subclass of the variable's class as declared
        // (Allows possibility of them being the same class - does not require a strict or proper subtype)
        
        // If valueNode's type and the declared type are the same, everything checks out
        // If the type of the value node and the declared class are not exactly the same, check if the declared type is a parent type of the actual type
        // (i.e., implicit upcasting is allowed)
        
        Clazz currentClass = valueNode.whatsMyType(t, manager, table, report);
        Clazz valClass = currentClass;
        boolean atRoot = false;
        boolean subType = false;
        
        // Note: This loop formerly used 'currentClass.getParent() != null' as its condition
        // This was supplemented by two additional conditional checks before and after the loop
        // In the interest of consolidation, it was determined that this was a slightly cleaner way of handling that
        while(!subType){
            if (currentClass.equals(assignType)){
                manager.addSymbol(table, variable, currentClass);
                subType = true;
            }
            else if (atRoot){
                break;
            }
            
            if (currentClass.getParent() != null) {
                currentClass = currentClass.getParent();
            }
            else {
                atRoot = true;
            }
        }
        
        // Typecheck the 'in' expression
        myClass = inExpr.whatsMyType(t, manager, table, report);
        
        // Log any type errors encountered in typechecking the assignment in the let
        if (!subType){
            // Add the variable to the symbol table with the assigned type anyway
            // (so that its absence does not cascade into seemingly unrelated errors)
            manager.addSymbol(table, variable, assignType);
            typeError = true;
            report.err("\n ERROR: Value class " + valClass.getClassName() + " not a subclass of variable class " + varType + "\n");
            
        }
        
		manager.restorePrevious(table);
        
        nameOfNodeType = myClass.getClassName();
        t.log_error(typeError);
		System.out.println("Let-init node: Type = " + nameOfNodeType);
		return myClass;
	}
    
    @Override
    public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
        // Add a new scope for the let, and add the variable to the symbol table
        scopeVariables.add(new ArrayList<String>());
        scopeVariables.get(scopeVariables.size()-1).add(variable);

        StringBuilder tabs = super.addTabs(scopeDepth);
        
        // Generate the actual assignment value for the let
        valueNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
        text.append(";\n");
        
        // Handle uppercase-lowercase conversion between COOL and C, based on whether the capitalized or the lowercase version of certain type words is prohibited and reserved
        String validVarName = getValidIdentifier(variable, resolvedCollisions, classMemberVariables, scopeVariables, true);

        text.append(generateMemoryAllocationOnlyForObject(tabs, validVarName, varType));

        text.append(generateTabbedAssignmentLine(tabs, validVarName, varType, true, valueNode.getCValueForNode(), valueNode.getTypeName(), true, true));
        
        // Generate code for the let's in-expression
        inExpr.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
        
		cValueOrReference = inExpr.getCValueForNode();

        // Remove the added scope for the let
		retrieveSilencedIdentifier(variable, resolvedCollisions);
        scopeVariables.remove(scopeVariables.size()-1);
    }
}