import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

// Implementation for class node
class ClassNode extends ASTNode {
	
	// fields contains both methods and variables
	private ArrayList<ASTNode> fields;
    private String nameOfParent;
    
    // Store information on which fields are initialized and which are nested objects
    // Used to generate code for the initial assignment or for the call to the appropriate constructor
    // Need to be accessible to other ClassNodes for inheritance purposes
    // (i.e., if Bar holds a variable x and Foo inherits Bar, a new Foo needs to initialize its copy of x)
    protected ArrayList<ASTNode> initializationList;
    protected LinkedHashMap<String, String> nestedObjectMap;

    ArrayList<ASTNode> combinedFields;
    ArrayList<ASTNode> combinedInitializationList;
    LinkedHashMap<String,String> combinedNestedObjectMap;
	
	public ClassNode(String type, String name, 
			ArrayList<ASTNode> fs, String p){
		super(type);
		
		//System.out.println("New class node");

		fields = fs;
		nameOfNodeType = name;
		nameOfParent = p;
		whatKindOfNode = "class";
        
        initializationList = new ArrayList<ASTNode>();
        nestedObjectMap = new LinkedHashMap<String,String>();
	}
	
	public void addField(ASTNode newField){
		fields.add(newField);
	}
	
	public String getParentClassName(){
		return nameOfParent;
	}
	
	public ArrayList<ASTNode> getFields(){
		return fields;
	}

	
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking class node");

		setType();
		
		manager.newScope();
		
        // We will use this reference to add fields to the symbol table from this class and its ancestors/superclasses
        Clazz currentClass = myClass;
		
		// Add variables defined for the scope of the class to the symbol table
		TreeMap<String,Clazz> currentClassFields = currentClass.getFields();
		Set<String> fieldNames = currentClassFields.keySet();
		for (String fieldName : fieldNames){

			if (fieldName.equals("self")){
				report.err("\n ERROR: identifier self cannot be redefined as class attribute\n");
                System.exit(1);
			}
            
			if (!table.containsKey(fieldName)){
				manager.addSymbol(table, fieldName, currentClassFields.get(fieldName));
			}
		}
		
		// Add in fields from superclasses
		while (currentClass.getParent() != null){
			Clazz parentClass = currentClass.getParent();
			TreeMap<String,Clazz> parentClassFields = parentClass.getFields();
			Set<String> parentFieldNames = parentClassFields.keySet();
			for (String fieldName : parentFieldNames){
				if (!table.containsKey(fieldName)){
					manager.addSymbol(table, fieldName, parentClassFields.get(fieldName));
				}
			}
			currentClass = parentClass;
		}

        // Typecheck methods and fields
        for (int i = 0; i < fields.size(); ++i){
            
            ASTNode currentMemberNode = fields.get(i);
     
            currentMemberNode.setParent(this);
            
            String currentMemberIs = currentMemberNode.getWhatKindOfNode();
            
            // Typecheck the method or field node
            Clazz memberClass = currentMemberNode.whatsMyType(t, manager, table, report);
            
            if (currentMemberNode.isError()){
                if (currentMemberIs.equals("method")){
                    report.err("\n ERROR: Type error in method of class " + nameOfNodeType + "\n");
                }
                else {
                    report.err("\n ERROR: Type error in field of class " + nameOfNodeType + "\n");
                }
                
                typeError = true;
            }
            
            // Build initialization lists for primitives and composed objects
            // Formerly in generateC, but moved to typechecking step in order to make these lists accessible for all classes in the program
            if (currentMemberIs.equals("assign")){
                nestedObjectMap.put(((AssignNode)currentMemberNode).getVariableName(),((AssignNode)currentMemberNode).getDeclaredTypeName());
                initializationList.add(currentMemberNode);
            }
            
            else if (currentMemberIs.equals("variable")){
                nestedObjectMap.put(((VarNode)currentMemberNode).getVariableName(),((VarNode)currentMemberNode).getDeclaredTypeName());
            }
        }
		
        // This was moved here from the generateC method so that the order will be correct for subclasses adding their parents' attributes
        Collections.reverse(fields);
        
        // Clear inner scope from table; restore outer scope if it exists
		manager.restorePrevious(table);
        
		t.log_error(typeError);
		System.out.println("Class node: Type = " + nameOfNodeType);
		return myClass;
    }
	
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        
        StringBuilder tabs = super.addTabs(scopeDepth);
        
        // Ensure that 'this' pointer is seen in the class scope
        scopeVariables.add(new ArrayList<String>());
        scopeVariables.get(scopeVariables.size()-1).add("self");

        for (ASTNode field: combinedFields){
            
            // Get names of all fields
            String nameOfField = field.getVariableName();
            classMemberVariables.add(nameOfField);
        }
        
        // Generate a constructor for the class
        generateConstructor(text, tabs, combinedInitializationList, combinedNestedObjectMap, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

        // Generate a copy method for the class
        generateCopyMethod(text, scopeDepth, classMemberVariables, scopeVariables, resolvedCollisions);
        
        // Generate code for all methods belonging to the class
		for (ASTNode field: fields){
			if (!field.getWhatKindOfNode().equals("method")){
				continue;
			}
			field.generateC(text, nameOfNodeType, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);
		}
        
        scopeVariables.remove(scopeVariables.size()-1);
	}
    
    public void generateStructDefinition(StringBuilder text, int scopeDepth, ArrayList<String> classMemberVariables,ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions){

        // Use these to contain the generated code under construction
		StringBuilder classC = new StringBuilder("");
        StringBuilder tabs = super.addTabs(scopeDepth);

        // Define data structure for class
        classC.append("struct " + nameOfNodeType + "\n{");
                
        // We need to account for all of the attributes inherited from this class' superclasses
        // We get the ClassNode corresponding to each ancestor, and grab its listed class member variables,
        // as well as its list of ASTNodes for initializing them
        RootNode root = (RootNode) this.parentNode;
        HashMap<String, ClassNode> classMap = root.classMap;
        
        Clazz ancestorClass = myClass.getParent();
        String ancestorClassName = ancestorClass.getClassName();
        
        combinedFields = new ArrayList<ASTNode>();
        combinedInitializationList = new ArrayList<ASTNode>(this.initializationList);
        combinedNestedObjectMap = new LinkedHashMap<String,String>(this.nestedObjectMap);

        while (!ancestorClassName.equals("Object") && !ancestorClassName.equals("IO")){
            ClassNode ancestorClassNode = classMap.get(ancestorClassName);
            ArrayList<ASTNode> ancestorFields = new ArrayList<ASTNode> (ancestorClassNode.getFields());
            
            // This is to ensure the order is correct in the final list
            Collections.reverse(ancestorFields);
            
            combinedFields.addAll(ancestorFields);
            combinedInitializationList.addAll(ancestorClassNode.initializationList);
            combinedNestedObjectMap.putAll(ancestorClassNode.nestedObjectMap);
            
            ancestorClass = ancestorClass.getParent();
            ancestorClassName = ancestorClass.getClassName();
        }
        
        // These are to ensure that the order ends up correct when the code is generated, and that the fields are listed in order from highest-order superclass to lowest
        Collections.reverse(combinedInitializationList);
        
        // This was changed from a list to a map, and can no longer be reversed directly - handled at point of iteration in generateConstructor
        // Collections.reverse(combinedNestedObjectMap);
        Collections.reverse(combinedFields);
        
        combinedFields.addAll(fields);
        
		for (ASTNode field: combinedFields){
            
            // Get names of all fields
            String nameOfField = field.getVariableName();
            
            String fieldNodeIs = field.getWhatKindOfNode();
			if (fieldNodeIs.equals("method")){
				continue;
			}
            
			classC.append("\n");
            classC.append(tabs);
            
            // Handle type for declaration of fields
            String declaredType = field.getDeclaredTypeName();
            
			classC.append("_Shell* ");
			
            // Changes case on certain variable names that are acceptable in COOL but not in C
            String validFieldName = getValidIdentifier(nameOfField, resolvedCollisions, classMemberVariables, scopeVariables, false);
            classC.append(validFieldName + ";");
		}
        
        // NOTE: Keep an eye on this for whether it's 100% what you want in all situations (i.e., assignments/initializations)
		if (classC.charAt(classC.length()-1) == ','){
			classC.deleteCharAt(classC.length()-1);
		}
        
        // Add built-in self-referencing field - used to have an identically-named field across all base-level structs
        classC.append("\n");
        classC.append(tabs);
        classC.append(nameOfNodeType + "** _data" + ";");

        // Close data structure definition
		classC.append("\n};\n\n");
        
        // Attach code generated for class to that generated for the entire COOL program
        text.append(classC);

    }

    // Handles definition of constructor for class - public method to return an initialized struct of the appropriate type - invoked above in generateC
    private void generateConstructor(StringBuilder text, StringBuilder tabs, ArrayList<ASTNode> initializationList, HashMap<String,String> nestedObjectMap, ArrayList<String> classMemberVariables,ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
                                        
        // Handles definition of constructor for class - public method to return an initialized struct of the appropriate type
        text.append("_Shell* " + nameOfNodeType + "_" + nameOfNodeType + "()\n{\n");

        String tempVariableClass = getNextTempVariable(whatKindOfNode);

        setInConstructor(tempVariableClass);

        text.append(tabs);
        text.append("_Shell* " + tempVariableClass + " = " +  "_newShellReference();\n");

		ArrayList<String> inheritedTypes = new ArrayList<String>();
		inheritedTypes.add(nameOfNodeType);
		Clazz currentClass = myClass;        

		while (currentClass.getParent()!= null){
			currentClass = currentClass.getParent();
            inheritedTypes.add(currentClass.getClassName());
        }
        
        text.append(tabs);
        text.append(tempVariableClass + "->numTypes" + " = " + inheritedTypes.size() + ";\n");        

        text.append(tabs);
        text.append(tempVariableClass + "->type" + " = malloc(" + inheritedTypes.size() + "* sizeof(_TYPE));");

		// Append class type and all inherited types
		for (int i = 0; i < inheritedTypes.size(); ++i){
			text.append("\n" + tabs + tempVariableClass + "->type[" + i + "] = _" + inheritedTypes.get(i).toUpperCase() + ";");
		}

        text.append(tabs);
        text.append(tempVariableClass + "->init" + " = true;\n\n");

        text.append(tabs);
        text.append(tempVariableClass + "->reference" + " = malloc(sizeof(" + nameOfNodeType + "));\n");

        // Handle method names and method pointers
        text.append(tabs);
        text.append(tempVariableClass + "->numMethods" + " = _" + nameOfNodeType.toUpperCase() + "_NumMethods;\n");

        text.append(tabs);
        text.append(tempVariableClass + "->methodNames" + " = _" + nameOfNodeType.toUpperCase() + "_MethodNames;\n");

        text.append(tabs);
        text.append(tempVariableClass + "->methodPointers" + " = _" + nameOfNodeType.toUpperCase() + "_MethodPointers;\n");

        text.append(tabs);
        text.append(tempVariableClass + "->defaultCopyMethod" + " = _" + nameOfNodeType.toUpperCase() + "_DefaultCopyMethod;\n");

        // Initialize built-in _data field, which self-references the struct and acts as a 'this' pointer
        text.append(tabs);
        text.append("((" + nameOfNodeType + " *) " + tempVariableClass + "->reference)" + "->_data" + " = malloc(sizeof(" + nameOfNodeType + "*));\n");
        text.append(tabs);
        text.append("((" + nameOfNodeType + " *) " + tempVariableClass + "->reference)" + "->_data" + " = " + "(" + nameOfNodeType + "**)" + "&(" + tempVariableClass + "->reference)" +";\n");
        
        // Generates code calling the appropriate constructor for all fields flagged as being nested objects above
        ArrayList<String> reversedNestedObjects = new ArrayList<String>(nestedObjectMap.keySet());
        Collections.reverse(reversedNestedObjects);
        for (String nestedObjectName : reversedNestedObjects){
            String nestedObjectType = nestedObjectMap.get(nestedObjectName);

            String validNestedName = getValidIdentifier(nestedObjectName, resolvedCollisions, classMemberVariables, scopeVariables, false);
            
            // Note: Modified this to use new object shell struct syntax on 5/24/2020               
            if (nestedObjectType.equals("Bool") || nestedObjectType.equals("Int") || nestedObjectType.equals("String")){
                text.append(tabs);
                text.append("(*((" + nameOfNodeType + " *) " + tempVariableClass + "->reference)" + "->_data)->" + validNestedName + " = " + nestedObjectType + "_" + nestedObjectType + "()" + ";\n"); 
            }
           else {
                String tempVariableNested = getNextTempVariable("constructor");
                text.append(generateMemoryAllocationOnlyForObject(tabs, tempVariableNested, nestedObjectType));		
                text.append("\n" + tabs + "(*((" + nameOfNodeType + " *) " + tempVariableClass + "->reference)" + "->_data)->" + validNestedName + " = " + tempVariableNested + ";\n");                 
            }
        }
        
        // Generates code in the constructor that assigns initial values to all fields flagged as needing initialization above
        for (ASTNode initNode : initializationList){
            ((AssignNode) initNode).setObjectInstanceName("(" + nameOfNodeType + " *) " + tempVariableClass);
            initNode.generateC(text, nameOfNodeType, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

            text.append("\n"); 
        }
     
        // Return initialized struct and close constructor definition
        text.append(tabs + "return " + tempVariableClass + ";");
        text.append("\n};\n\n");

        setNotInConstructor();

    }

    // Handles definition of 'hidden' method for copy contents of a class struct
    private void generateCopyMethod(StringBuilder text, int scopeDepth, ArrayList<String> classMemberVariables,ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions){
        StringBuilder tabs = super.addTabs(scopeDepth);

        text.append("void " + "_" + nameOfNodeType + "_copy(_Shell* _src, _Shell* _dest){\n"); 

        text.append(tabs);
        text.append("_dest->reference = malloc(sizeof(" + nameOfNodeType + "));\n");

		for (ASTNode field: combinedFields){

            // Get names of all fields
            String nameOfField = field.getVariableName();
            String validFieldName = getValidIdentifier(nameOfField, resolvedCollisions, classMemberVariables, scopeVariables, false);

            String fieldNodeIs = field.getWhatKindOfNode();
			if (fieldNodeIs.equals("method")){
				continue;
			}

            text.append(tabs);
            text.append("((" + nameOfNodeType + " *) _dest->reference)->_data = malloc(sizeof(" + nameOfNodeType + "*));\n");

            text.append(tabs);
            text.append("((" + nameOfNodeType + " *) _dest->reference)->_data = (" + nameOfNodeType + " **) &(_dest->reference);\n"); 

            text.append(tabs);
            text.append("(*((" + nameOfNodeType + " *) _dest->reference)->_data)->" + validFieldName + " = (*((" + nameOfNodeType + " *) _src->reference)->_data)->" + validFieldName + ";\n"); 
		}        

        text.append("}\n\n"); 

    }    
}