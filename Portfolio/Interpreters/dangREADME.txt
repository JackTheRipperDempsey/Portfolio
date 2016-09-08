This is taken from Daniel Friedman's 'devils and angels' exercise, as adapted by Dr. Zena Ariola.  Copied here with permission by the latter.

"The language DAng is an ordinary call-by-value, statically-scoped functional language. In addition, it offers exception-handling facilities in the form of three rather unusual control constructs:

Milestone(<exp>)
When a Milestone is encountered, it becomes charged: the current context in which <exp> occurs is remembered for later use. This is essentially a function of one argument that acts as the identity on <exp>. Unless a Devil() call is subsequently encountered ...

Devil(<exp>)
Devil(e) sends control back to the most recently encountered Milestone(), after its argument is evaluated to some value v. This value v is passed to the continuation commencing at the Milestone(), as if v were the result returned by the Milestone() expression. Presumably this allows the computation to take a different path, possibly avoiding the Devil() that is lurking somewhere ahead on the previously used path.

If another Devil(), or maybe even the same Devil(), is subsequently encountered, then control passes back to the penultimate milestone, not to the one just used. In other words, each milestone can be returned to exactly once; a succession of Devil()s pushes the computation back to earlier and earlier states.

If a Devil() is encountered with no milestone remaining, it has no effect. Unless an Angel() is subsequently encountered ...

Angel(<exp>)
Angel() behaves similarly to Devil(), except that it moves a computation forward. When an expression Angel(e) is encountered, e is first evaluated, resulting in some value v. The computation is then moved forward to where it was when it most recently encountered a Devil() expression, with v given to the Devil()'s continuation as if it were the value of the Devil() expression.

A succession of Angel()s pushes the computation forward to more advanced stages. If another Angel(), or maybe even the same Angel(), is subsequently encountered, then control passes back to the penultimate Devil(), not to the one just used.

If an Angel() is encountered with no Devil() remaining, it has no effect."