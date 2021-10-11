import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

// Implementation for case node
class CaseNode extends ASTNode {
	
    // Each statement in the switch/case table is a child node
	private ArrayList<ASTNode> cases;
	private ASTNode caseNode;
	
	public CaseNode(String type, ASTNode expr, ArrayList<ASTNode> nodes){
		super(type);
		
		//System.out.println("New case node");
		
		cases = nodes;
		caseNode = expr;
		whatKindOfNode = "case";
	}
    
    // Add a new statement to the switch/case table
	public void addCase(ASTNode newCase){
		cases.add(newCase);
	}
	
    // The nearest common ancestor/lowest-order superclass shared by the all branches of the case/switch table defines the type of the case node itself
	@Override
	public Clazz whatsMyType(TypeError t, TableManager manager, HashMap<String, Clazz> table, ErrorReport report){
		
		System.out.println("Typechecking case node");

		setType();
		
		caseNode.setParent(this);
        
        // Check the type of the governing expression for the switch/case table
		Clazz caseClass = caseNode.whatsMyType(t, manager, table, report);
		
		// Find lowest-order common superclass of all case nodes and check types at the same time
		Clazz commonClass = null;
		if (cases.size() > 0){
            
			// This loop has to go inside the conditional or Java complains about initialization
            // Typechecks each statement within the switch/case table, and keeps track of the lowest-order common superclass/ancestor
			for (int i = 0; i < cases.size(); ++i){
                LetNoInitNode nextCase = (LetNoInitNode) cases.get(i);

				nextCase.setParent(this);
				nextCase.setCaseAssignedValue(caseNode);

				Clazz nextClass = nextCase.whatsMyType(t, manager, table, report);
                commonClass = (commonClass == null) ? nextClass : commonClass.getCommonParent(nextClass);
			}
			
			myClass = commonClass;
		}	
		
        nameOfNodeType = myClass.getClassName();
		t.log_error(typeError);
		System.out.println("Case node: Type = " + nameOfNodeType);
		return myClass;
	}
	
    // Generate C code for the switch/case table
	@Override
	public void generateC(StringBuilder text, String governingClassName, ArrayList<String> classMemberVariables, ArrayList<ArrayList<String>> scopeVariables, HashMap<String, ArrayList<String>> resolvedCollisions, int scopeDepth){

		// Add any necessary tabs
		StringBuilder tabs = super.addTabs(scopeDepth);		

		Collections.reverse(cases);
      
        text.append(tabs);
        String tempVariableBody = getNextTempVariable(whatKindOfNode);
        cValueOrReference = tempVariableBody;
		text.append(ASTNode.generateMemoryAllocationOnlyForObject(tabs, tempVariableBody, nameOfNodeType));

        String tempVariableCase = getNextTempVariable(caseNode.getWhatKindOfNode());
		text.append(ASTNode.generateMemoryAllocationOnlyForObject(tabs, tempVariableCase, caseNode.getTypeName()));

		caseNode.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth);

		text.append(ASTNode.generateTabbedAssignmentLine(tabs, tempVariableCase, caseNode.getTypeName(), true, caseNode.getCValueForNode(), caseNode.getTypeName(), true, true));	

		// Find the case statement of lowest-order matching type
		String tempVariableCaseBranchTypes = getNextTempVariable("case_branch_types");
		text.append("\n\n" + tabs + "_TYPE " + tempVariableCaseBranchTypes + "[" + cases.size() + "] = {");

		for (int i = 0; i < cases.size(); ++i){
			LetNoInitNode nextCase = (LetNoInitNode) cases.get(i);
			text.append("_" + nextCase.getVarType().toUpperCase());
			if (i < (cases.size()-1)){
				text.append(", ");
			}
		}

		text.append("};");

		String tempVariableCaseSentinel = getNextTempVariable("case_sentinel");
		text.append("\n" + tabs + "_TYPE " + tempVariableCaseSentinel + " = _OBJECT;");

		String tempVariableCaseMatch = getNextTempVariable("case_match");
		text.append("\n" + tabs + "bool " + tempVariableCaseMatch + " = false;");

		String tempVariableCasesNumber = getNextTempVariable("case_number");
		text.append("\n" + tabs + "int " + tempVariableCasesNumber + " = (sizeof (" + tempVariableCaseBranchTypes + "))/(sizeof (_TYPE));");

		text.append("\n" + tabs + "for (int _i = 0; _i < " + tempVariableCase + "->numTypes; ++_i){");
		text.append("\n\t" + tabs + "for (int _j = 0; _j < " + tempVariableCasesNumber + "; ++_j){");
		text.append("\n\t\t" + tabs + "if (" + tempVariableCase + "->type[_i] == " + tempVariableCaseBranchTypes + "[_j]){");
		text.append("\n\t\t\t" + tabs + tempVariableCaseSentinel + " = " + tempVariableCase + "->type[_i];");
		text.append("\n\t\t\t" + tabs + tempVariableCaseMatch + " = true;");

		text.append("\n\t\t\t" + tabs + "break;");
		text.append("\n\t\t" + tabs + "}");
		text.append("\n\t" + tabs + "}");
		text.append("\n\t" + tabs + "if (" + tempVariableCaseMatch + "){");
		text.append("\n\t\t" + tabs + "break;");
		text.append("\n\t" + tabs + "}");
		text.append("\n" + tabs + "}");
		
        // Generate code for the governing case statement
		text.append("\n" + tabs + "switch (");
		text.append(tempVariableCaseSentinel);
		text.append("){\n");

		// Generate code for each case branch
		for (int i = 0; i < cases.size(); ++i){

			// scopeVariables.add(new ArrayList<String>());

			LetNoInitNode nextCase = (LetNoInitNode) cases.get(i);

			text.append("\n\n" + tabs + "\tcase ");
			text.append("_" + nextCase.getVarType().toUpperCase() + ":");
			text.append("\n");
			text.append(tabs + "\t{\n");

			nextCase.generateC(text, governingClassName, classMemberVariables, scopeVariables, resolvedCollisions, scopeDepth+2);
			
			text.append(ASTNode.generateTabbedAssignmentLine(new StringBuilder(tabs.toString() + "\t\t"), tempVariableBody, nameOfNodeType, true, nextCase.getCValueForNode(), nextCase.getTypeName(), true, true));

			text.append("\n" + tabs + "\t\tbreak;\n");
			text.append(tabs + "\t}\n");
		}
		
		// Close brackets
		text.append("\n");
		text.append(tabs);
		text.append("}\n");

        // scopeVariables.remove(scopeVariables.size()-1);
	} 
	
}