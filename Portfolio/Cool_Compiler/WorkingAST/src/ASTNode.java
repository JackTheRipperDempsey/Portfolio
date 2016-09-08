/*
 * The ASTNode class is the foundation of our abstract syntax tree. 
 * Here we have the abstract class for a basic ASTNode, as well as 
 * implementations of that for different kinds of nodes in our tree 
 * (e.g., int literal nodes, method nodes, etc).
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;

abstract public class ASTNode {
	
	// typeError records whether there was a type error reported in checking this node
	protected boolean typeError;
	// myClass provides a record for type assigned to the node
	protected Clazz myClass;
	// nameOfNodeType is used to return the name of the Clazz attached to a node
	protected String nameOfNodeType;
	
	// parentNode is set upon creation of the node
	ASTNode parentNode = null;
	/* whatKindofNode is different than nameOfNodeType in that
	 it is used to record whether the node is a root, a class, etc.
	 
	 This is needed because of the need for some node's to trace their 
	 way back to the calling method or class */
	protected String whatKindOfNode;
	
	
	// This is our method for typechecking our node and by extension all of its children
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t,
			TableManager manager, HashMap<String,Clazz> table) {
		return myClass;
	}

	public boolean isError(){
		return typeError;
	}
	
	public void setType(HashMap<String,Clazz> classes){
		myClass = classes.get(nameOfNodeType);
	}
	
	public String getTypeName(){
		return nameOfNodeType;
	}
	
	public String getNodeType(){
		return whatKindOfNode;
	}
	
	public void setParent(ASTNode a){
		parentNode = a;
	}
	
	public ASTNode getParent(){
		return parentNode;
	}
	
	public ASTNode(String type){
		nameOfNodeType = type;
		typeError = false;
	}
	
	public String nameOfVar(){
		return "no variable in this node; this was returned in error";
	}

	public ArrayList<String> getArgs() {
		return null;
	}

	public String getMethodName() {
		return null;
	}

}

// Implementation for root node
class RootNode extends ASTNode {
	
	// children contains all classes in the file
	protected ArrayList<ClassNode> children;
	protected HashMap<String,Clazz> symbols;
	
	public RootNode(String type, ArrayList<ClassNode> nodes){
		super(type);
		//System.out.println("New root node");
		children = nodes;
		whatKindOfNode = "root";
	}
	
	public void addChild(ClassNode newChild){
		children.add(newChild);
	}
	
	public ArrayList<ClassNode> getClasses(){
		return children;
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t,
			TableManager manager, HashMap<String,Clazz> table){
		
		System.out.println("Typechecking root node");
		symbols = table;
		setType(classes);
		
		// typecheck all classes
		for (int i = 0; i < children.size(); ++i){
			
            symbols.clear();
			manager.clearBacklog();
			Clazz childClass = children.get(i).whatsMyType(classes, t, manager,symbols);
			if (children.get(i).isError()){
				System.out.println("\n ERROR: Type error in child of root \n");
				typeError = true;
				t.log_error(typeError);

			}
		}
				
		System.out.println("Root node: Type = " + myClass.nameOfClass);
		return myClass;
	}
}

//Implementation for class node
class ClassNode extends ASTNode {
	
	// fields contains both methods and variables
	protected ArrayList<ASTNode> fields;
	protected String nameOfParent;
	
	public ClassNode(String type, String name, 
			ArrayList<ASTNode> fs, String p){
		super(type);
		
		//System.out.println("New class node");

		fields = fs;
		nameOfNodeType = name;
		nameOfParent = p;
		whatKindOfNode = "class";

	}
	
	public void addField(ASTNode newField){
		fields.add(newField);
	}
	
	public String getParentClass(){
		return nameOfParent;
	}
	
	public ArrayList<ASTNode> getFields(){
		return fields;
	}

	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking class node");

		setType(classes);
		
		manager.newScope();
		
		Clazz cls = classes.get(nameOfNodeType);
		
		// Add variables defined for the scope of the class to the symbol table
		TreeMap<String,Clazz> fs = cls.getFields();
		Set<String> set = fs.keySet();
		for (String s : set){
			if (!table.containsKey(s)){
				manager.addSymbol(table, s, fs.get(s));
			}
		}
		
		// Add in fields from superclasses
		while (cls.getParent() != null){
			Clazz par = cls.getParent();
			TreeMap<String,Clazz> fs2 = par.getFields();
			Set<String> set2 = fs2.keySet();
			for (String s : set2){
				if (!table.containsKey(s)){
					manager.addSymbol(table, s, fs2.get(s));
				}
			}
			cls = par;
		}
		
		// Typecheck variables (what are conventionally called 'fields') first
		for (int i = 0; i < fields.size(); ++i){
			
			if (fields.get(i).whatKindOfNode != "method"){
				continue;
			}
			
			fields.get(i).setParent(this);
				
			Clazz fieldClass = fields.get(i).whatsMyType(classes, t, manager, table);
				
			if (fields.get(i).isError()){
				System.out.println("\n ERROR: Type error in method of class " + nameOfNodeType + "\n");
				typeError = true;
				}
		}
		
		// Typecheck methods
		for (int i = (fields.size()-1); i >= 0; --i){
			
			if (fields.get(i).whatKindOfNode == "method"){
				continue;
			}
			
			fields.get(i).setParent(this);
				
			Clazz fieldClass = fields.get(i).whatsMyType(classes, t, manager, table);
				
			if (fields.get(i).isError()){
				System.out.println("\n ERROR: Type error in field of class " + nameOfNodeType + "\n");
				typeError = true;
				}
		}
		
		manager.restorePrevious(table);
		t.log_error(typeError);

		System.out.println("Class node: Type = " + myClass.nameOfClass);
		return myClass;
	}
}

//Implementation for method node

class MethodNode extends ASTNode {
	
	// Here, myClass doubles as the return type for the method
	
	protected TreeMap<String,String> arguments;
	protected ArrayList<ASTNode> body;
	protected String methodName;

	
	@Override
	public String getMethodName(){
		return methodName;
	}
	
	public MethodNode(String type, ArrayList<ASTNode> statements, 
			TreeMap<String, String> args, String name){
		super(type);
		
		//System.out.println("New method node");

		arguments = args;
		body = statements;
		methodName = name;
		whatKindOfNode = "method";

	}
	
	// Make sure we have the arguments for our method
	@Override
	public ArrayList<String> getArgs(){	
		ArrayList<String> args = new ArrayList<String>();
		
		Set<String> set = arguments.keySet();
		for (String s : set){
			String cls = arguments.get(s);
			args.add(cls);
		}
		
		return args;
	}
		
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking method node");
		manager.newScope();
		Set<String> set = arguments.keySet();
		for (String s : set){
			String cName = arguments.get(s);
			Clazz cls = classes.get(cName);
			manager.addSymbol(table,s,cls);
		}
		
		setType(classes);
		
		// Make sure that arguments are typed correctly
		for (int i = 0; i < body.size(); ++i){
		
			ASTNode node = body.get(i);
			node.setParent(this);
			Clazz nodeClass = node.whatsMyType(classes, t, manager, table);
			
			// Address return type for method
			if (i == (body.size()-1)) {
				
					// Handle case that return type is SELF_TYPE
					if (myClass.nameOfClass == "SELF_TYPE"){
						if (!nodeClass.equals(this.getParent().myClass)){
							System.out.println("\n ERROR: Type mismatch between stated and " +
									"actual return values of method " + methodName + "\n");
							System.out.println("Stated type: " + this.getParent().myClass.nameOfClass);
							System.out.println("Actual type: " + nodeClass.nameOfClass + "\n");
							typeError = true;
							t.log_error(typeError);
						}
					}
					else {
						if (!myClass.equals(nodeClass)){
							System.out.println("\n ERROR: Type mismatch between stated and " +
									"actual return values of method " + methodName + "\n");
							System.out.println("Stated type: " + myClass.nameOfClass);
							System.out.println("Actual type: " + nodeClass.nameOfClass + "\n");
							typeError = true;
							t.log_error(typeError);
						}
					}
			}
		}
				
		manager.restorePrevious(table);
		
		System.out.println("Method node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
	
}

//Implementation for dispatch node
class DispatchNode extends ASTNode {
	
	Clazz callingClass;
	protected ArrayList<ASTNode> args;

	String nameOfMethod;
	protected ASTNode callingExpression;
	
	// We need two constructors, based on the syntax for the dispatch statement
	public DispatchNode(String type, ArrayList<ASTNode> arguments, String name){
		super(type);
		
		//System.out.println("New dispatch node");

		args = arguments;
		nameOfMethod = name;
		whatKindOfNode = "dispatch";
		callingExpression = null;
	}
	
	public DispatchNode(String type, ASTNode expr, ArrayList<ASTNode> arguments, String name){
		super(type);
		
		//System.out.println("New dispatch node");

		args = arguments;
		nameOfMethod = name;
		whatKindOfNode = "dispatch";
		callingExpression = expr;
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking dispatch node");
		//System.out.println(nameOfMethod);

		// Check to see if this is a local dispatch/static in Java terms
		if (nameOfNodeType.equals("local")){
			ASTNode findClassNode = this.getParent();
		/*	if (findClassNode == null){
				System.out.println("Null parent");
			} */
			while(findClassNode.getNodeType() != "class"){
				findClassNode = findClassNode.getParent();
			}
			
			ClassNode cls = (ClassNode) findClassNode;
			nameOfNodeType = cls.getTypeName();
		}
		
		else if ((callingExpression != null)){
			callingExpression.setParent(this.getParent());
			callingClass = callingExpression.whatsMyType(classes,t,manager,table);
		}
		
		setType(classes);
		if (callingClass == null){
			callingClass = classes.get(nameOfNodeType);
		}
		
		Clazz currentClass = callingClass;
		//System.out.println("Calling class is " + callingClass.getClassName());
		
		// Check to see if method exists for class or one of its ancestors
		TreeMap<String,ArrayList<Clazz>> ms = currentClass.getMethods();
		while (!ms.containsKey(nameOfMethod)){
			if (currentClass.getParent() == null){
				typeError = true;
				System.out.println("\n ERROR: Can't find method " + nameOfMethod
						+ " in class " + callingClass.getClassName() + "\n");
				
				t.log_error(typeError);
				
				return myClass;
			}
			
			else{
				currentClass = currentClass.getParent();
				ms = currentClass.getMethods();
			}
		}
		
		ArrayList<Clazz> declaredTypes = ms.get(nameOfMethod);

		// Typecheck arguments to method, including return type
		if (declaredTypes.get(0).getClassName().equals("SELF_TYPE")){
			myClass = callingClass;
		}
		
		else{
			myClass = declaredTypes.get(0);
		}
		
		for (int i = 1; i < declaredTypes.size(); ++i){
			args.get(i-1).setParent(this.getParent());
			Clazz argType = args.get(i-1).whatsMyType(classes, t, manager, table);
			if (!declaredTypes.get(i).equals(argType)){
				typeError = true;
				t.log_error(typeError);

				System.out.println("\n ERROR: Declared type of argument " 
						+ declaredTypes.get(i).getClassName() +
						" does not match actual type " + argType.nameOfClass
						+ " in method " + nameOfMethod + "\n");
			}
		
		}
		
		System.out.println("Dispatch node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
}

//Implementation for static dispatch node
class StaticDispatchNode extends ASTNode {
	
	Clazz callingClass;
	Clazz ancestorClass;
	String ancestorType;
	protected ArrayList<ASTNode> args;
	protected ASTNode callingExpression;

	String nameOfMethod;
	
	public StaticDispatchNode(String type, ASTNode expr, ArrayList<ASTNode> arguments, 
			String name, String aType){
		super(type);
		
		//System.out.println("New static dispatch node");

		args = arguments;
		nameOfMethod = name;
		ancestorType = aType;
		whatKindOfNode = "static dispatch";
		callingExpression = expr;

	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){		
		
		System.out.println("Typechecking static dispatch node");

		setType(classes);
		callingExpression.setParent(this.getParent());

		callingClass = callingExpression.whatsMyType(classes,t,manager,table);
		ancestorClass = classes.get(ancestorType);
		
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
			t.log_error(typeError);

			System.out.println("\n ERROR: Class " + callingClass.getClassName() + 
					" not a subclass of " + ancestorClass.getClassName() + "\n");
		}
				
		currentClass = ancestorClass;
		
		// Make sure method exists for class or one of its ancestors
		TreeMap<String,ArrayList<Clazz>> ms = currentClass.getMethods();
		while (!ms.containsKey(nameOfMethod)){
			if (currentClass.getParent() == null){
				typeError = true;
				System.out.println("\n ERROR: Can't find method " + nameOfMethod
						+ " in class " + callingClass.getClassName() + "\n");
				
				t.log_error(typeError);

				return myClass;
			}
			
			else{
				currentClass = currentClass.getParent();
				ms = currentClass.getMethods();
			}
		}
		
		// Handle return type of method
		ArrayList<Clazz> declaredTypes = ms.get(nameOfMethod);
		
		if (declaredTypes.get(0).getClassName().equals("SELF_TYPE")){
			myClass = callingClass;
		}
		
		else{
			myClass = declaredTypes.get(0);
		}		
		
		// Typecheck arguments to method
		for (int i = 1; i < declaredTypes.size(); ++i){
			args.get(i-1).setParent(this.getParent());
			Clazz argType = args.get(i-1).whatsMyType(classes, t, manager, table);
			if (!declaredTypes.get(i).equals(argType)){
				typeError = true;
				t.log_error(typeError);

				System.out.println("\n ERROR: Declared type of argument " 
						+ declaredTypes.get(i).getClassName() +
						" does not match actual type " + argType.nameOfClass
						+ " in method " + nameOfMethod + "\n");
			}
		}
		
		System.out.println("Static dispatch node: Type = " + myClass.nameOfClass);
		return myClass;
	}	
}

//Implementation of conditional node
class CondNode extends ASTNode {
	
	protected ASTNode ifNode;
	protected ASTNode thenNode;
	protected ASTNode elseNode;
	
	public CondNode(String type, ASTNode ifN, ASTNode elseN,
			ASTNode thenN) {
		super(type);
		
		//System.out.println("New cond node");

		ifNode = ifN;
		thenNode = thenN;
		elseNode = elseN;
		whatKindOfNode = "cond";
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking conditional node");

		ifNode.setParent(this);
		thenNode.setParent(this);
		elseNode.setParent(this);

		setType(classes);

		// Check if node to make sure it's a boolean expression
		Clazz ifClass = ifNode.whatsMyType(classes, t, manager, table);
		if (!ifClass.equals(classes.get("Bool"))){
			typeError = true;
			System.out.println("\n ERROR: Conditional statement not of type Bool \n");
		}
		
		//Find lowest-order superclass shared by then and else nodes
		
		Clazz thenClass = thenNode.whatsMyType(classes, t, manager, table);
		Clazz elseClass = elseNode.whatsMyType(classes, t, manager, table);

		
		Clazz commonClass = thenClass.getCommonParent(elseClass);
		
		myClass = commonClass;
		
		t.log_error(typeError);

		System.out.println("Cond node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
}

//Implementation for sequence node
class SequenceNode extends ASTNode {
	
	protected ArrayList<ASTNode> elements;
	
	public SequenceNode(String type, ArrayList<ASTNode> nodes){
		super(type);
		
		//System.out.println("New sequence node");

		elements = nodes;
		
		whatKindOfNode = "sequence";

	}
	
	public void addElement(ASTNode newElement){
		elements.add(newElement);
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking sequence node");

		setType(classes);
		
		// Typecheck each node in the sequence
		Clazz lastClass = classes.get("Object");
		for (int i = (elements.size()-1); i >= 0; --i){
			elements.get(i).setParent(this);
			Clazz elementClass = elements.get(i).whatsMyType(classes, t, manager, table);
			if (elements.get(i).isError()){
				typeError = true;
				System.out.println("\n ERROR: Type error in sequence of statements \n");
			}
			lastClass = elementClass;
		}
		
		myClass = lastClass;
		
		t.log_error(typeError);

		System.out.println("Sequence node: Type = " + myClass.nameOfClass);
		return myClass;
	}
}

//Implementation for case node
class CaseNode extends ASTNode {
	
	protected ArrayList<ASTNode> cases;
	
	public CaseNode(String type, ArrayList<ASTNode> nodes){
		super(type);
		
		//System.out.println("New case node");
		
		cases = nodes;
		whatKindOfNode = "case";

	}
	
	public void addCase(ASTNode newCase){
		cases.add(newCase);
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking case node");

		setType(classes);
		
		//Find lowest-order common superclass of all case nodes
		// and check types at the same time
		Clazz commonClass;
		if (cases.size() > 0){
			cases.get(0).setParent(this);
			commonClass = cases.get(0).whatsMyType(classes, t, manager, table);
			
			// The loop has to go inside the conditional or Java
			// complains about initialization
			for (int i = 0; i < cases.size(); ++i){
				cases.get(i).setParent(this);

				Clazz caseClass = cases.get(i).whatsMyType(classes, t, manager, table);
				commonClass = commonClass.getCommonParent(caseClass);
			}
			
			myClass = commonClass;
		}	
		
		t.log_error(typeError);
		
		System.out.println("Case node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
}

//Implementation for loop node
class LoopNode extends ASTNode {
	
	protected ASTNode whileNode;
	protected ArrayList<ASTNode> loopExpressions;
	
	public LoopNode(String type, ASTNode wNode, ArrayList<ASTNode> nodes){
		super(type);
		
		//System.out.println("New loop node");
		
		whileNode = wNode;
		loopExpressions = nodes;
		whatKindOfNode = "loop";

	}
	
	public void addExpression(ASTNode expr){
		loopExpressions.add(expr);
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking loop node");

		setType(classes);
		whileNode.setParent(this);
		
		// Check loop condition
		if (!whileNode.whatsMyType(classes, t, manager, table).equals(classes.get("Bool"))){
			typeError = true;
			t.log_error(typeError);

			System.out.println("\n ERROR: Loop condition of type other than bool \n");
		}

		// Check expressions in loop
		for (int i = 0; i < loopExpressions.size(); ++i){
			loopExpressions.get(i).setParent(this);
			Clazz loopClass = loopExpressions.get(i).whatsMyType(classes, t, manager, table);
		}
		
		System.out.println("Loop node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
}

//Implementation for binary operation node
class BinOpNode extends ASTNode {
	
	protected ASTNode leftChild;
	protected ASTNode rightChild;
		
	protected String op;
	public BinOpNode(String type, ASTNode lchild, ASTNode rchild, 
			String token) {
		super(type);
		
		//System.out.println("New binop node");
		
		leftChild = lchild;
		rightChild = rchild;
		op = token;
		whatKindOfNode = "binop";

	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking binop node");

		setType(classes);
		
		leftChild.setParent(this);
		rightChild.setParent(this);
		
		// Check the expressions on each side of the operator
		Clazz leftType = leftChild.whatsMyType(classes, t, manager, table);
		Clazz rightType = rightChild.whatsMyType(classes, t, manager, table);
		
		// Determine if operator is typed as an integer or a boolean
		if (myClass.equals(classes.get("Int"))){

			if ((!leftType.equals(classes.get("Int"))) ||
					(!rightType.equals(classes.get("Int")))){
				typeError = true;
				System.out.println("\n ERROR: Non-integer arguments to arithmetic operation \n");
			}
		}
		
		else if (myClass.equals(classes.get("Bool"))){
			if (op.equals("=")) {
				if (!leftType.equals(rightType)){
					System.out.println("\n ERROR: Argument types don't match for equality operator \n");
					typeError = true;
				}
			}
			else {
				if ((!leftType.equals(classes.get("Int"))) ||
						(!rightType.equals(classes.get("Int")))){
					typeError = true;
					System.out.println("\n ERROR: Non-integer arguments to less-than comparison \n");
				}
			}
		}
		
		t.log_error(typeError);
		
		System.out.println("Binop node: Type = " + myClass.nameOfClass);
		return myClass;
		
	}

}

//Implementation for unitary operator node
class UnOpNode extends ASTNode {
	
	protected ASTNode childNode;
	
	protected String op;
	
	public UnOpNode(String type, ASTNode child, 
			String token) {
		super(type);
		
		//System.out.println("New unop node");

		childNode = child;
		op = token;
		whatKindOfNode = "unop";

	}

	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){

		System.out.println("Typechecking unop node");

		setType(classes);
		
		childNode.setParent(this);
		
		// Check type of node being operated on
		Clazz childType = childNode.whatsMyType(classes, t, manager, table);
		
		// Determine if operator is typed as an integer or a boolean
		if (myClass.equals(classes.get("Int"))){

			if (!childType.equals(classes.get("Int"))){
				typeError = true;
				System.out.println("\n ERROR: Non-integer argument to integer negation operator \n");
			}
		}
		
		else if (myClass.equals(classes.get("Bool")) && (op.equals("isvoid"))){
				if (!childType.equals(classes.get("Bool"))){
					typeError = true;
					System.out.println("\n ERROR: Non-boolean argument to NOT statement \n");
				}			
		}
		
		t.log_error(typeError);
	
		System.out.println("UnOp node: Type = " + myClass.nameOfClass);
		return myClass;
	}

}

//Implementation for assignment node
class AssignNode extends ASTNode{
	
	protected ASTNode valueNode;
	protected String variable;
	Clazz superClass;
	
	@Override
	public String nameOfVar(){
		return variable;
	}

	public AssignNode(String type, ASTNode valNode,
			String name){
		super(type);
		
		//System.out.println("New assign node");

		valueNode = valNode;
		variable = name;
		whatKindOfNode = "assign";

	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){

		System.out.println("Typechecking assign node");

		// This is for when we define a variable at the same time we declare it
		if (!table.containsKey(variable)){
			manager.addSymbol(table, variable, myClass);
		}
		
		superClass = table.get(variable);
		
		valueNode.setParent(this);
		myClass = valueNode.whatsMyType(classes,t,manager,table);
		
		//Make sure valueNode's class is a subclass of the variable's class
						
		if (myClass.equals(superClass)){
			
			t.log_error(typeError);
			System.out.println("Assign node: Type = " + myClass.nameOfClass);
			return myClass;
		}
				
		Clazz currentClass = myClass;
		while((currentClass.getParent() != null)){

			if (currentClass.equals(superClass)){
				t.log_error(typeError);
				System.out.println("Assign node: Type = " + myClass.nameOfClass);
				return myClass;
			}
			currentClass = currentClass.getParent();
		}
		
		if (currentClass.equals(superClass)){
			t.log_error(typeError);
			System.out.println("Assign node: Type = " + myClass.nameOfClass);
			return myClass;
		}

		typeError = true;
		
		t.log_error(typeError);
		System.out.println("\n ERROR: " + myClass.nameOfClass + " not a subclass of " + superClass.nameOfClass
				+ "\n");

		System.out.println("Assign node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
}

//Implementation for new variable node
class NewTypeNode extends ASTNode{
	
	public NewTypeNode(String type){
		super(type);
		//System.out.println("New new type node");
		whatKindOfNode = "new type";
	}
	
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){	
		
		System.out.println("Typechecking new type node");

		setType(classes);
		
		t.log_error(typeError);
		
		System.out.println("New type node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
}

//Implementation for let node with initialization
class LetInitNode extends ASTNode{
	
	protected ASTNode valueNode;
	protected String variable;
	protected ASTNode inExpr;
	protected String varType;
	
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
		
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking let-init node");

		Clazz assignType = classes.get(varType);
		manager.newScope();
		// This is for when we define a variable at the same time we declare it
		manager.addSymbol(table, variable, assignType);
		
		setType(classes);
		
		valueNode.setParent(this);
		inExpr.setParent(this);
		
		// Get type of expression following 'in'
		
		myClass = inExpr.whatsMyType(classes, t, manager, table);
		
		//Make sure valueNode's class is a subclass of the variable's class
						
		if (valueNode.whatsMyType(classes, t, manager, table).equals(assignType)){
			
			t.log_error(typeError);
			
			manager.restorePrevious(table);
			System.out.println("Let-init node: Type = " + myClass.nameOfClass);

			return myClass;
		}
		
		// Typecheck value assigned with the let statement
		Clazz currentClass = valueNode.whatsMyType(classes, t, manager, table);
		Clazz valClass = currentClass;
		while(currentClass.getParent() != null){
			if (currentClass.equals(assignType)){
				
				t.log_error(typeError);
				
				manager.restorePrevious(table);
				System.out.println("Let-init node: Type = " + myClass.nameOfClass);

				return myClass;
			}
			currentClass = currentClass.getParent();
		}
		
		System.out.println("\n ERROR: Value " + valClass.nameOfClass + " not a subclass of " +
				"variable " + " variable class " + varType + "\n");
		typeError = true;
		
		t.log_error(typeError);
		
		manager.restorePrevious(table);

		System.out.println("Let-init node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
}

//Implementation for let node without initialization
class LetNoInitNode extends ASTNode{
	
	protected String variable;
	protected ASTNode inExpr;
	protected String varType;
	
	public LetNoInitNode(String type, String name, ASTNode e, String vType){
		super(type);
		
		//System.out.println("New let no init node");

		variable = name;
		inExpr = e;
		varType = vType;
		whatKindOfNode = "letnoinit";

	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t,
			TableManager manager, HashMap<String, Clazz> table){		

		System.out.println("Typechecking let-no-init node");

		Clazz assignType = classes.get(varType);
		manager.newScope();
		// This is for when we define a variable at the same time we declare it
		manager.addSymbol(table, variable, assignType);
		
		setType(classes);
		
		inExpr.setParent(this);
		
		// Typecheck "in" expression
		myClass = inExpr.whatsMyType(classes, t, manager, table);
		
		t.log_error(typeError);
		
		manager.restorePrevious(table);
		
		System.out.println("Let-no-init node: Type = " + myClass.nameOfClass);
		return myClass;
	}
	
}

// Implementation for isvoid node
class IsVoidNode extends ASTNode{
	protected ASTNode expr;
	public IsVoidNode(String type, ASTNode e){
		super(type);
		System.out.println("New isvoid node");

		expr = e;
		whatKindOfNode = "isvoid";
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){

		System.out.println("Typechecking isvoid node");

		setType(classes);
		
		expr.setParent(this);
		
		// Typecheck expression
		Clazz c = expr.whatsMyType(classes, t, manager, table);
		if ((c.equals(classes.get("Int"))) || (c.equals(classes.get("Bool"))) 
				|| (c.equals(classes.get("String")))){
			typeError = true;
			System.out.println("\n ERROR: Isvoid function cannot accept arguments of type int, bool, or string \n");
		}
		
		t.log_error(typeError);

		System.out.println("Isvoid node: Type = " + myClass.nameOfClass);
		return myClass;
	}
}

//Implementation for variable node
class VarNode extends ASTNode{
	protected String variable;
	
	public VarNode(String type, String name){
		super(type);
		//System.out.println("New variable node");

		variable = name;
		whatKindOfNode = "variable";

	}
	
	@Override
	public String nameOfVar(){
		return variable;
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		// Handle references to containing class
		if (variable.equals("self")){
			
			if (table.containsKey(variable)){
				manager.removeSymbol(table,variable);
			}
			ASTNode findClassNode = this.getParent();
			while(findClassNode.getNodeType() != "class"){
				findClassNode = findClassNode.getParent();
			}
			
			myClass = classes.get(findClassNode.getTypeName());
			
		}
		
		// Find variable in symbol table
		if (!table.containsKey(variable)){
			manager.addSymbol(table, variable, myClass);
		}
		
		else {
			myClass = table.get(variable);
		}
		
		t.log_error(typeError);

		System.out.println("Variable node: Type = " + myClass.nameOfClass);
		return myClass;
	}
}

//Implementation for integer literal node
class IntConstNode extends ASTNode{
	protected Integer value;
	public IntConstNode(String type, Integer val) {
		super(type);
		
		//System.out.println("New int const node");

		value = val;
		whatKindOfNode = "intconst";
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking int constant node");

		setType(classes);

		if (!myClass.equals(classes.get("Int"))){
			typeError = true;
		}

		t.log_error(typeError);

		System.out.println("Int constant node: Type = " + myClass.nameOfClass);
		return myClass;
	}
}

//Implementation for string literal node
class StringConstNode extends ASTNode{
	protected String text;
	public StringConstNode(String type, String str) {
		super(type);
		
		//System.out.println("New string const node");

		text = str;
		whatKindOfNode = "stringconst";
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking string constant node");

		setType(classes);

		if (!myClass.equals(classes.get("String"))){
			typeError = true;
		}

		t.log_error(typeError);

		System.out.println("String constant node: Type = " + myClass.nameOfClass);
		return myClass;
	}
}

//Implementation for boolean literal node
class BoolConstNode extends ASTNode{
	protected boolean val;
	public BoolConstNode(String type, boolean bool) {
		super(type);
		
		//System.out.println("New bool const node");

		val = bool;
		whatKindOfNode = "boolconst";
	}
	
	@Override
	public Clazz whatsMyType(HashMap<String,Clazz> classes, TypeError t, 
			TableManager manager, HashMap<String, Clazz> table){
		
		System.out.println("Typechecking bool constant node");

		setType(classes);

		if (!myClass.equals(classes.get("Bool"))){
			typeError = true;
		}
		
		t.log_error(typeError);

		System.out.println("Bool constant node: Type = " + myClass.nameOfClass);
		return myClass;
	}
}
