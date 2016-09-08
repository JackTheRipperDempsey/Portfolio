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
  | RES_FUN   of (string * term);

(* Here is a basic environment implementation *)
exception not_found;
datatype env = Env of (string * term) list

fun new_env() = Env(nil);
fun extend_env (Env(oldterms), id, value) = Env( (id, value):: oldterms);

fun interp (exp : term, env) =

  case exp of
    AST_ERROR s                 => RES_ERROR s
  | AST_NUM  x                  => RES_NUM x
  | AST_BOOL b                  => RES_BOOL b
  | AST_SUCC                    => RES_SUCC
  | AST_PRED                    => RES_PRED
  | AST_ISZERO                  => RES_ISZERO
  | AST_IF (exp1, exp2, exp3)   => let 
					val res = interp(exp1, env)
				   in
					if not(res = RES_BOOL true) andalso not(res = RES_BOOL false)
					then RES_ERROR "Conditional not boolean"
					else let
                        val RES_BOOL b = res
					     in 
						if b
						then interp(exp2,env)
						else interp(exp3,env)
					     end
				   end
| AST_APP (exp1, exp2)        => let val dummy = 0 in
                                    case exp1 of AST_FUN (var, f) =>
                                    let
                                        val env2 = extend_env(env,var,exp2)
                                    in
                                        interp(f, env2)
                                    end
                                 | _ =>
                                    let val res1 = interp(exp1,env)
                                        val res2 = interp(exp2,env)
                                    in
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
                                  end
    | AST_ID name                 => lookup_env(env, name)
    | AST_FUN  (var, exp)         => RES_FUN (var, exp)

and lookup_env (Env(nil), id) = (print("Free Var!! "^id); raise not_found)
    | lookup_env (Env((id1,term1)::nextTerms), id) =
        if (id1 = id)
        then interp( term1, new_env())
        else lookup_env (Env(nextTerms), id);


(*  Once you have defined interp, you can try out simple examples by
      interp (parsestr "succ (succ 7)", new_env());
      interp (parsestr "pred (pred 7)", new_env());
      interp (parsestr "succ (pred 7)", new_env());
      interp (parsestr "iszero (succ 7)", new_env());
      interp (parsestr "(fn x => iszero (pred x)) 1", new_env());
      interp (parsestr "(fn x => if iszero(x) then 1 else 0) 0", new_env());
      interp (parsestr "(fn x => if iszero(0) then 1 else 0) 0", new_env());
      interp (parsestr "(fn x => succ(pred(x))) 0", new_env());
      interp (parsestr "succ (succ (succ 7))", new_env());
      interp (parsestr "(fn x => succ (succ (succ x))) 7", new_env());

    and you can try out larger examples by
      interp (parsefile "your-file-here", new_env());
*)
