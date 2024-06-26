# Structural Induction

**Your are allowed to copy/clone/fork this repository, but not to share solutions of the exercise in any public repository or web page.**

Note that except the part on writing your own tests, this exercise is meant to be solved on paper. You do not need to add/complete any code.

## Proofs on lists

### Composition of maps ⭐️

Prove that the following equivalence holds by using inductive reasoning:

```
∀ (l: List[T]),
  l.map(f).map(g) === l.map(f `andThen` g)
```

(`∀` is short for “for all”, so the statement above says: “forall list `l` of type `List[T]`, `l.map(f).map(g)` equals ``l.map(f `andThen` g)``”.)

Here are the relevant axioms for this proof:

1. `Nil.map(f) === Nil`
1. `(x :: xs).map(f) === f(x) :: (xs.map(f))`
1. ``(f `andThen` g)(x) === g(f(x))``

Be very precise in your proof:

- Make sure to state what you want to prove, and what your induction hypothesis is, if any.
- Clearly state which axiom you use at each step, and when/if you use the induction hypothesis.
- Use only one axiom / hypothesis at each step: applying two axioms requires two steps.
- Underline the part of each expression on which you apply the axiom or hypothesis at each step.

### A more complicated proof

We want to implement a function `sum(list: List[Int]): Int`, which returns the sum of the elements of a list of Int-s. We can easily specify that function as follows:

```
(1)  sum(Nil) === 0
(2)  sum(x :: xs) === x + sum(xs)
```

If we naively translate this specification into a Scala implementation, we end up with a non-tail-recursive function. Instead, we implement it using `foldLeft`:

```scala
def betterSum(list: List[Int]): Int =
  list.foldLeft(0)(add)

def add(a: Int, b: Int): Int = a + b
```

However, that implementation is not obviously correct anymore. We would like to prove that it is correct for all lists of integers. In other words, we want to prove that

```
list.foldLeft(0)(add) === sum(list)
```

for all lists of integers.

In addition to the specification of sum (axioms 1-2), you may use the following axioms:

```
(3)  Nil.foldLeft(z)(f) === z
(4)  (x :: xs).foldLeft(z)(f) === xs.foldLeft(f(z, x))(f)
(5)  add(a, b) === a + b
(6)  a + b === b + a
(7)  (a + b) + c === a + (b + c)
(8)  a + 0 === a
```

Axioms 3-5 follow from the implementations of `foldLeft` and `add`. Axioms 6-8 encode well-known properties of `Int.+`: commutativity, associativity, and neutral element.

**Your task**: Prove the following lemma by structural induction:

```
∀ (l: List[Int]) (z: Int), l.foldLeft(z)(add) === z + sum(l)
```

From that lemma, we can (with the help of axioms 6 and 8) derive that the implementation of `betterSum` is correct by substituting `0` for `z` in the lemma. You are not asked to do that last bit.

## Proofs on Trees and other structures

Now we will study structurally induction on trees. We’ll also see how to write proofs about arbitrary structurally recursive functions, not just `map`, `fold`, and other functions for which axioms were given by the problem statement.

We’ll use the `Expr` type from the calculator lab as our motivating example. Recall the definition of Expr:

```scala
enum Expr:
  case Num(value: BigInt)
  case Var(name: String)
  case Add(e1: Expr, e2: Expr)
  case Sub(e1: Expr, e2: Expr)
  case Mul(e1: Expr, e2: Expr)
```

… and of `evaluate`, written here with a function context. Notice that we return a `BigInt` (not an `Option[BigInt]`) for simplicity, which means that undefined variables have a default value (and that there are no overflows, since we’re using `BigInt`):

```scala
def evaluate(ctx: String => BigInt, e: Expr): BigInt = e match
  case Num(value)  => value
  case Var(name)   => ctx(name)
  case Add(e1, e2) => evaluate(ctx, e1) + evaluate(ctx, e2)
  case Sub(e1, e2) => evaluate(ctx, e1) - evaluate(ctx, e2)
  case Mul(e1, e2) => evaluate(ctx, e1) * evaluate(ctx, e2)
```

To reason about arithmetic in the rest of this exercise, use the following axioms:

* `ZeroPlusZero` `0 + 0 === 0`
* `CommuPlus` `a + b === b + a`
* `CommuMul` `a * b === b * a`
* `BoolEq` `(a == b) === true ⇔ a === b`
* `TrueAnd` `a && b === true ⇔ ((a === true) and (b === true))`

Let’s warm up with a simple operation, `mirror`:

```scala
def mirror(e: Expr): Expr = e match
  case Num(value)  => Num(value)
  case Var(name)   => Var(name)
  case Add(e1, e2) => Add(mirror(e2), mirror(e1))
  case Sub(e1, e2) => Sub(mirror(e2), mirror(e1))
  case Mul(e1, e2) => Mul(mirror(e2), mirror(e1))
```

`mirror` mirrors the left and right children of an expression tree. For example, `mirror(Add(Num(1), Num(2)))` is `Add(Num(2), Num(1))`.

### Writing axioms from definitions

We have seen three ways to reason about the execution of code until now:

1. The substitution method with concrete variables, to understand how code works on a specific input;
2. Applying rewrite rules (such as `MapCons` or `IH`), to prove properties valid for all inputs;
3. The substitution method with variables, to reason about strongest postconditions.

In formal proofs we applied only (2), rewrite rules, but these techniques are related: uses of the substitution method can be converted into applications of rewrite rules. For example, for `evaluate`, applying the substitution method gives us the following axioms for all `value`, `name`, `e1`, and `e2`:

* `EvalNum`: `evaluate(ctx, Num(value)) === value`
* `EvalVar`: `evaluate(ctx, Var(name)) === ctx(name)`
* `EvalAdd`: `evaluate(ctx, Add(e1, e2)) === evaluate(ctx, e1) + evaluate(ctx, e2)`
* `EvalSub`: `evaluate(ctx, Sub(e1, e2)) === evaluate(ctx, e1) - evaluate(ctx, e2)`
* `EvalMul`: `evaluate(ctx, Mul(e1, e2)) === evaluate(ctx, e1) * evaluate(ctx, e2)`

Write similar axioms for `mirror`, above.

### Proving properties of mirror ⭐️

Does `mirror` preserve the value of its input expression? That is, can we prove `eval(ctx, mirror(e)) == eval(ctx, e)`?

If yes, write a proof of this fact using structural induction on `e`. If not, give a counterexample (a value for `e` and `ctx` such that the theorem does *not* hold, then conjecture a theorem that does hold and prove it).

<details> <summary>Hint</summary>

It does not hold: `mirror` changes the value of `Sub` nodes: `eval(Sub(1, 3))` is `-2`, but `eval(mirror(Sub(1, 3)))` is `2`. If you attempt a proof, you will get stuck on the inductive step for `Sub`:

```
        evaluate(ctx, mirror(Sub(e1, e2)))
    === evaluate(ctx, Sub(mirror(e2), mirror(e1))) // by MirrorSub
    === evaluate(ctx, mirror(e2)) + evaluate(ctx, mirror(e1)) // by EvalSub
    === evaluate(ctx, e2) - evaluate(ctx, e1) // by IH1 and IH2
    =/= evaluate(ctx, e1) - evaluate(ctx, e2) // Not equal!
    === evaluate(ctx, Sub(e1, e2)) // by EvalSub
```

What does hold, however, is the fact that `mirror(mirror(e)) == e`. Functions like `mirror` that verify this property are called `involutive`.

Now prove it!
</details>

## Verification of static analyzers ⭐️

Let’s continue our program proofs journey by looking at a *static* analyzer. A static analyzer is a program that checks a property of another program.

For example, the following static analyzer checks whether the input expression `e` *always* evaluate to `0`:

```scala
def zeroExpr(e: Expr): Boolean = e match
  case Num(value)  => value == 0
  case Var(_)      => false
  case Add(e1, e2) => zeroExpr(e1) && zeroExpr(e2)
  case Sub(e1, e2) => zeroExpr(e1) && zeroExpr(e2)
  case Mul(e1, e2) => zeroExpr(e1) && zeroExpr(e2)
```

It correctly identifies that `Add(Num(0), Mul(Num(0), Num(0)))` is always zero by returning `true`, and it returns `false` for `Add(Num(0), Mul(Num(1), Num(0)))`.

We can write the desired property formally as `P(e) === ∀ ctx. evaluate(ctx, e) == 0`.

A static analyzer is *sound* if all programs for which it returns `true` have the property (in other words, it has no false positives):

* **Soundness**: `∀ e. zeroExpr(e) == true ⇒ P(e)`. Or equivalently: `∀ e. ¬P(e) ⇒ zeroExpr(e) == false`.

It is *complete* if it identifies all programs that have the property (in other words, there are no false negatives: all programs for which it returns `false` do not have the property).

* **Completeness**: `∀ e. P(e) ⇒ zeroExpr(e) == true`. Or equivalently: `∀ e. zeroExpr(e) == false ⇒ ¬P(e)`.

1. Write rewriting rules for `zeroExpr`.

2. Is `zeroExpr` complete? Is it sound? For each of these properties, provide either a counterexample if the property is wrong, or a proof if it’s correct.

## constfold 🔥

We can now move to a more complicated property: the soundness of a program transformation, `constfold(e)`.

For static analyzers, soundness meant answering `true` only if the input expression had the property. For program transformations, soundness means producing an expression equivalent to the original input. Two expressions are said to be equivalent if they evaluate to the same value under arbitrary contexts, for instance `Add(Num(1),Var(x))` is equivalent to `Add(Var(x),Num(1))`.

`constfold` is implemented as a simple function that maps `constfold1` over the whole expression tree.

```scala
def mapExpr(e: Expr, f: Expr => Expr): Expr =
  val mapped = e match
    case Num(_)      => e
    case Var(_)      => e
    case Add(e1, e2) => Add(mapExpr(e1, f), mapExpr(e2, f))
    case Sub(e1, e2) => Sub(mapExpr(e1, f), mapExpr(e2, f))
    case Mul(e1, e2) => Mul(mapExpr(e1, f), mapExpr(e2, f))
  f(mapped)

def constfold1(expr: Expr) = expr match
  case Add(Num(n1), Num(n2)) => Num(n1 + n2)
  case Sub(Num(n1), Num(n2)) => Num(n1 - n2)
  case Mul(Num(n1), Num(n2)) => Num(n1 * n2)
  case e                     => e

def constfold(e: Expr): Expr =
  mapExpr(e, constfold1)
```

We can use the same induction techniques on trees that we have learnt to reason about the correctness of `constfold`.

1. Derive rewriting rules for `mapExpr` and `constfold1`.

2. Prove the soundness of `constfold1`. This means that given an arbitrary `e`, we want to show `evaluate(ctx, constfold1(e)) === evaluate(ctx, e)`.

2. Prove the soundness of `mapExpr` assuming that `f` is sound. This means that given an `f` such that `∀ e ctx. evaluate(ctx, f(e)) === evaluate(ctx, e)`, we want to prove `∀ e ctx. evaluate(ctx, mapExpr(e,f)) === evaluate(ctx, e)`.

8. Combine the previous two results to prove the soundness of constfold.

## Write your own tests

So far we usually included some tests to verify your code. Now it's your turn to write the tests on your own. There are many different ways how to write tests depending on what syntax you prefer and what you want to test.

### Set up a test file

Your scala code files are usually located at `src/main/scala/...`. The corresponding test files then are usually located at `src/test/scala/...`. You usually name your test file the same name as the file you want to test with a "Test" attached. In our example we have the scala file `src/main/scala/examples/examples.scala`. The corresponding test file is already included `src/test/scala/examplesTest.scala`. Actually your test file is just another scala file and you can do everything in it, what you can do in a usual scala file (but of course the different location is intended to separate tests from actual program code). For each scala file of you project, you should have a separate test file. The head of your file should usually look like this:

```scala
package nameOfPackage

import munit.*

class NameOfTestSet extends FunSuite {
  //Here is what you want to test
}
```

Tests are grouped in a class, which you can name by your own preference. Here we used `munit` which provides the basic scala syntax for writing tests but other libraries are possible, too. For example the tests for the contextual abstraction exercise use a different library. In our case a test is declared within the class `ExampleTest` by invoking `test`, giving it a name. The test passes as long as the body does not crash by an exception. For example

```scala
test("example test that succeeds") {
    val obtained = 42
    val expected = 42
    assertEquals(obtained, expected)
    assert(true)
    assert(2+4 == 6)
}

test("example test that fails"){
  assertEquals(2+4,8)
  assert(false)
  assert(2*2 == 4)
}
```

"example test that fails" fails in the first line of the body, the rest of the test is not checked after the crash, even though `assert(2*2==4)` would pass. We can run the tests by `test` in `sbt` or if you don't want to test everything you may use `testOnly` followed by the name of the package and the class of tests you would like to run, e.g. `testOnly examples.SucceedFailTest`.

### How to write tests properly

You are quite free on what you want to test. Take for example the factorial function `tailfac` that we provided in the file `examples.scala`. You are free to test random values that you calculated by hand e.g. `assertEquals(tailfac(4), 24)`, but this may be true only by accident and it may fail on other inputs. You will never be able to check correctness for all values by testing. What's the problem with a test like `assertEquals(fac,tailfac)`?

<details><summary> Solution</summary>

This test would immediately fail with
```
values are not the same
=> Obtained
examples.ExampleTests$$Lambda$6969/0x0000000101e40840@4038e68c
=> Diff (- obtained, + expected)
-examples.ExampleTests$$Lambda$6969/0x0000000101e40840@4038e68c
+examples.ExampleTests$$Lambda$6970/0x0000000101e41840@1b0f3e8a
```
because it is comparing the references instead of the functions, the scala code of `fac` and `tailfac` is not equal. Apart from that the function problem is undecidable.
</details>

Therefore you should aim to test specific special cases, like the base case or what should happen on a wrong input

```scala
test("pathologicalCases"){
  assertEquals(tailfac(1),1)
  assertEquals(tailfac(0),1)
  assertEquals(tailfac(-1),1)
}
```

Your tests should always be according to your program: The value that we expect from `tailfac(-1)` depends on our definition of `tailfac`. To test several inputs at once you can simply use scala code to construct those.

```scala
  test("samples"){
  def fac(n: Int): Int = 
    if n <= 1 then 1 else n*fac(n-1)
  for i <- 1 to 10 yield assertEquals(tailfac(i),fac(i))
  }
```

Ensure that your tests cover both expected success cases and potential failure cases.

### Write your own tests

Now it's your turn. Make a test file and write appropriate tests for the functions in the `Expr.scala` file. As mentioned above there is no right or wrong, it depends on what specifications of the functions you would like to test.