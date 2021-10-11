/*
 * TypeError is a simple class used for tracking the number of type 
 * errors encountered and aborting typechecking if that number exceeds 
 * a designated maximum allowable number.
 */

public class TypeError {
	
	int max_errors;
	int count;
	
	public TypeError(int max){
		max_errors = max;
		count = 0;
	}
	
	public void log_error(boolean yes){
		if (yes){
			++count;
			if (count >= max_errors){
				System.out.println("Too many type errors - aborting compilation");
				System.exit(1);
			}
		}
	}
	
	public int howManyErrors(){
		return count;
	}
	
}
