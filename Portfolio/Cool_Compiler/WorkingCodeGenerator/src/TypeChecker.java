/*
 * The TypeChecker class has two purposes.  The first one is exactly 
 * what its name implies: Given the root node of an abstract syntax tree, 
 * it will call that node's function returning its type (represented 
 * by a Clazz object).  During the course of that call, each of the other 
 * nodes in trees will typecheck themselves as well.
 * 
 * Other forms of typechecking here include ensuring that we do not 
 * have a circular inheritance loop in our Clazz's, that each Cool file 
 * has a Main class, and that all variables and method arguments refer to
 * recognized Clazz's.
 * 
 * The other purpose of the TypeChecker class is to fill in the tables
 * maps containing our methods and their arguments, and then adding these 
 * to each Clazz as appropriate.  This includes the builtin methods for 
 * the builtin Clazz's, as well as the methods and fields of user-defined
 * Clazz's.  These are then used in typechecking to ensure that all methods 
 * that are called are defined for the appropriate class being used to call 
 * them.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TypeChecker {

    // This acts as a symbol table - from here, it gets passed to the whatsMyTypeMethod of the COOL program being compiled's root node
	private TableManager manageT;
	public TypeChecker(TableManager tManage) {
		manageT = tManage;
	}
	
    // This does some preliminary setup and then performs typechecking on the abstract syntax tree, starting at the root node
	public void checkTypes(RootNode root, HashMap<String, Clazz> clazzes, HashMap<String, Clazz> variables, HashMap<String, ArrayList<String>> methods, ErrorReport report){
		
        // Sets maximum allowable number of type errors; typechecking will end if we go past that
		TypeError error = new TypeError(10);
				
        // Define an example object for each default type, and add the names of their built-in methods and those methods' return values and arguments
		ObjectClazz object = new ObjectClazz(null);
		CoolString string = new CoolString(object);
		Int integer = new Int(object);
		SELF_TYPE self = new SELF_TYPE(object);
		IOClazz io = new IOClazz(object);
		Bool bool = new Bool(object);
		
		ArrayList<Clazz> arguments = new ArrayList<Clazz>();
				
		arguments.add(object);
		
		object.addMethod("abort", arguments);
		
		arguments.clear();
		
		arguments.add(string);
		object.addMethod("type_name",arguments);
		arguments.clear();
		
		arguments.add(self);
		object.addMethod("copy",arguments);
		arguments.clear();
		
		arguments.add(self);
		arguments.add(string);
		io.addMethod("out_string",arguments);
		arguments.clear();
				
		arguments.add(self);
		arguments.add(integer);
		io.addMethod("out_int",arguments);
		arguments.clear();
		
		arguments.add(string);
		io.addMethod("in_string",arguments);
		arguments.clear();
		
		arguments.add(integer);
		io.addMethod("in_int",arguments);
		arguments.clear();
		
		arguments.add(integer);
		string.addMethod("length",arguments);
		arguments.clear();
		
		arguments.add(string);
		arguments.add(string);
		string.addMethod("concat",arguments);
		arguments.clear();
		
		arguments.add(string);
		arguments.add(integer);
		arguments.add(integer);
		string.addMethod("substr",arguments);
		arguments.clear();
		
        // With built-in methods added, add default class objects to list of known classes for the program being compiled
		clazzes.put(object.getClassName(),object);
		clazzes.put(self.getClassName(),self);
		clazzes.put(io.getClassName(),io);
		clazzes.put(string.getClassName(),string);
		clazzes.put(integer.getClassName(),integer);
		clazzes.put(bool.getClassName(),bool);
				
        // Add class names and a placeholder object for each user-defined class to list of known classes for the program being compiled, and also ensure that no classes are redefined
		ArrayList<ClassNode> classNodes = root.getClasses();
        HashSet<String> classNames = new HashSet<String>(clazzes.keySet());
        HashSet<String> repeatedNames = new HashSet<String>();
		
        // This loop needs to run before the next one over classNodes because that way all classes will be known at the time parent classes are assigned (and also because this way all classes will be checked for redefinition)
		 for (int i = 0; i < classNodes.size(); ++i){
            String newClassName = classNodes.get(i).getTypeName();
			UserClass newClass = new UserClass(newClassName,object);
			clazzes.put(newClassName, newClass);
            
             // Store the names of all user classes and track attempted redefinitions
            if (!classNames.contains(newClassName)){
                classNames.add(newClassName);
            }
            
            else {
                repeatedNames.add(newClassName);
            }
		}
        
        // Ensure that classes are not redefined
        if (repeatedNames.size() > 0){
            String errorMessage = "Class redefinition is not permitted. Aborting compilation.\n";
            errorMessage += "Classes that were attempted to be redefined: \n";
            for (String repeatedClassName : repeatedNames){
                errorMessage += repeatedClassName + "\n";
            }
            report.err(errorMessage);
            System.exit(1);
        }
		
		// For each class node, get names and types of known fields and methods (with arguments and return values)
		for (int i = 0; i < classNodes.size(); ++i){
            
            ClassNode currentClassNode = classNodes.get(i);
            String currentClassName = currentClassNode.getTypeName();
            Clazz currentClass = clazzes.get(currentClassName);
            Clazz parentClass = clazzes.get(currentClassNode.getParentClassName());
            
            // Ensure that String, Int, and Bool are not inherited from
            // It is acceptable here to use the '==' operator because we are doing a simple reference check against the defined instance of the Int, CoolString, and Bool Clazzes
            if (parentClass == bool || parentClass == integer || parentClass == string){
                report.err("Error: it is illegal for a user-defined class to inherit from Int, Bool, or String");
                report.err("Error: Class " + currentClassName + " attempted to inherit from " + parentClass.getClassName());
                System.exit(1);
            }
            
            currentClass.setParent(parentClass);
			
			ArrayList<ASTNode> internals = currentClassNode.getFields();
			
			for (int j = 0; j < internals.size(); ++j){
                
                // Check that each defined method's return type and argument types refer to known class names, and that each argument identifier is distinct
				if (internals.get(j).getWhatKindOfNode() == "method"){

                    MethodNode method = (MethodNode) internals.get(j);
                    
                    // Check that argument names are not reused
                    ArrayList<String> argumentNames = method.getArgNames();
                    HashSet<String> foundNames = new HashSet<String>();
                    HashSet<String> reusedNames = new HashSet<String>();
                    
                    for (String argName : argumentNames){
                        if (!foundNames.contains(argName)){
                            foundNames.add(argName);
                        }
                        else {
                            reusedNames.add(argName);
                        }
                    }
                    
                    if (!reusedNames.isEmpty()){
                        report.err("Error: Use of non-distinct identifiers in the argument list for method " + method.getMethodName() + " in class " + currentClassName);
                        report.err("Following argument names were reused within the same argument list: ");
                        for (String argName : reusedNames){
                            report.err(argName);
                        }
                        System.exit(1);
                    }
                    
                    // Check that argument and return types are valid
                    String methodReturnType = method.getTypeName();
                    
					ArrayList<Clazz> argumentTypes = new ArrayList<Clazz>();
					
					if (!clazzes.containsKey(methodReturnType)){
						report.err("Return type " + methodReturnType + " does not refer to actual class");
						System.exit(1);
					}

					// Keep track of argument/type names to generate header at top of .c file
					ArrayList<String> newMethodArgs = new ArrayList<String>();					
					argumentTypes.add(clazzes.get(methodReturnType));
					
					ArrayList<String> argumentTypeNames = method.getArgTypes();
					for (int arg = 0; arg < argumentTypeNames.size(); ++arg){
						if (!clazzes.containsKey(argumentTypeNames.get(arg))){
							report.err("Argument " + argumentTypeNames.get(arg) + " does not refer to actual class");
							System.exit(1);
						}
						argumentTypes.add(clazzes.get(argumentTypeNames.get(arg)));
						newMethodArgs.add(argumentNames.get(arg));
					}
					currentClass.addMethod(method.getMethodName(),argumentTypes);

					// Use this to track name of class and method
					String compoundName = currentClassName + "_" + method.getMethodName();
					methods.put(compoundName, newMethodArgs);
				}
				
				else {
                    
                    // Check that each defined class field is typed to a known class
                    ASTNode field = internals.get(j);
                    String fieldType = field.getTypeName();
                    
					if (!clazzes.containsKey(fieldType)){
						report.err("Variable type " + fieldType + " does not refer to actual class");
						System.exit(1);
					}
					
					currentClass.addField(field.getVariableName(),clazzes.get(fieldType));
				}
			}
			
		}
		
        // Ensure each class has a Main class with a defined main method
		if (!clazzes.containsKey("Main")){
			report.err("Error: No Main class");
			System.exit(1);
		}
		
		else {
			if (!clazzes.get("Main").getMethods().containsKey("main")){
				report.err("Error: Main class does not contain a method main");
				System.exit(1);
			}
		}
        
        
        // Ensure that there are no illegal inheritance cycles
        
        // It should be safe to iterate directly over clazzes.values() because an earlier step in Cool.java has already checked for attempted class redefinition
        for (Clazz nextClass : clazzes.values()){
			Clazz parentClass;
			HashSet<String> allParentFields = new HashSet<String>();

			if (nextClass.getParent() != null){
				parentClass = nextClass.getParent();
				allParentFields.addAll(parentClass.getFields().keySet());

				if (parentClass.equals(nextClass)){
					report.err("Class " + nextClass.getClassName() + " illegally set as own parents");
					System.exit(1);
				}
				
				while (parentClass.getParent() != null){
					parentClass = parentClass.getParent();
					allParentFields.addAll(parentClass.getFields().keySet());

					if (parentClass.equals(nextClass)){
						report.err("Classes " + nextClass.getClassName() +
								" and " + parentClass.getClassName() + " form illegal circular inheritance loop" );
						System.exit(1);
					}
				}
			}

			// Make sure that Class hasn't redefined parent attribute
			Set<String> currentClassFieldNames = nextClass.getFields().keySet();
			for (String fieldName : currentClassFieldNames){
				if (allParentFields.contains(fieldName)){
						report.err("Class attribute " + fieldName + " illegally redefined in class " + nextClass.getClassName());
						System.exit(1);
				}
			}
		}
		
		// Check the type of the root node, which will in turn check the types of all nodes beneath it in the abstract syntax tree
		root.setClassMap(clazzes);
		root.whatsMyType(error, manageT, variables, report);
		
		System.out.println("Typechecking complete");
		
		if (error.howManyErrors() == 0){
			System.out.println("No errors reported");
		}
		
		else {
			report.err(error.howManyErrors() + " type errors reported");
			System.exit(1);
		}
	}
	
}
