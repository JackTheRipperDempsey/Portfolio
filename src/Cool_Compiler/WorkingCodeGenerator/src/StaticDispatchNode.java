import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

// Implementation for static dispatch node
class StaticDispatchNode extends ASTNode {
	
    // Static dispatch syntax makes it unambiguous that the method being called is inherited from an ancestor class
	private Clazz callingClass;
	private Clazz ancestorClass;
	private String ancestorType;
	private ArrayList<ASTNode> args;
	private ASTNode callingExpression;

	private String nameOfMethod;
	
    // Only need one constructor because there is only one accepted syntactical form of static dispatch
	public StaticDispatchNode(String type, ASTNode expr, ArrayList<ASTNode> arguments, 
			String name, String aType){
		super(type);
		
		//System.out.println("New static dispatch node");
		args = arguments;
		nameOfMethod = name;
		ancestorType = aType;
		whatKindOfNode = "static_dispatch";
		callingExpression = expr;
	}
    
    // Used to access the name of the method being dispatched
    public String getNameOfMethod(){
        return nameOfMethod;
    }
    
    // Used to access the arguments of the method being dispatched
    public ArrayList<ASTNode> getArgs(){
        return args;
    }
    
	// Typecheck static dispatch node - should be the return type of the method being called
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking static dispatch node");

		setType();
        
        // Determine the type of the calling expression and that type's ancestor type
		callingExpression.setParent(this.getParent());
		callingClass = callingExpression.whatsMyType(t,manager,table, report);
		ancestorClass = classes.get(ancestorType);
		
        // Derive calling class if it is not explicitly named in the call
		if (callingClass == null){
			callingClass = classes.get(nameOfNodeType);
		}
		
		// Check to make sure we have an actual superclass-subclass relationship
		Clazz currentClass = callingClass;
				
		while(currentClass.getParent() != null){
			if (currentClass.equals(ancestorClass)){
				break;
			}
			currentClass = currentClass.getParent();
		}
				
		if (!currentClass.equals(ancestorClass)){
			typeError = true;
			report.err("\n ERROR: Class " + callingClass.getClassName() +
					" not a subclass of " + ancestorClass.getClassName() + "\n");
		}
        
        
		// Make sure method exists for class or one of its ancestors
        boolean methodNotFound = false;
		TreeMap<String,ArrayList<Clazz>> currentClassMethods = currentClass.getMethods();
		while (!currentClassMethods.containsKey(nameOfMethod)){
            
            // We have arrived at the root of the class structure, and we still can't find the method
			if (currentClass.getParent() == null){
				typeError = true;
                methodNotFound = true;
				report.err("\n ERROR: Can't find method " + nameOfMethod
						+ " in class " + callingClass.getClassName() + "\n");
			}
			
			else{
				currentClass = currentClass.getParent();
				currentClassMethods = currentClass.getMethods();
			}
		}
		
        if (!methodNotFound){
            // Handle return type of method
            ArrayList<Clazz> declaredTypes = currentClassMethods.get(nameOfMethod);
		
            // Check if return type is SELF_TYPE, in which case we know that the return type must equal the type of the calling class
            if (declaredTypes.get(0).getClassName().equals("SELF_TYPE")){
                myClass = callingClass;
            }
		
            // Otherwise, set return type based on first declared type from list of known types associated with the method
            else{
                myClass = declaredTypes.get(0);
            }
		
            System.out.println(declaredTypes.size());
            System.out.println(args.size());
		
            // Typecheck all argument nodes and assign type and parent class
            for (int i = 1; i < declaredTypes.size(); ++i){
                ASTNode nextArg = args.get(i-1);
                Clazz declaredType = declaredTypes.get(i);
            
                nextArg.setParent(this.getParent());
                Clazz argType = nextArg.whatsMyType(t, manager, table, report);
            
                // Ensure that the actual type of each argument is either the same as the declared type, or a valid subtype
                boolean subType = false;
            
                currentClass = argType;
            
                // Note: This loop formerly used 'currentClass.getParent() != null' as its condition
                // However, that discounted the possibility that the declared type would be Object (at the very root of the type structure), in which case ANY valid type would be a valid subtype; because the conditional check occurs at the very beginning of the loop (before the start of an iteration), this would preempt the final check of declaredType against current Class; as such the loop has been modified to this format
                boolean atRoot = false;
                while(!subType){
                    if (declaredType.equals(currentClass)){
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
                    report.err("\n ERROR: Declared type of argument "
                                       + declaredType.getClassName() +
                                       " does not match actual type " + argType.getClassName()
                                       + " in method " + nameOfMethod + "\n");
                }
            }
        }
		
        nameOfNodeType = myClass.getClassName();
        t.log_error(typeError);
		System.out.println("Static dispatch node: Type = " + nameOfNodeType);
		return myClass;
	}
  
    // Generate code for the method call
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth)
	{
        // Add any necessary tabs
		Collections.reverse(args);
        StringBuilder tabs = super.addTabs(scopeDepth);
		
        String tempVariableStatic = getNextTempVariable(whatKindOfNode);
        String tempVariableArgs = getNextTempVariable("argList");

        cValueOrReference = tempVariableStatic;
        text.append(ASTNode.generateInitializationForObject(tabs, tempVariableStatic, nameOfNodeType));

        ArrayList<String> tempVarArguments = new ArrayList<String>();

        for (int i = 0; i < args.size(); ++i){
            ASTNode arg = args.get(i);
            
            arg.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
            tempVarArguments.add(arg.getCValueForNode());
        }

        callingExpression.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

        text.append("\n" + tabs + "_Shell** " + tempVariableArgs + ";");
        if (tempVarArguments.size() > 0){
            text.append("\n" + tabs + tempVariableArgs + " = (_Shell**) malloc(" + tempVarArguments.size() + "* sizeof(_Shell*));");
        }

        for (int i = 0; i < tempVarArguments.size(); ++i){
            text.append("\n" + tabs + tempVariableArgs + "[" + i + "] = " + tempVarArguments.get(i) + ";\n");
        }

        text.append("\n" + tabs + tempVariableStatic + " = _staticDispatch(\"" +  ancestorClass.getClassName() + "_" + nameOfMethod + "\", ");

        text.append(callingExpression.getCValueForNode());

        // Close dispatch statement
        text.append(", " + tempVariableArgs + ");");
	}
    
}