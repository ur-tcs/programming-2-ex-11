import contextual.*

import Orderings.*
import Algebra.*
import Expr.*

type Address = (Int, String) 
val addressBook: List[Address] = List(
  (1020, "Av. d'Epenex"),
  (1015, "Rte des Noyerettes"),
  (1015, "Rte Cantonale"))

val sortedAddressBook = addressBook.sorted(using Orderings.pairOrdering)

val students = List(
  Student("Alice", 2021),
  Student("Bob", 2022),
  Student("Charlie", 2021),
  Student("David", 2022),
  Student("Eve", 2023))

val sortedStudents1 = students.sorted(using Orderings.studentOrdering1)
val sortedStudents2 = students.sorted(using Orderings.studentOrdering2)

val xs = List(1, 2, 3, 4, 5)

reduceSemiGroup(xs)(using Algebra.sumSemiGroup)
// error：empty.reduceLeft not supported
// reduceSemiGroup(List.empty)(using Algebra.sumSemiGroup)

reduce(xs)(using Algebra.sumMonoid)
// ok
reduce(List.empty)(using Algebra.sumMonoid)

val bigints = List(BigInt(-8), BigInt(2), BigInt(0), BigInt(4), BigInt(100))
reduceSemiGroup(bigints)
val posInfinity: Option[BigInt] = None
val liftedBigints = posInfinity :: bigints.map(Option(_))
// adding a positive infinity value to the list should not change the result
reduce(liftedBigints)

val e1 = Let("x", Num(1), Add(Var("x"), Num(2)))
evaluate(e1)

val e2 = Let("x", Num(1), Let("y", Num(2), Mul(Var("x"), Var("y"))))
evaluate(e2)

val e3 = Let("x", Let("y", Num(1), Add(Var("y"), Var("y"))), Mul(Var("x"), Var("x")))
evaluate(e3)