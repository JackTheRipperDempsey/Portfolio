/*
 * Pair is a simple class that we use for our symbol table in order 
 * to associate variables or arguments to methods with a type (a Clazz).
 */
public class Pair {
	String key;
	Clazz val;
	
	public Pair(String s, Clazz c){
		key = s;
		val = c;
	}
	
	public String getKey(){
		return key;
	}
	
	public Clazz getVal(){
		return val;
	}
}
