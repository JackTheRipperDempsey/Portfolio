import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

// Implementation for dispatch node
class DispatchNode extends ASTNode {
	
    //  One of these serves to reference the class of the expression that is currently calling the method being dispatched, and the other serves to reference the class where the method is defined (which may be an ancestor of the former)
	private Clazz classMethodBelongsTo;
	private Clazz callingClass;
    
    // Arguments and method being invoked
	private ArrayList<ASTNode> args;
	private String nameOfMethod;
    
    // If there is not a simple object instance calling the method, but a complex expression that needs to be evaluated, this will be populated
	private ASTNode callingExpression;
	
	// We need two constructors, based on the syntax for the dispatch statement

    // This constructor is used if the call is made on a simple object instance
	public DispatchNode(String type, ArrayList<ASTNode> arguments, String name){
		super(type);
		
		//System.out.println("New dispatch node");
		args = arguments;
		nameOfMethod = name;
		whatKindOfNode = "dispatch";
		callingExpression = null;
	}
	
    // This constructor is used if the call is made on an expression that needs to be evaluated first, rather than on a simple object instance
	public DispatchNode(String type, ASTNode expr, ArrayList<ASTNode> arguments, String name){
		super(type);
		
		//System.out.println("New dispatch node");
		args = arguments;
		nameOfMethod = name;
		whatKindOfNode = "dispatch";
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
	
    // Determine the type of the dispatch node - should be the return type of the method being called
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking dispatch node");

		// Check to see if this is a local dispatch/static in Java terms
		if (nameOfNodeType.equals("local")){
			ASTNode findClassNode = this.getParent();

			while(!findClassNode.getWhatKindOfNode().equals("class")){
				findClassNode = findClassNode.getParent();
			}
			
			nameOfNodeType = findClassNode.getTypeName();
            
             // Ensure callingClass is populated if there is a simple object reference making the call
            callingClass = classes.get(nameOfNodeType);
		}
		
        // If there is a complex expression making the method call, we need to evaluate the type of that expression before proceeding
		else if ((callingExpression != null)){
			callingExpression.setParent(this.getParent());
			callingClass = callingExpression.whatsMyType(t,manager,table, report);
		}
		
		setType();
		
		Clazz currentClass = callingClass;
		
		// Check to see if method is defined for immediate class or if it is inherited from one of its ancestors
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
            classMethodBelongsTo = currentClass;
        
            // Typecheck arguments to method, including return type, based on known types of method (taken from map of types of arguments of methods owned by parent class)
            ArrayList<Clazz> declaredTypes = currentClassMethods.get(nameOfMethod);
        
            // Check if return type is SELF_TYPE, in which case we know that the return type must equal the type of the calling class
            if (declaredTypes.get(0).getClassName().equals("SELF_TYPE")){
                myClass = callingClass;
            }
		
            // Otherwise, set return type based on first declared type from list of known types associated with the method
            else{
                myClass = declaredTypes.get(0);
            }
        
            // Typecheck all argument nodes and assign type and parent class
            System.out.println("Number of arguments: " + args.size());
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
            
                // Log any type errors found with the arguments to the dispatch statement
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
		System.out.println("Dispatch node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // Generate code for the method call
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth)
	{
        // Add any necessary tabs
        Collections.reverse(args);
        StringBuilder tabs = super.addTabs(scopeDepth);
        
        // Generate signature for calling class
        String tempVariableDispatch= getNextTempVariable(whatKindOfNode);
        String tempVariableArgs = getNextTempVariable("argList");

        cValueOrReference = tempVariableDispatch;
        text.append(ASTNode.generateMemoryAllocationOnlyForObject(tabs, tempVariableDispatch, nameOfNodeType));

        ArrayList<String> tempVarArguments = new ArrayList<String>();

        for (int i = 0; i < args.size(); ++i){
            ASTNode arg = args.get(i);
            
            arg.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
            tempVarArguments.add(arg.getCValueForNode());
        }

        if (callingExpression != null){
            callingExpression.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
        }
        
        text.append("\n" + tabs + "_Shell** " + tempVariableArgs + ";");
        if (tempVarArguments.size() > 0){
            text.append("\n" + tabs + tempVariableArgs + " = (_Shell**) malloc(" + tempVarArguments.size() + "* sizeof(_Shell*));");
        }

        for (int i = 0; i < tempVarArguments.size(); ++i){
            text.append("\n" + tabs + tempVariableArgs + "[" + i + "] = " + tempVarArguments.get(i) + ";\n");
        }

        text.append("\n" + tabs + tempVariableDispatch + " = _dispatch(\"_" + nameOfMethod + "\", ");


        if (callingExpression != null){
            text.append(callingExpression.getCValueForNode());
        }
        else {
            if (inConstructor){
                text.append(constructorTempVariable);
            }
            else {
                text.append("this");
            }
        }

        // Close dispatch statement
        text.append(", " + tempVariableArgs + ");");

    }
}