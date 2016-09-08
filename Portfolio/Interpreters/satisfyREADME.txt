Copied with permission from Dr. Zena Ariola.

"A propositional formula is said to be satisfiable if there exists a way of setting propositional variables to true or false that makes the whole formula true. It is said to be falsifiable if there are values for its variables that makes the formula false. Finally, it is said to be valid if all possible choices of values for the variables makes the formula true (i.e., if it is not falsifiable).

For example, (p∨q)∧¬p is both satisfiable (e.g., make p false and q true) and falsifiable (e.g., make p and q both false). The formula p∨¬p is satisfiable and valid, and not falsifiable.

One way to determine whether a formula is satisfiable or falsifiable is to look at all possible assignments. This is a bit wasteful, since if the formula is something like p∧¬p∧(q∨r∨s∨t) we would like to stop trying to satisfy the formula almost immediately, rather than trying all 32 possibilities. Therefore, we can try to find a satisfying or falsifying assignment by search, short-circuiting whenever we realize that our goal is impossible (e.g., after seeing p∧¬p).

If we represent propositional formulas with the datatype

datatype prop = 
  Var of string
| Or of prop * prop
| And of prop * prop
| If of prop * prop
| Not of prop
then the following code for determining satisfiability seems initially plausible (where a bool env is an environment mapping variables to booleans):

(* satisfy : prop * bool env -> (bool env) option
     Determines whether there is a way to make the given
     proposition true, given an assignment specifying truth values
     for the variables we have seen so far. Returns SOME assignment
     if one exists, and otherwise returns NONE.
 *)
fun satisfy (Var v, asn) =
   (case lookup (asn, v) of
      NONE       => (* We can make the formula (consisting of
                       just this variable) true by making the
                       variable true. *)
                    SOME (extend (asn, v, true))
    | SOME true  => (* We already decided this variable should
                       be true, so return the assignment unchanged *)
                    SOME asn
    | SOME false => (* Oops...we already decided this variable
                       must be false, so we cannot make the
                       given formula true. *)
                    NONE)
| satisfy (And(p1,p2), asn) =
   (* Try to find an assignment satisfying both p1 and p2. *)
   (case satisfy(p1, asn) of
      SOME asn’ => satisfy(p2, asn’)
    | NONE      => NONE)

| satisfy (Or(p1, p2), asn) =
   (* Try to satisfy p1. If that is impossible, satisfy p2. *)
   (case satisfy(p1, asn) of
      SOME asn’ => SOME asn’
    | NONE      => satisfy(p2, asn))
| satisfy (Not p, asn) = (* There is a way to make (Not p) true
                            iff there is a way to make p false
                            (without changing the truth values of any
                            variables appear in asn). *)
   falsify(p, asn)
| satisfy (If(p1, p2), asn) = satisfy(Or(Not(p1), p2), asn)
(where we assume there is a very similar function falsify : prop * bool env -> (bool env) option for falsifying a proposition).

Unfortunately, if we try to check whether (p∨q)∧¬p is satisfiable by running

satisfy(And(Or(Var "p", Var "q"), Not (Var "p")), empty)
we wrongly get the answer NONE.

Make sure you understand why before continuing on!

The problem is that this code signals success by returning, but we expect a function to return only once. If we cannot extend the answer to satisfy the “rest” of a proposition, there’s no way to un-return and request a different satisfying assignment.

There are several ways to fix this, but they all boil down to turning “the rest of the proposition” into an extra parameter. One of the simplest methods is to add a continuation function:

(*
   These functions should return true iff there is an extension of
   the given assignment satisfying/falsifying the given
   proposition that ALSO makes the continuation function return
   true. [Intuitively, the continuation function should check
   that "the rest of the proposition" is satisfiable or
   falsifiable respectively.]
*)
satisfy : prop * bool env * (bool env -> bool) -> bool
falisfy : prop * bool env * (bool env -> bool) -> bool
so that we can write

fun satisfiable(p) = satisfy(p, empty, (fn _ => true))
fun falsifiable(p) = falisfy(p, empty, (fn _ => true))
fun valid(p) = not (falsifiable p)
"