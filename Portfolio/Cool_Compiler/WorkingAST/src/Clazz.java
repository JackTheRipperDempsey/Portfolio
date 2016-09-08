/*
 * The Clazz class is how we represent objects in Cool.  Each Clazz
 * has a list of methods and a list of fields, as well as a reference
 * to its parent class for inheritance purposes. By default, all Clazz's
 * parent is set to the Object Clazz.
 * 
 * In this file, we have included the overall Clazz class, as well 
 * as several subclasses represent the builtin object types in Cool.
 * These are: Int, String, Bool, SELF_TYPE, IO, Object. We have also
 * included a Clazz for user-defined classes.
 * 
 * While most of the methods here should be relatively intuitive 
 * getters and setters, the getCommonParent method is slightly more 
 * complicated.  It takes two Clazz's as input, and returns their 
 * common parent.  This is used in type-checking if we need to find 
 * the common parent of the Clazz's associated with two or more nodes 
 * in our abstract syntax tree.
 */

import java.util.ArrayList;
import java.util.TreeMap;

public class Clazz {
	
	protected String nameOfClass;
	protected Clazz parent;
	protected TreeMap<String,ArrayList<Clazz>> methods;
	protected TreeMap<String,Clazz> fields;
	
	public Clazz(String name, Clazz p){
		nameOfClass = name;
		parent = p;
		methods = new TreeMap<String,ArrayList<Clazz>>();
		fields = new TreeMap<String,Clazz>();
	}
	
	public String getClassName(){
		return nameOfClass;
	}
	
	public Clazz getParent(){
		return parent;
	}
	
	public void setParent(Clazz p){
		parent = p;
	}
	
	public TreeMap<String,ArrayList<Clazz>> getMethods(){
		return methods;
	}
	
	public TreeMap<String,Clazz> getFields(){
		return fields;
	}
	
	public void addMethod(String name, ArrayList<Clazz> args){
		ArrayList<Clazz> argus = new ArrayList<Clazz>();
		for (int i = 0; i < args.size(); ++i){
			argus.add(args.get(i));
		}
		methods.put(name, argus);
	}
	
	public void addField(String name, Clazz val){
		fields.put(name, val);
	}
	
	public Clazz getCommonParent(Clazz coClass){
		
		// Is it the same Clazz?
		if (coClass.equals(this)){
			return this;
		}
		
		// Is this Clazz an ancestor of coClass?
		Clazz currentNeighbor = coClass;
		while(currentNeighbor.getParent() != null){
			currentNeighbor = currentNeighbor.getParent();
			if (currentNeighbor.equals(this)){
				return this;
			}
		}
			
		// Is coClass an ancestor of this Clazz;
		Clazz currentAncestor = this;
		while(currentAncestor.getParent() != null){
			currentAncestor = currentAncestor.getParent();
			if(currentAncestor.equals(coClass)){
				return coClass;
			}
		}
		
		Clazz commonParent = coClass.getParent().getCommonParent(this.getParent());
		
		return commonParent;	
		
	}
}

class Int extends Clazz {
		
	public Int(String name, Clazz p){
		super(name, p);
		nameOfClass = "Int";
	}
	
}

class CoolString extends Clazz {
	
	public CoolString(String name, Clazz p){
		super(name, p);
		nameOfClass = "String";
	}
	
}

class Bool extends Clazz {
	
	public Bool(String name, Clazz p){
		super(name, p);
		nameOfClass = "Bool";
	}
	
}

class IOClazz extends Clazz {
	
	public IOClazz(String name, Clazz p){
		super(name, p);
		nameOfClass = "IO";
	}
	
}

class ObjectClazz extends Clazz {
	
	public ObjectClazz(String name, Clazz p){
		super(name, p);
		nameOfClass = "Object";
	}
	
}

class UserClass extends Clazz{
	
	public UserClass(String name, Clazz p){
		super(name, p);
	}
	
}

class SELF_TYPE extends Clazz{
	
	public SELF_TYPE(String name, Clazz p){
		super(name, p);
		nameOfClass = "SELF_TYPE";

	}
	
}
