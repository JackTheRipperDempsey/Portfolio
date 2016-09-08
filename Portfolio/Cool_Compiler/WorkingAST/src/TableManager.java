/*
 * The TableManager class functions as our symbol table. This is used 
 * in typechecking to maintain a record of all variables within the 
 * current scope and their respectives types (Clazz's).
 * 
 * In addition to the expected methods for adding and removing 
 * variables from our table, we use the methods newScope and 
 * restorePrevious to manage which scope we're in (e.g., when we 
 * enter or return from a let statement or a new method call).
 */

import java.util.Stack;
import java.util.HashMap;

public class TableManager {

	private Stack<Pair> backlog;
	
	public TableManager(){
		backlog = new Stack<Pair>();
	}
	
	public void newScope(){
		Pair dummy = new Pair("marker",null);
		backlog.push(dummy);
	}
	
	public void addSymbol(HashMap<String,Clazz> symbols, 
			String name, Clazz type){
		if (symbols.containsKey(name)){
			Pair save = new Pair(name,symbols.get(name));
			backlog.push(save);
			symbols.remove(name);
		}
		
		symbols.put(name,type);
	}
	
	public void removeSymbol(HashMap<String,Clazz> symbols, String name){
		symbols.remove(name);
	}
	
	public void restorePrevious(HashMap<String,Clazz> symbols){
		while((!backlog.isEmpty()) && 
				(!backlog.peek().getKey().equals("marker"))){
			
			Pair restore = backlog.pop();
			String name = restore.getKey();
			Clazz type = restore.getVal();
			symbols.remove(name);
			symbols.put(name,type);
		}
		
		if ((!backlog.isEmpty()) && 
				(backlog.peek().getKey().equals("marker"))){
			backlog.pop();
		}
	}
	
	public void clearBacklog(){
		backlog.clear();
	}
	
}
