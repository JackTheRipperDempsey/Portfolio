import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CodeGenerator {
	public void generateCode(RootNode rootNode, HashMap<String, Clazz> clazzes, HashMap<String, ArrayList<String>> methodSignatures, String outputFile){
        ArrayList<String> classNames = new ArrayList<String>(clazzes.keySet());

        StringBuilder cText = new StringBuilder("");
        
		cText.append("#include <stdlib.h>\n\n");
        cText.append("#include <stdio.h>\n\n");
		cText.append("#include <string.h>\n\n");
        cText.append("#include <stdbool.h>\n\n");        

        // Add known class names as enums
        cText.append("typedef enum { ");
        for (int i = 0; i < classNames.size(); ++i){
            String className = classNames.get(i);
            cText.append("_" + className.toUpperCase());
            cText.append(", ");
        }

        // Use this for new objects prior to type assignment
        cText.append("} _TYPE;\n\n");        

        // Use this for getting typenames
        cText.append("char** _typeNames;\n");
        cText.append("\n");     

        // Declare shell struct here so that it can be referenced as return pointer type for our function pointers
        cText.append("\ntypedef struct _Shell _Shell;\n");

        // Use this for storing array of function pointers for each class of struct
        cText.append("typedef _Shell* (*_functionPtr)(_Shell* this, _Shell** args);\n");
        cText.append("\n"); 

        // Use this for storing default copy method for each class of structs
        cText.append("typedef _Shell* (*_copyPtr)(_Shell* _src, _Shell* _dest);\n");
        cText.append("\n"); 

        // Define shell struct for housing all object instances
        cText.append("\nstruct _Shell\n");
        cText.append("{\n\tvoid* reference;\n");
        cText.append("\tbool init;\n");
        cText.append("\tint numTypes;\n");
        cText.append("\t_TYPE *type;\n");
        cText.append("\tint numMethods;\n");
        cText.append("\tchar** methodNames;\n");
        cText.append("\t_functionPtr* methodPointers;\n");
        cText.append("\t_copyPtr defaultCopyMethod;\n");
        cText.append("};\n\n"); 

        // Generate shell dispatch method for calling function pointers
        cText.append("_Shell* _dispatch(char* methodName, _Shell* this, _Shell** args){\n");
        cText.append("\tint index = -1;\n");
        cText.append("\tchar* nextMethodName;\n");

        cText.append("\tfor (int i = 0; i < (this->numMethods); ++i){\n");
        cText.append("\t\tfor (int j = 0; j < (this->numTypes); ++j){\n");
        cText.append("\t\t\t_TYPE nextTypeNum = this->type[j];\n");
        cText.append("\t\t\tchar* nextTypeName =_typeNames[nextTypeNum];\n"); 
		cText.append("\t\t\tchar* compareName = (char*) malloc(1+ strlen(nextTypeName) + strlen(methodName));\n");
		cText.append("\t\t\tstrcat(compareName,nextTypeName);\n");
        cText.append("\t\t\tstrcat(compareName,methodName);\n");        
        cText.append("\t\t\tnextMethodName = this->methodNames[i];\n");

        cText.append("\t\t\tif (strcmp(compareName,nextMethodName) == 0){\n");
        cText.append("\t\t\t\tindex = i;\n");
        cText.append("\t\t\t\tbreak;\n");
        cText.append("\t\t\t}\n");
        cText.append("\t\t}\n");
        cText.append("\t\tif (index != -1){\n");
        cText.append("\t\t\tbreak;\n");
        cText.append("\t\t}\n");
        cText.append("\t}\n");

        cText.append("\tif (index == -1){\n");
        cText.append("\t\tprintf(\"Attempted to invoke method %s, but it could not be found.\\n\", methodName);\n");
        cText.append("\t\texit(1);\n");
        cText.append("\t}\n");

        cText.append("\t_functionPtr correctMethod = this->methodPointers[index];\n");
        cText.append("\treturn correctMethod(this, args);\n");
        cText.append("}\n");           

        // Generate shell dispatch method for calling function pointers
        cText.append("_Shell* _staticDispatch(char* methodName, _Shell* this, _Shell** args){\n");
        cText.append("\tint index = -1;\n");
        cText.append("\tchar* nextMethodName;\n");
        cText.append("\tfor (int i = 0; i < (this->numMethods); ++i){\n");
        cText.append("\t\tnextMethodName = this->methodNames[i];\n");
        cText.append("\t\tif (strcmp(methodName,nextMethodName) == 0){\n");
        cText.append("\t\t\tindex = i;\n");
        cText.append("\t\t\tbreak;\n");
        cText.append("\t\t}\n");
        cText.append("\t}\n");

        cText.append("\tif (index == -1){\n");
        cText.append("\t\tprintf(\"Attempted to invoke method %s, but it could not be found.\\n\", methodName);\n");
        cText.append("\t\texit(1);\n");
        cText.append("\t}\n");

        cText.append("\t_functionPtr correctMethod = this->methodPointers[index];\n");
        cText.append("\treturn correctMethod(this, args);\n");
        cText.append("}\n");                           

        // Use this for accessing generated method names and method pointers
        for (int i = 0; i < classNames.size(); ++i){
            String className = classNames.get(i);
            cText.append("int _" + className.toUpperCase() + "_NumMethods = 0;\n");
            cText.append("char** _" + className.toUpperCase() + "_MethodNames;\n");
            cText.append("_functionPtr* _" + className.toUpperCase() + "_MethodPointers;\n");
            cText.append("_copyPtr _" + className.toUpperCase() + "_DefaultCopyMethod;\n");
        }

        cText.append("\n");        
        
        // Define initialization of new object instance
        cText.append("_Shell* _newShellReference(){\n");
        cText.append("\t_Shell* _temp_shell = malloc(sizeof(_Shell));\n");
        cText.append("\t_temp_shell->numTypes = 1;\n");
        cText.append("\t_temp_shell->type = malloc(sizeof(_TYPE));\n");
        cText.append("\t_temp_shell->type[0] =_OBJECT;\n");
        cText.append("\t_temp_shell->init=false;\n");
        cText.append("\t_temp_shell->numMethods = _OBJECT_NumMethods;\n");
        cText.append("\t_temp_shell->methodNames = _OBJECT_MethodNames;\n");
        cText.append("\t_temp_shell->methodPointers = _OBJECT_MethodPointers;\n");
        cText.append("\t_temp_shell->defaultCopyMethod = _OBJECT_DefaultCopyMethod;\n");
        cText.append("\treturn _temp_shell;\n");
        cText.append("}\n\n");

        // Define initialization of new object instance

        cText.append("typedef struct Object Object;\n");
        cText.append("struct Object\n");
        cText.append("{\n");
        cText.append("\tObject** _data;\n");
        cText.append("};\n\n");

        cText.append("_Shell* Object_Object(){\n");
        cText.append("\t_Shell* _temp_obj = _newShellReference();\n");
        cText.append("\t_temp_obj->numTypes = 1;\n");
        cText.append("\t_temp_obj->type = malloc(sizeof(_TYPE));\n");
        cText.append("\t_temp_obj->type[0] =_OBJECT;\n");
        cText.append("\t_temp_obj->reference = malloc(sizeof(Object));\n");
        cText.append("\t_temp_obj->init = true;\n");
        cText.append("\t_temp_obj->numMethods = _OBJECT_NumMethods;\n");
        cText.append("\t_temp_obj->methodNames = _OBJECT_MethodNames;\n");
        cText.append("\t_temp_obj->methodPointers = _OBJECT_MethodPointers;\n");
        cText.append("\t_temp_obj->defaultCopyMethod = _OBJECT_DefaultCopyMethod;\n");
        cText.append("\t((Object *) _temp_obj->reference)->_data = malloc(sizeof(Object*));\n");
        cText.append("\t(((Object *) _temp_obj->reference)->_data) = (Object**)&(_temp_obj->reference);\n");
        cText.append("\treturn _temp_obj;\n");
        cText.append("}\n\n");

        cText.append("void _Object_copy(_Shell* _src, _Shell* _dest){\n");
        cText.append("}\n\n");

        // Define structs and constructors for all other built-in types
        cText.append("typedef struct IO IO;\n");
        cText.append("struct IO\n");
        cText.append("{\n");
        cText.append("\tIO** _data;\n");
        cText.append("};\n\n");

        cText.append("_Shell* IO_IO(){\n");
        cText.append("\t_Shell* _temp_io = _newShellReference();\n");
        cText.append("\t_temp_io->numTypes = 2;\n");
        cText.append("\t_temp_io->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_io->type[0] =_IO;\n");
        cText.append("\t_temp_io->type[1] =_OBJECT;\n");
        cText.append("\t_temp_io->reference = malloc(sizeof(IO));\n");
        cText.append("\t_temp_io->init=true;\n");
        cText.append("\t_temp_io->numMethods = _IO_NumMethods;\n");
        cText.append("\t_temp_io->methodNames = _IO_MethodNames;\n");
        cText.append("\t_temp_io->methodPointers = _IO_MethodPointers;\n");
        cText.append("\t_temp_io->defaultCopyMethod = _IO_DefaultCopyMethod;\n");
        cText.append("\t((IO *) _temp_io->reference)->_data = malloc(sizeof(IO*));\n");
        cText.append("\t(((IO *) _temp_io->reference)->_data) = (IO**)&(_temp_io->reference);\n");
        cText.append("\treturn _temp_io;\n");
        cText.append("}\n\n");      

        cText.append("void _IO_copy(_Shell* _src, _Shell* _dest){\n");
        cText.append("}\n\n");  

        cText.append("typedef struct Int\n");
        cText.append("{\n");
        cText.append("\tint* _data;\n");
        cText.append("} Int;\n\n");

        cText.append("_Shell* Int_Int(){\n");
        cText.append("\t_Shell* _temp_int = _newShellReference();\n");
        cText.append("\t_temp_int->numTypes = 2;\n");
        cText.append("\t_temp_int->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_int->type[0] =_INT;\n");
        cText.append("\t_temp_int->type[1] =_OBJECT;\n");
        cText.append("\t_temp_int->reference = malloc(sizeof(Int));\n");
        cText.append("\t_temp_int->init=true;\n");
        cText.append("\t_temp_int->numMethods = _INT_NumMethods;\n");
        cText.append("\t_temp_int->methodNames = _INT_MethodNames;\n");
        cText.append("\t_temp_int->methodPointers = _INT_MethodPointers;\n");
        cText.append("\t_temp_int->defaultCopyMethod = _INT_DefaultCopyMethod;\n");
        cText.append("\t((Int *) _temp_int->reference)->_data = malloc(sizeof(int));\n");
        cText.append("\t*(((Int *) _temp_int->reference)->_data) = 0;\n");
        cText.append("\treturn _temp_int;\n");
        cText.append("}\n\n");

        cText.append("void _Int_copy(_Shell* _src, _Shell* _dest){\n");
        cText.append("}\n\n");  
        
        cText.append("typedef struct Bool\n");
        cText.append("{\n");
        cText.append("\tbool* _data;\n");
        cText.append("} Bool;\n\n");

        cText.append("_Shell* Bool_Bool(){\n");
        cText.append("\t_Shell* _temp_bool = _newShellReference();\n");
        cText.append("\t_temp_bool->numTypes = 2;\n");
        cText.append("\t_temp_bool->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_bool->type[0] =_BOOL;\n");
        cText.append("\t_temp_bool->type[1] =_OBJECT;\n");        
        cText.append("\t_temp_bool->reference = malloc(sizeof(Bool));\n");
        cText.append("\t_temp_bool->init=true;\n");
        cText.append("\t_temp_bool->numMethods = _BOOL_NumMethods;\n");
        cText.append("\t_temp_bool->methodNames = _BOOL_MethodNames;\n");
        cText.append("\t_temp_bool->methodPointers = _BOOL_MethodPointers;\n");
        cText.append("\t_temp_bool->defaultCopyMethod = _BOOL_DefaultCopyMethod;\n");
        cText.append("\t((Bool *) _temp_bool->reference)->_data = malloc(sizeof(bool));\n");
        cText.append("\t*(((Bool *) _temp_bool->reference)->_data) = false;\n");
        cText.append("\treturn _temp_bool;\n");
        cText.append("}\n\n");

        cText.append("void _Bool_copy(_Shell* _src, _Shell* _dest){\n");
        cText.append("}\n\n");  
        
        cText.append("typedef struct String\n");
        cText.append("{\n");
        cText.append("\tchar** _data;\n");
        cText.append("} String;\n\n");
        
        cText.append("_Shell* String_String(){\n");
        cText.append("\t_Shell* _temp_string = _newShellReference();\n");
        cText.append("\t_temp_string->numTypes = 2;\n");
        cText.append("\t_temp_string->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_string->type[0] =_STRING;\n");
        cText.append("\t_temp_string->type[1] =_OBJECT;\n"); 
        cText.append("\t_temp_string->reference = malloc(sizeof(String));\n");
        cText.append("\t_temp_string->init=true;\n");
        cText.append("\t_temp_string->numMethods = _STRING_NumMethods;\n");
        cText.append("\t_temp_string->methodNames = _STRING_MethodNames;\n");
        cText.append("\t_temp_string->methodPointers = _STRING_MethodPointers;\n");
        cText.append("\t_temp_string->defaultCopyMethod = _STRING_DefaultCopyMethod;\n");
        cText.append("\t((String *) _temp_string->reference)->_data = malloc(sizeof(char*));\n");
        cText.append("\t*(((String *) _temp_string->reference)->_data) = \"\";\n");
        cText.append("\treturn _temp_string;\n");
        cText.append("}\n\n");

        cText.append("void _String_copy(_Shell* _src, _Shell* _dest){\n");
        cText.append("}\n\n");  

        cText.append("void _SELF_TYPE_copy(_Shell* _src, _Shell* _dest){\n");
        cText.append("}\n\n");  
		
		cText.append("_Shell* Object_type_name(_Shell* this, _Shell** args){\n");
        cText.append("\t_TYPE _typeNum = this->type[0];\n");
        cText.append("\tchar* _typeName =_typeNames[_typeNum];\n"); 
        cText.append("\t_Shell* _temp_string = _newShellReference();\n");
        cText.append("\t_temp_string->numTypes = 2;\n");
        cText.append("\t_temp_string->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_string->type[0] =_STRING;\n");
        cText.append("\t_temp_string->type[1] =_OBJECT;\n"); 
        cText.append("\t_temp_string->reference = malloc(sizeof(String));\n");
        cText.append("\t_temp_string->init=true;\n");
        cText.append("\t_temp_string->numMethods = _STRING_NumMethods;\n");
        cText.append("\t_temp_string->methodNames = _STRING_MethodNames;\n");
        cText.append("\t_temp_string->methodPointers = _STRING_MethodPointers;\n");
        cText.append("\t_temp_string->defaultCopyMethod = _STRING_DefaultCopyMethod;\n");
        cText.append("\t((String *) _temp_string->reference)->_data = malloc(sizeof(char*));\n");
        cText.append("\t*(((String *) _temp_string->reference)->_data) = _typeName;\n");        
        cText.append("\treturn _temp_string;\n");
		cText.append("}\n\n");

		cText.append("_Shell* Object_abort(_Shell* this, _Shell** args){\n");
        cText.append("\texit(1);");
        cText.append("\treturn this;");
        cText.append("\n}\n\n");

        // TODO: Finish implementing this
		cText.append("_Shell* Object_copy(_Shell* this, _Shell** args){\n");

        cText.append("\t_Shell* _temp_copy = _newShellReference();\n");
        cText.append("\t_temp_copy->numTypes = this->numTypes;\n");
        cText.append("\t_temp_copy->type = malloc((_temp_copy->numTypes)*sizeof(_TYPE));\n");
        cText.append("\tfor (int _temp_index = 0; _temp_index < this->numTypes; ++_temp_index){\n");
        cText.append("\t\t_temp_copy->type[_temp_index] = this->type[_temp_index];\n");
        cText.append("\t}\n");
        cText.append("\t_temp_copy->init = true;\n");

        cText.append("\t_temp_copy->numMethods = this->numMethods;\n");
        cText.append("\t_temp_copy->methodNames = this->methodNames;\n");
        cText.append("\t_temp_copy->methodPointers = this->methodPointers;\n");
        cText.append("\t_temp_copy->defaultCopyMethod = this->defaultCopyMethod;\n");

        cText.append("\tif (_temp_copy->type[0] == _INT){\n");
        cText.append("\t\t_temp_copy->reference = malloc(sizeof(Int));\n");
        cText.append("\t\t((Int *) _temp_copy->reference)->_data = malloc(sizeof(int));\n");
        cText.append("\t\t*((Int *) _temp_copy->reference)->_data = *(((Int *) this->reference)->_data);\n");
        cText.append("\t}\n");

        cText.append("\telse if (_temp_copy->type[0] == _BOOL){\n");
        cText.append("\t\t_temp_copy->reference = malloc(sizeof(Bool));\n");
        cText.append("\t\t((Bool *) _temp_copy->reference)->_data = malloc(sizeof(bool));\n");
        cText.append("\t\t*((Bool *) _temp_copy->reference)->_data = *(((Bool *) this->reference)->_data);\n");
        cText.append("\t}\n");

        cText.append("\telse if (_temp_copy->type[0] == _STRING){\n");
        cText.append("\t\t_temp_copy->reference = malloc(sizeof(String));\n");
        cText.append("\t\tchar* sourceString = (char*) (*(((String *) this->reference)->_data));\n");
		cText.append("\t\tchar* dest = (char*) malloc(1+ strlen(sourceString));\n");
		cText.append("\t\tstrcat(dest,sourceString);\n");
        cText.append("\t\t((String *) _temp_copy->reference)->_data = malloc(sizeof(char*));\n");
        cText.append("\t\t*((String *) _temp_copy->reference)->_data = dest;\n");
        cText.append("\t}\n");

        cText.append("\telse if (_temp_copy->type[0] == _IO){\n");
        cText.append("\t\t_temp_copy->reference = malloc(sizeof(IO));\n");
        cText.append("\t\t((IO *) _temp_copy->reference)->_data = malloc(sizeof(IO*));\n");
        cText.append("\t\t(((IO *) _temp_copy->reference)->_data) = (IO**)&(_temp_copy->reference);\n");
        cText.append("\t}\n");

        cText.append("\telse if (_temp_copy->type[0] == _OBJECT){\n");
        cText.append("\t\t_temp_copy->reference = malloc(sizeof(Object));\n");
        cText.append("\t\t((Object *) _temp_copy->reference)->_data = malloc(sizeof(Object*));\n");
        cText.append("\t\t(((Object *) _temp_copy->reference)->_data) = (Object**)&(_temp_copy->reference);\n");
        cText.append("\t}\n");

        cText.append("\telse {\n");
        cText.append("\t\tthis->defaultCopyMethod(this,_temp_copy);\n");
        cText.append("\t}\n");

        cText.append("\treturn _temp_copy;");
        cText.append("\n}\n\n");

		cText.append("_Shell* IO_out_string(_Shell* this, _Shell** args){\n");
        cText.append("\t_Shell* string = args[0];\n");
		cText.append("\tprintf(\"%s\",*(((String *) (string->reference))->_data));\n");
        cText.append("\treturn this;\n");
        cText.append("}\n\n");
		
		cText.append("_Shell* IO_out_int(_Shell* this, _Shell** args){\n");
        cText.append("\t_Shell* integer = args[0];\n");
		cText.append("\tprintf(\"%d\",*(((Int *) (integer->reference))->_data));\n");
        cText.append("\treturn this;\n");
		cText.append("}\n\n");
        
		cText.append("_Shell* IO_in_string(_Shell* this, _Shell** args){\n");
		cText.append("\tchar* string = (char*) malloc( 1000 );\n");
		cText.append("\tscanf(\"%s\",string);\n");
        cText.append("\t_Shell* _temp_string = _newShellReference();\n");
        cText.append("\t_temp_string->numTypes = 2;\n");
        cText.append("\t_temp_string->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_string->type[0] =_STRING;\n");
        cText.append("\t_temp_string->type[1] =_OBJECT;\n"); 
        cText.append("\t_temp_string->reference = malloc(sizeof(String));\n");
        cText.append("\t_temp_string->init=true;\n");
        cText.append("\t_temp_string->numMethods = _STRING_NumMethods;\n");
        cText.append("\t_temp_string->methodNames = _STRING_MethodNames;\n");
        cText.append("\t_temp_string->methodPointers = _STRING_MethodPointers;\n");
        cText.append("\t_temp_string->defaultCopyMethod = _STRING_DefaultCopyMethod;\n");
        cText.append("\t((String *) _temp_string->reference)->_data = malloc(sizeof(char*));\n");
        cText.append("\t*(((String *) _temp_string->reference)->_data) = string;\n");
        cText.append("\treturn _temp_string;\n");
		cText.append("}\n\n");
		
		cText.append("_Shell* IO_in_int(_Shell* this, _Shell** args){\n");
		cText.append("\tint integer;\n");
		cText.append("\tscanf(\"%d\",&integer);\n");
        cText.append("\t_Shell* _temp_int = _newShellReference();\n");
        cText.append("\t_temp_int->numTypes = 2;\n");
        cText.append("\t_temp_int->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_int->type[0] =_INT;\n");
        cText.append("\t_temp_int->type[1] =_OBJECT;\n"); 
        cText.append("\t_temp_int->reference = malloc(sizeof(Int));\n");
        cText.append("\t_temp_int->init=true;\n");
        cText.append("\t_temp_int->numMethods = _INT_NumMethods;\n");
        cText.append("\t_temp_int->methodNames = _INT_MethodNames;\n");
        cText.append("\t_temp_int->methodPointers = _INT_MethodPointers;\n");
        cText.append("\t_temp_int->defaultCopyMethod = _INT_DefaultCopyMethod;\n");
        cText.append("\t((Int *) _temp_int->reference)->_data = malloc(sizeof(int));\n");
        cText.append("\t*(((Int *) _temp_int->reference)->_data) = integer;\n");
        cText.append("\treturn _temp_int;\n");    
		cText.append("}\n\n");
		
        cText.append("_Shell* String_length(_Shell* this, _Shell** args){\n");
        cText.append("\tchar* string = (char*) (*(((String *) this->reference)->_data));\n");
        cText.append("\tint integer = strlen(string);\n");
        cText.append("\t_Shell* _temp_int = _newShellReference();\n");
        cText.append("\t_temp_int->numTypes = 2;\n");
        cText.append("\t_temp_int->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_int->type[0] =_INT;\n");
        cText.append("\t_temp_int->type[1] =_OBJECT;\n"); 
        cText.append("\t_temp_int->reference = malloc(sizeof(Int));\n");
        cText.append("\t_temp_int->init=true;\n");
        cText.append("\t_temp_int->numMethods = _INT_NumMethods;\n");
        cText.append("\t_temp_int->methodNames = _INT_MethodNames;\n");
        cText.append("\t_temp_int->methodPointers = _INT_MethodPointers;\n");
        cText.append("\t_temp_int->defaultCopyMethod = _INT_DefaultCopyMethod;\n");
        cText.append("\t((Int *) _temp_int->reference)->_data = malloc(sizeof(int));\n");
        cText.append("\t*(((Int *) _temp_int->reference)->_data) = integer;\n");
		cText.append("\treturn _temp_int;\n");
		cText.append("}\n\n");	
        
        cText.append("_Shell* String_concat(_Shell* this, _Shell** args){\n");
        cText.append("\t_Shell* argObj = args[0];\n");
        cText.append("\tchar* callerString = (char*) (*(((String *) this->reference)->_data));\n");
        cText.append("\tchar* argString = (char*) (*(((String *) argObj->reference)->_data));\n");
		cText.append("\tchar* dest = (char*) malloc(1+ strlen(callerString) + strlen(argString));\n");
		cText.append("\tstrcat(dest,callerString);\n");
        cText.append("\tstrcat(dest,argString);\n");
        cText.append("\t_Shell* _temp_string = _newShellReference();\n");
        cText.append("\t_temp_string->numTypes = 2;\n");
        cText.append("\t_temp_string->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_string->type[0] =_STRING;\n");
        cText.append("\t_temp_string->type[1] =_OBJECT;\n"); 
        cText.append("\t_temp_string->reference = malloc(sizeof(String));\n");
        cText.append("\t_temp_string->init=true;\n");
        cText.append("\t_temp_string->numMethods = _STRING_NumMethods;\n");
        cText.append("\t_temp_string->methodNames = _STRING_MethodNames;\n");
        cText.append("\t_temp_string->methodPointers = _STRING_MethodPointers;\n");
        cText.append("\t_temp_string->defaultCopyMethod = _STRING_DefaultCopyMethod;\n");
        cText.append("\t((String *) _temp_string->reference)->_data = malloc(sizeof(char*));\n");
        cText.append("\t*(((String *) _temp_string->reference)->_data) = dest;\n");
        cText.append("\treturn _temp_string;\n");        
		cText.append("}\n\n");		

        cText.append("_Shell* String_substr(_Shell* this, _Shell** args){\n");
        cText.append("\t_Shell* startObj = args[0];\n");
        cText.append("\t_Shell* lengthObj = args[1];\n");
        cText.append("\tchar* callerString = (char*) (*(((String *) this->reference)->_data));\n");
        cText.append("\tint startIndex = *(((Int *) startObj->reference)->_data);\n");
        cText.append("\tint length = *(((Int *) lengthObj->reference)->_data);\n");
		cText.append("\tchar* buffer = (char*) malloc(length+1);\n");
		cText.append("\tmemcpy(buffer,&callerString[startIndex],length);\n");
        cText.append("\tbuffer[length]=\'\\0\';\n");
        cText.append("\t_Shell* _temp_string = _newShellReference();\n");
        cText.append("\t_temp_string->numTypes = 2;\n");
        cText.append("\t_temp_string->type = malloc(2*sizeof(_TYPE));\n");
        cText.append("\t_temp_string->type[0] =_STRING;\n");
        cText.append("\t_temp_string->type[1] =_OBJECT;\n"); 
        cText.append("\t_temp_string->reference = malloc(sizeof(String));\n");
        cText.append("\t_temp_string->init=true;\n");
        cText.append("\t_temp_string->numMethods = _STRING_NumMethods;\n");
        cText.append("\t_temp_string->methodNames = _STRING_MethodNames;\n");
        cText.append("\t_temp_string->methodPointers = _STRING_MethodPointers;\n");
        cText.append("\t_temp_string->defaultCopyMethod = _STRING_DefaultCopyMethod;\n");
        cText.append("\t((String *) _temp_string->reference)->_data = malloc(sizeof(char*));\n");
        cText.append("\t*(((String *) _temp_string->reference)->_data) = buffer;\n");
        cText.append("\treturn _temp_string;\n");     
        cText.append("}\n\n");

        HashMap<String,ArrayList<String>> classMethods = new HashMap<String,ArrayList<String>>();

        for (int i = 0; i < classNames.size(); ++i){
            String className = classNames.get(i);
            classMethods.put(className.toUpperCase(),new ArrayList<String>());
        }
        classMethods.get("OBJECT").add("Object_abort");
        classMethods.get("OBJECT").add("Object_type_name");
        classMethods.get("OBJECT").add("Object_copy");

        classMethods.get("IO").add("IO_out_string");
        classMethods.get("IO").add("IO_out_int");
        classMethods.get("IO").add("IO_in_string");
        classMethods.get("IO").add("IO_in_int");

        classMethods.get("STRING").add("String_length");
        classMethods.get("STRING").add("String_concat");
        classMethods.get("STRING").add("String_substr");  

        // Generate method signatures/declarations
        for (String compoundName : methodSignatures.keySet()){
            cText.append("_Shell* " + compoundName + "(_Shell* this");

            cText.append(", _Shell** args");
            cText.append(");\n\n");

            String[] splitNames = compoundName.split("_");
            String className = splitNames[0];
            classMethods.get(className.toUpperCase()).add(compoundName);
        }

        // Generate struct and constructor declarations
        for (ClassNode nextClass : rootNode.getClasses()){
            String className = nextClass.getTypeName();
            cText.append("typedef struct " + className + " " + className + ";\n");
            nextClass.generateStructDefinition(cText, 1, new ArrayList<String>(),new ArrayList<ArrayList<String>>(), new HashMap<String,ArrayList<String>>());
            cText.append("_Shell* " + className + "_" + className + "();\n\n" );
        }
		
		rootNode.generateC(cText, null, null, new ArrayList<ArrayList<String>>(), null,0);

        // Generate a driving main method
        cText.append("int main(int argc, char *argv[]) {\n");
        
        // Use this for getting typenames
        cText.append("\t_typeNames = (char**) malloc("+ classNames.size() + " * sizeof(char*));\n");
        for (int i = 0; i < classNames.size(); ++i){
            cText.append("\t_typeNames[" + i + "] = \"" + classNames.get(i) + "\";\n");            
        }
        cText.append("\n");            

        // Populate method names
        for (String className : classNames){
            String upperCaseName = className.toUpperCase();
            ArrayList<String> methodNames = classMethods.get(upperCaseName);
            Clazz currentClazz = clazzes.get(className);
            while (currentClazz.getParent() != null){
                currentClazz = currentClazz.getParent();
                //String currentClassName = currentClazz.getClassName().toUpperCase();
                //methodNames.addAll(classMethods.get(currentClassName));
                for (String inheritedMethod : currentClazz.getMethods().keySet()){
                    String compoundMethodName = currentClazz.getClassName() + "_" + inheritedMethod;
                    methodNames.add(currentClazz.getClassName() + "_" + inheritedMethod);
                }
            }
            int methodCount = methodNames.size();

            cText.append("\t_" + upperCaseName + "_NumMethods = " + methodCount + ";\n");
            cText.append("\t_" + upperCaseName + "_MethodNames = (char**) malloc(" + methodCount + " * sizeof(char*));\n");
            cText.append("\t_" + upperCaseName + "_MethodPointers = (_functionPtr*) malloc(" + methodCount + " * sizeof(_functionPtr));\n");
            for (int i = 0; i < methodCount; ++i){
                String compoundMethodName = methodNames.get(i);
                cText.append("\t" + "_" + upperCaseName + "_MethodNames[" + i + "] = \"" + compoundMethodName + "\";\n");
                cText.append("\t" + "_" + upperCaseName + "_MethodPointers[" + i + "] =  (_functionPtr)" + compoundMethodName + ";\n");
            }
            cText.append("\t_" + upperCaseName + "_DefaultCopyMethod = " + "(_copyPtr) _" + className + "_copy;\n");
        }

        cText.append("\n");

        cText.append("\t_Shell* program_Main = Main_Main();\n");
        cText.append("\t_Shell** main_Args;\n");
        cText.append("\tMain_main(program_Main, main_Args);\n");
        cText.append("}\n");
        
		try {
            if (outputFile.equals("")){
                outputFile = "c_output.txt";
            }
			FileWriter fileWriter = new FileWriter(outputFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(cText.toString());
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
            System.exit(1);
		}
		
	}
}
