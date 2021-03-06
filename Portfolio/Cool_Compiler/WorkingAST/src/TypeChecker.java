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
import java.util.Set;
import java.util.TreeMap;

public class TypeChecker {

	private TableManager manageT;
	public TypeChecker(TableManager tManage) {
		manageT = tManage;
	}
	
	public void checkTypes(RootNode root){
		
		TypeError error = new TypeError(10);
		
		HashMap<String, Clazz> clazzes = new HashMap<String,Clazz>();
		HashMap<String, Clazz> variables = new HashMap<String,Clazz>();
		
		ObjectClazz object = new ObjectClazz("Object", null);
		CoolString string = new CoolString("String", object);
		Int integer = new Int("Int", object);
		SELF_TYPE self = new SELF_TYPE("SELF_TYPE", object);
		IOClazz io = new IOClazz("IO", object);
		Bool bool = new Bool("Bool", object);
		
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
		
		clazzes.put(object.getClassName(),object);
		clazzes.put(self.getClassName(),self);
		clazzes.put(io.getClassName(),io);
		clazzes.put(string.getClassName(),string);
		clazzes.put(integer.getClassName(),integer);
		clazzes.put(bool.getClassName(),bool);
				
		ArrayList<ClassNode> classNodes = root.getClasses();
		
		for (int i = 0; i < classNodes.size(); ++i){
			UserClass newClass = 
					new UserClass(classNodes.get(i).getTypeName(),object);
			
			clazzes.put(classNodes.get(i).getTypeName(), newClass);
		}
		
		
		for (int i = 0; i < classNodes.size(); ++i){
			
			clazzes.get(classNodes.get(i).getTypeName()).setParent(clazzes.get(classNodes.get(i).getParentClass()));
			
			ArrayList<ASTNode> internals = classNodes.get(i).getFields();
			
			for (int j = 0; j < internals.size(); ++j){
				if (internals.get(j).getNodeType() == "method"){
					ArrayList<Clazz> args = new ArrayList<Clazz>();
					
					if (!clazzes.containsKey(internals.get(j).getTypeName())){
						System.out.println("Return type " + internals.get(j).getTypeName()
								+ " does not refer to actual class");
						System.exit(1);
					}
					
					args.add(clazzes.get(internals.get(j).getTypeName()));
					
					ArrayList<String> as = internals.get(j).getArgs();
					for (int a = 0; a < as.size(); ++a){
						if (!clazzes.containsKey(as.get(a))){
							System.out.println("Argument " + as.get(a)
									+ " does not refer to actual class");
							System.exit(1);
						}
					}
					clazzes.get(classNodes.get(i).getTypeName()).addMethod(internals.get(j).getMethodName(), 
							args);
				}
				
				else {
					if (!clazzes.containsKey(internals.get(j).getTypeName())){
						System.out.println("Variable type " + internals.get(j).getTypeName()
								+ " does not refer to actual class");
						System.exit(1);
					}
					
					clazzes.get(classNodes.get(i).getTypeName()).addField(internals.get(j).nameOfVar(), 
							clazzes.get(internals.get(j).getTypeName()));
				}
			}
			
		}
		
		if (!clazzes.containsKey("Main")){
			System.out.println("Error: No Main class");
			System.exit(1);
		}
		
		else {
			if (!clazzes.get("Main").getMethods().containsKey("main")){
				System.out.println("Error: Main class does not contain a method main");
				System.exit(1);
			}
		}
		
		Set<String> set = clazzes.keySet();
		for (String s : set){
			Clazz curr = clazzes.get(s);
			Clazz par;
			if (curr.getParent() != null){
				par = curr.getParent();
				
				if (par.equals(curr)){
					System.out.println("Class " + curr.getClassName() +
							" illegally set as own parents");
					System.exit(1);
				}
				
				while (par.getParent() != null){
					par = par.getParent();
					if (par.equals(curr)){
						System.out.println("Classes " + curr.getClassName() +
								" and " + par.getClassName() + " form illegal circular inheritance loop" );
						System.exit(1);
					}
				}
			}
		}
		
		root.whatsMyType(clazzes, error, manageT, variables);
		
		System.out.println("Typechecking complete");
		
		if (error.howManyErrors() == 0){
			System.out.println("No errors reported");
		}
		
		else {
			System.out.println(error.howManyErrors() + " type errors reported");
		}
	}
	
}
