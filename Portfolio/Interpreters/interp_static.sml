(*  Here's a skeleton file to help you get started on Interpreter 1.
 * Original version by Geoffrey Smith - http://users.cs.fiu.edu/~smithg/
 *)

use "parser.sml";


(* Here is a result datatype *)
datatype result =
    RES_ERROR of string
  | RES_NUM   of int
  | RES_BOOL  of bool
  | RES_SUCC
  | RES_PRED
  | RES_ISZERO
  | RES_FUN   of (string * term)
  | RES_CLOSURE of (string * term * env)
and env =
    Env       of (string * result) list;

(* Here is a basic environment implementation *)
exception not_found;

fun new_env() = Env(nil);
fun extend_env (Env(oldenv), id, value) = Env( (id, value):: oldenv);
fun extend_env_all (Env(oldenv), id_value_list) = Env(id_value_list @ oldenv);
fun lookup_env (Env(nil), id) = (print("Free Var!! "^id); raise not_found)
   |lookup_env (Env((id1,value1)::b), id) =  
        if (id1 = id) 
        then value1
	    else lookup_env(Env(b), id) ;

(*  Here's a partial skeleton of interp : (term * environment) -> result.
    I've done the first case for you
*)
fun interp (exp, env) = 

  case exp of
    AST_ERROR s                 => RES_ERROR s
  | AST_NUM  x                  => RES_NUM x
  | AST_BOOL b                  => RES_BOOL b
  | AST_SUCC                    => RES_SUCC
  | AST_PRED                    => RES_PRED
  | AST_ISZERO                  => RES_ISZERO
  | AST_IF (exp1, exp2, exp3)   => let val res = interp(exp1, env)
                                   in
                                    if not(res = RES_BOOL true) andalso not(res = RES_BOOL false)
                                    then RES_ERROR "Conditional not boolean"
                                    else let val RES_BOOL b = res
                                         in
                                            if b
                                            then interp(exp2,env)
                                            else interp(exp3,env)
                                         end
                                   end
  | AST_APP (exp1, exp2)        =>          let val res1 = interp(exp1,env)
                                                val res2 = interp(exp2,env)
                                            in
                                            case res1 of RES_CLOSURE(var,fexp,e2) =>
                                                let
                                                    val env2 = extend_env(e2,var,res2)
                                                in
                                                    interp(fexp, env2)
                                                end
                                            | _ =>
                                                if res1  = RES_ISZERO
                                                then case res2 of RES_NUM x => if x = 0 then RES_BOOL true else RES_BOOL false
                                                     | _ => RES_ERROR "Function iszero needs parameter of type int"
                                                else if res1 = RES_SUCC
                                                     then case res2 of RES_NUM x => RES_NUM (x+1)
                                                          | _ => RES_ERROR "Function succ needs parameter of type int"
                                                     else if res1 = RES_PRED
                                                          then case res2 of RES_NUM x => if x = 0 then RES_NUM 0 else RES_NUM (x-1)
                                                               | _ => RES_ERROR "Function pred needs parameter of type int"
                                                          else RES_ERROR "Attemping to call something that is not a function"
                                            end

  | AST_ID name                 => lookup_env(env, name)
  | AST_FUN  (var, exp)         => RES_CLOSURE(var,exp,env)

(*  Once you have defined interp, you can try out simple examples by
      interp (parsestr "succ (succ 7)"), new_env());
    and you can try out larger examples by
      interp (parsefile "your-file-here", new_env());
*)
