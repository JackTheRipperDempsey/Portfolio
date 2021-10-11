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

    // The backup table for handling collisions between names of variables defined in overlapping scopes - contains pairs of variable names and types; scopes are defined by sentinel variables
	private Stack<Pair> backlog;
	
	public TableManager(){
		backlog = new Stack<Pair>();
	}
	
    // Adds a sentinel variable when a new nested scope is entered
	public void newScope(){
		Pair sentinel = new Pair("marker",null);
		backlog.push(sentinel);
	}
	
    // Add variable to the symbol table passed in as an argument; if there is a collision, push the existing entry to the backlog (i.e., the most immediate/innermost scope's use of the name takes precedence)
	public void addSymbol(HashMap<String,Clazz> symbols, 
			String name, Clazz type){
		if (symbols.containsKey(name)){
			Pair save = new Pair(name,symbols.get(name));
			backlog.push(save);
			symbols.remove(name);
		}
		
		symbols.put(name,type);
	}
	
    // Remove an entry from the symbol table passed in as an argument
	public void removeSymbol(HashMap<String,Clazz> symbols, String name){
		symbols.remove(name);
	}
	
    // Add entries to the symbol table passed in as an argument from the backlog until you hit a sentinel variable; used when returning to an outer scope from the innermost one
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
	
    // Clears the backlog
	public void clearBacklog(){
		backlog.clear();
	}
	
}
