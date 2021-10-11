import java.util.ArrayList;
import java.util.HashMap;

// Implementation for root node
class RootNode extends ASTNode {
	
	// children contains all classes in the file
	private ArrayList<ClassNode> children;
    
    // This will be used as a local symbol table for class-level scopes
	private HashMap<String,Clazz> symbols;
    
    // This is needed to maintain a record of the ClassNodes composing the program
    // It comes into play when dealing with inheritance and you need a Clazz A that inherits Clazz B to have access to Clazz B's
    // initialized attributes and nested objects lists
    protected HashMap<String, ClassNode> classMap;
	
	public RootNode(String type, ArrayList<ClassNode> nodes){
		super(type);
		//System.out.println("New root node");
		children = nodes;
		whatKindOfNode = "root";
        classMap = new HashMap<String, ClassNode>();
	}
	
	public void addChild(ClassNode newChild){
		children.add(newChild);
	}
	
	public ArrayList<ClassNode> getClasses(){
		return children;
	}
	
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String,Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking root node");
		symbols = table;
		setType();
		
		// Typecheck all classes
		for (int i = 0; i < children.size(); ++i){
            ClassNode currentChild = children.get(i);
            currentChild.setParent(this);
            
            // Reset symbol table for each class-level scope
            symbols.clear();
			manager.clearBacklog();
            
            // Typecheck each child class node and log any errors
			Clazz childClass = currentChild.whatsMyType(t, manager,symbols, report);
			if (currentChild.isError()){
				report.err("\n ERROR: Type error in child of root \n");
				typeError = true;

			}
            
            classMap.put(childClass.getClassName(), currentChild);
		}
				
        t.log_error(typeError);
		System.out.println("Root node: Type = " + nameOfNodeType);
		return myClass;
	}
	
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){
        // Generate code for each child class node
		for (ClassNode child: children){
			child.generateC(text, null, new ArrayList<String>(), scopeVariables, new HashMap<String, ArrayList<String>>(), scopeDepth+1);
		}
	}
}