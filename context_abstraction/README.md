# Context Abstraction Exercise

Exercises marked with ⭐️ are the most important ones. Exercises marked with 🔥 are the most challenging ones. You do not need to complete all exercises to succeed in this class, and you do not need to do all exercises in the order they are written.

**Your are allowed to copy/clone/fork this repository, but not to share solutions of the exercise in any public repository or web page.**

## Testing

To test your code, run `sbt test` in the root directory of the project. We are using [ScalaCheck](https://scalacheck.org/) for automated property-based testing. ScalaCheck is inspired by Haskell’s [QuickCheck](https://hackage.haskell.org/package/QuickCheck).

ScalaCheck generates random inputs for your functions and checks if the properties hold for those inputs. If your code fails the tests, ScalaCheck will provide a counter-example to help you debug your code. Then, you can try the counter-example in the worksheet to check what went wrong.

## Ordering ⭐️

The skeleton for this part can be found in src/main/scala/contextual/Orderings.scala.

In mathematics, a **partial order** on a set is an arrangement such that, for certain pairs of elements, one precedes the other. The word partial indicates that not every pair of elements needs to be comparable; that is, there may be pairs for which neither element precedes the other. For example, the set of strings is partially ordered by the `substring` operator. There are pairs of strings that are not comparable by `substring`, e.g. “abc” and “def” are not of each other.

A **total order** or **linear order** is a partial order in which any two elements are comparable. For example, the set of integers is totally ordered by the `<=` operator.

See [Partially ordered set](https://en.wikipedia.org/wiki/Partially_ordered_set) and [Total order](https://en.wikipedia.org/wiki/Total_order) for more details.

In Scala, the partial order is represented by `scala.math.PartialOrdering`, and the total order is represented by `scala.math.Ordering` (as a subtype of `PartialOrdering`).

`Ordering` is a type class that represents the result of comparing two values of a type. It describes both an equivalence relation and a total order on such equivalence classes:

* Equivalence relation: `{(a, b) | compare(a, b) == 0}`
* Total order: `{(a, b) | compare(a, b) <= 0}`

The sign of the result of `compare` indicates the ordering of the two values, where the exact values do not matter:

* Negative: `a < b`
* Zero: `a == b`
* Positive: `a > b`

To ensure the result is total ordering, the following properties must hold for all `a`, `b`, and `c` on `compare`:

```scala
def inverse[T](x: T, y: T)(using ord: Ordering[T]): Boolean =
  sign(ord.compare(x, y)) == -sign(ord.compare(y, x))

def transitive[T](x: T, y: T, z: T)(using ord: Ordering[T]): Boolean =
  !(ord.compare(x, y) > 0 && ord.compare(y, z) > 0) || ord.compare(x, z) > 0

def consistent[T](x: T, y: T, z: T)(using ord: Ordering[T]): Boolean =
  ord.compare(x, y) != 0 || sign(ord.compare(x, z)) == sign(ord.compare(y, z))
```

(where the helper function `sign` represents the sign of the result).

### Implement the `Ordering` typeclass for pairs

Implement an instance of the `Ordering` type class for pairs of type `(A, B)`, where `A` and `B` have `Ordering` instances defined on them.

It should be implemented as a lexicographic ordering, where the first component is ordered first, and the second component is ordered second. That is, `(a, b) <= (c, d)` if and only if:

* `a < c`, or
* `a == c` and `b <= d`.

```scala
// TODO: you should modify this signature according to the requirements
given pairOrdering[A, B]: Ordering[(A, B)] with
  def compare(x: (A, B), y: (A, B)): Int = ???
```

Example use case: Consider a program for managing an address book. We would like to sort the addresses by zip codes first and then by street name. Two addresses with different zip codes are ordered according to their zip code, otherwise (when the zip codes are the same) the addresses are sorted by street name. E.g.

```scala
type Address = (Int, String) 
val addressBook: List[Address] = List(
  (1020, "Av. d'Epenex"),
  (1015, "Rte des Noyerettes"),
  (1015, "Rte Cantonale"))

val sortedAddressBook = addressBook.sorted(using Orderings.pairOrdering)
```

### Mapping ordering

Suppose we have a data structure to store students’ information:

```scala
case class Student(name: String, year: Int)
```

If we want to sort a list of students by their years of admission first and names second, we can create an ordering for `Student` as follows:

```scala
given studentOrdering1: Ordering[Student] with
  def compare(x: Student, y: Student): Int =
    val cmp1 = x.year.compare(y.year)
    if cmp1 != 0 then cmp1 else x.name.compare(y.name)
```

However, we already have orderings for `Int`, `String`, and pairs, so we can use them to create an ordering for `Student`.

Your task is to implement a general function “mapping” a known ordering for `B` to a new ordering for `A`. It is defined as a function `orderingBy`, that takes a function `f: A => B` and an ordering for `B` and returns an ordering for `A`. The mapping function `f` should be constructed carefully such that the resulting ordering follows the law of total ordering as well.

```scala
def orderingBy[A, B](f: A => B)(using ord: Ordering[B]): Ordering[A] =
  ???
```

With orderingBy, we can create an ordering for Student:

```scala
given studentOrdering2: Ordering[Student] = orderingBy((s: Student) => (s.year, s.name))
```

## Abstract Algebra with Type Classes

The skeleton for this part can be found in src/main/scala/contextual/Algebra.scala.

Recall the `SemiGroup` and `Monoid` type classes from the lecture:

```scala
trait SemiGroup[A]:
  extension (x: A) def combine(y: A): A

trait Monoid[A] extends SemiGroup[A]:
  def unit: A
```

The laws for `SemiGroup` and `Monoid` are:

```scala
def associative[T](x: T, y: T, z: T)(using sg: SemiGroup[T]): Boolean =
  x.combine(y).combine(z) == x.combine(y.combine(z))

def identity[T](x: T)(using m: Monoid[T]): Boolean =
  m.unit.combine(x) == x && x.combine(m.unit) == x
```

(since `Monoid` is also a `SemiGroup`, the associative law is also required for `Monoid`)

### Generalize `reduce`

We have seen a version of `reduce` for a list of `T` where `T` has a `SemiGroup` instance:

```scala
def reduceSemiGroup[T: SemiGroup](xs: List[T]): T =
  xs.reduceLeft(_.combine(_))
```

When we try to `generalize` reduce to work with any list, we run into a problem: we don’t have a default or fallback value to return when the list is empty.

Your task is to generalize `reduce` to work on lists of `T` where `T` has a `Monoid` instance such that it also works for empty lists.

```scala
def reduce[T: Monoid](xs: List[T]): T =
  xs.foldLeft(???)(???)
```

### A general way to lift a SemiGroup to a Monoid

There are some types that have a `SemiGroup` instance but not a `Monoid` instance. For example, the set of integers (`BigInt`) forms a `SemiGroup` through `min`. However, there is no corresponding identity element to form a `Monoid`.

```scala
given SemiGroup[BigInt] with
  extension (x: BigInt) def combine(y: BigInt): BigInt = x.min(y)
```

To show that `BigInt` does not form a `Monoid` through `min`, suppose there exists an identity element `u` in `BigInt` such that `min(a, u) == a` for all `a: BigInt`. However, we can always find a `v = u + 1` such that `u < v` and `min(v, u) == u != v`. Hence, there is no such identity element `u` in `BigInt`.

By adding an extra “positve infinity” element to the set and define it as the greatest element, we can form a `Monoid` through `min`.

In Scala, we usually use `Option` to represent the default/fallback value. Therefore, we can use `Option[BigInt]` to represent the integer set with “positive infinity”:

* `None` represents the “positive infinity” element
* `Some(a)` represents the integer `a`
* `None.combine(Some(a))` is `Some(a)` for all `a: BigInt`, since `a < +infinity`.
* `Some(a).combine(Some(b))` is `Some(min(a, b))` for all `a, b: BigInt`

The properties of `Monoid[Option[BigInt]]` correspond to the properties of `Monoid[Option[A]]` where `A` has a `SemiGroup` instance.

Your task is to implement a `Monoid` instance for `Option[A]` given a `SemiGroup` instance for `A`.

```scala
given [A: SemiGroup]: Monoid[Option[A]] = ??? // you can modify `=` to `with`
```

With the `Monoid` instance for `Option[A]`, we can lift a list of `BigInt` to a list of `Option[BigInt]` and then apply the general `reduce` function to it to find the minimum element.

```scala
val bigints = List(BigInt(-8), BigInt(2), BigInt(0), BigInt(4), BigInt(100))
reduceSemiGroup(bigints)
val posInfinity: Option[BigInt] = None
val liftedBigints = posInfinity :: bigints.map(Option(_))
// adding a positive infinity value to the list should not change the result
reduce(liftedBigints)
```

## Implicit Context ⭐️

You have seen in previous exercises an enum for arithmetic expressions. Let’s augment it with a `Let` form:

```scala
enum Expr:
  case Num(value: Int)
  case Var(name: String)
  case Let(name: String, value: Expr, body: Expr)
  case Add(e1: Expr, e2: Expr)
  case Sub(e1: Expr, e2: Expr)
  case Mul(e1: Expr, e2: Expr)
```

Write an eval function for expressions of this type. You should not use any mutable or global variables or mutable collections.

```scala
def evaluate(e: Expr): Int =
  def recur(e: Expr)(using ctx: Map[String, Int]): Int = e match
    case _ => ??? //this case is just to set up the skeleton, there is not necessarily a case "_"
  recur(e)(using Map.empty)
```

`Let(”x”, e1, e2)` should be evaluated like `{val x = e1; e2}`. You can assume that every `Var(x)` occurs in the body `b` of an enclosing `Let(x, e, b)`.

For example, the following expression should evaluate to `4` (can be found in the worksheet):

```scala
val e3 = Let("x", Let("y", Num(1), Add(Var("y"), Var("y"))), Mul(Var("x"), Var("x")))
evaluate(e3)
```

## Acknowledgement

The monoid exercise is adapted from the documentation of the [Cats](https://typelevel.org/cats/typeclasses/monoid.html) library.