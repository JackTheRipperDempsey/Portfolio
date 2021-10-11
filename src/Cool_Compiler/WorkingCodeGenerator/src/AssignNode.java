import java.util.ArrayList;
import java.util.HashMap;

// Implementation for assignment node
class AssignNode extends ASTNode{
	
    // The child node holding the value being assigned to a new token/symbol in the symbol table
	private ASTNode valueNode;
    
    // The symbol/reference variable that will hold the above value
	private String variable;
    
    // The declared type of the variable
    private String declaredTypeName;
    
    // This comes into play when generating code for the initialization of a new object
    
    // If we're assigning a value to member field of an object, such as part of that object's initialization, we need to be able to access that member field in the output C code using standard '(object name)->(field name)' syntax
    
    // This variable allows us to do that
    private String objectInstanceName;
    
    // The parent class of the variable being assigned a value
	private Clazz superClass;
	
	@Override
	public String getVariableName(){
		return variable;
	}
    
    @Override
    public String getDeclaredTypeName(){
        return declaredTypeName;
    }

	public AssignNode(String type, ASTNode valNode,
			String name){
		super(type);
		
		//System.out.println("New assign node");

        declaredTypeName = type;
		valueNode = valNode;
		variable = name;
		whatKindOfNode = "assign";
        objectInstanceName = null;

	}
    
    public void setObjectInstanceName (String objectName){
        objectInstanceName = objectName;
    }
	
    // The type of the assignment expression is the type of the value being assigned
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){

		System.out.println("Typechecking assign node");

		// This is for when we define a variable at the same time we declare it
		if (!table.containsKey(variable)){
			manager.addSymbol(table, variable, myClass);
		}
		
        // Get the declared type of the variable from the table (may or may not have just been added)
        // We will use this in an inheritance check below
		superClass = table.get(variable);
		
		valueNode.setParent(this);
		myClass = valueNode.whatsMyType(t,manager,table, report);
        nameOfNodeType = myClass.getClassName();
        
		// Make sure valueNode's actual class is a subclass of the variable's declared class
        // (Allows possibility of them being the same class - does not require a strict or proper subtype)
			
        // If the value node's type is the same as the declared type for the variable, everything checks out
        // Otherwise, check if the declared type is a parent type of the actual type, and continue until you hit the root of the type hierarchy
        // (i.e., implicit upcasting is allowed)

        Clazz currentClass = myClass;
        boolean atRoot = false;
        boolean subType = false;
        
        // Note: This loop formerly used 'currentClass.getParent() != null' as its condition
        // This was supplemented by two additional conditional checks before and after the loop
        // In the interest of consolidation, it was determined that this was a slightly cleaner way of handling that
        while(!subType){
            if (currentClass.equals(superClass)){
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

        if (!subType){
            typeError = true;
            report.err("\n ERROR: " + nameOfNodeType + " not a subclass of " + superClass.getClassName()
                               + "\n");
        }
		
		t.log_error(typeError);
		System.out.println("Assign node: Type = " + nameOfNodeType);
		return myClass;
	}
    
    // Generate code for the assignment statement
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
        // Determine however many tabs are needed for clean output
        StringBuilder tabs = super.addTabs(scopeDepth);
        
        // Store name of valueNode's type for easy access
        String valueNodeType = valueNode.getTypeName();
        
        // Determine if the variable being assigned belongs to the known scope
		boolean fromScope = false;
        
		for (int i = 0; i< scopeVariables.size(); ++i){
			if (scopeVariables.get(i).contains(variable)){
				fromScope = true;
				break;
			}
		}
        
        // Otherwise, determine if it's a class member field
        boolean fromClass = false;
        
        if (!fromScope){
            if (classMemberVariables.contains(variable)){
                fromClass = true;
            }
        }
      
        String tempVariableAssign = getNextTempVariable(whatKindOfNode);
        cValueOrReference = tempVariableAssign;

        text.append(ASTNode.generateMemoryAllocationOnlyForObject(tabs, tempVariableAssign, nameOfNodeType));
        
		valueNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

        text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableAssign, nameOfNodeType, true, valueNode.getCValueForNode(), valueNode.getTypeName(), true, true));
        
        text.append("\n\n");

        // Handle uppercase-lowercase conversion between COOL and C, based on whether the capitalized or the lowercase version of certain type words is prohibited and reserved

        String formattedVariable = getValidIdentifier(variable, resolvedCollisions, classMemberVariables, scopeVariables, false);
        // If the variable is a class member variable, then we need to make sure that we include the correct C '->' accessing syntax when we perform the assignment

        if (fromClass){
            StringBuilder rootReference = new StringBuilder(tabs);

            if (objectInstanceName != null){
                rootReference.append("(" + objectInstanceName + "->reference)->");
            }
            else {
                rootReference.append("((" + governingClassName + " *) (");
                if (inConstructor){
                    rootReference.append(constructorTempVariable);
                }
                else {
                    rootReference.append("this");
                }
                rootReference.append("->reference))->");
            }
            rootReference.append(formattedVariable);

            text.append(rootReference);
            text.append("->reference");
            
            text.append(" = ");

            text.append(tempVariableAssign + "->reference;\n");  

            // Reuse rootReference to assign type and init
            text.append(rootReference);
            text.append("->numTypes");
            text.append(" = ");
            text.append(tempVariableAssign + "->numTypes;\n");  

            text.append(rootReference);
            text.append("->type");
            text.append(" = ");
            text.append(tempVariableAssign + "->type;\n");  

            text.append(rootReference);
            text.append("->init");
            text.append(" = ");
            text.append(tempVariableAssign + "->init;\n");  

            text.append(rootReference);
            text.append("->numMethods");
            text.append(" = ");
            text.append(tempVariableAssign + "->numMethods;\n");  

            text.append(rootReference);
            text.append("->methodNames");
            text.append(" = ");
            text.append(tempVariableAssign + "->methodNames;\n");  

            text.append(rootReference);
            text.append("->methodPointers");
            text.append(" = ");
            text.append(tempVariableAssign + "->methodPointers;\n");  

            text.append(rootReference);
            text.append("->defaultCopyMethod");
            text.append(" = ");
            text.append(tempVariableAssign + "->defaultCopyMethod;\n");  
        }

        else {
            text.append(tabs + formattedVariable + "->reference");
            text.append(" = ");     
            text.append(tempVariableAssign + "->reference;\n");       

            // Assign type and init
            text.append(tabs + formattedVariable);
            text.append("->numTypes");
            text.append(" = ");
            text.append(tempVariableAssign + "->numTypes;\n");  

            text.append(tabs + formattedVariable);
            text.append("->type");
            text.append(" = ");
            text.append(tempVariableAssign + "->type;\n");  

            text.append(tabs + formattedVariable);
            text.append("->init");
            text.append(" = ");
            text.append(tempVariableAssign + "->init;\n");  

            text.append(tabs + formattedVariable);
            text.append("->numMethods");
            text.append(" = ");
            text.append(tempVariableAssign + "->numMethods;\n");  

            text.append(tabs + formattedVariable);
            text.append("->methodNames");
            text.append(" = ");
            text.append(tempVariableAssign + "->methodNames;\n");  

            text.append(tabs + formattedVariable);
            text.append("->methodPointers");
            text.append(" = ");
            text.append(tempVariableAssign + "->methodPointers;\n");  

            text.append(tabs + formattedVariable);
            text.append("->defaultCopyMethod");
            text.append(" = ");
            text.append(tempVariableAssign + "->defaultCopyMethod;\n");  
        }
                
        // NOTE: Watch this conditional for problems
        // UPDATE: Problem discovered 4/19/2020 - some class variables still being added as local scope variables, creating problems with subsequent references
        // Replaced '!parentNode.getWhatKindOfNode().equals("class")' with '!fromClass' to try to rectify this
        // Update: Problem discovered 3/20/2021 - conditional itself is unnecessary and incorrect; non-class variables can only be assigned to if they are already in scope, meaning they should not be readded

       /* if (!fromClass){
            scopeVariables.get(scopeVariables.size()-1).add(variable);
        } */
	}
	
}