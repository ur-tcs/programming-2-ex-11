package contextual

import org.scalacheck.*
import Gen.*
import Arbitrary.arbitrary
import Prop.forAll

class ExprTest extends Properties("Expr"):

  import Expr.*

  def genName(env: Seq[String]): Gen[String] =
    for
      c <- choose('a', 'z')
      cs <- listOfN(2, choose('0', '9'))
      n = (c :: cs).mkString
      if !env.contains(n)
    yield n

  val genNum: Gen[Num] =
    for
      value <- arbitrary[Int]
    yield Num(value)

  def genVar(env: Seq[String]): Gen[Var] =
    for
      name <- oneOf(env)
    yield Var(name)

  def genLet(env: Seq[String]): Gen[Let] =
    for
      name <- genName(env)
      value <- genExpr(env)
      body <- genExpr(name +: env)
    yield Let(name, value, body)

  def genAdd(env: Seq[String]): Gen[Add] =
    for
      e1 <- genExpr(env)
      e2 <- genExpr(env)
    yield Add(e1, e2)

  def genSub(env: Seq[String]): Gen[Sub] =
    for
      e1 <- genExpr(env)
      e2 <- genExpr(env)
    yield Sub(e1, e2)

  def genMul(env: Seq[String]): Gen[Mul] =
    for
      e1 <- genExpr(env)
      e2 <- genExpr(env)
    yield Mul(e1, e2)

  def genExpr(env: Seq[String]): Gen[Expr] =
    for
      c <- choose(0, 2)
      expr <- c match
        case 0 => if env.isEmpty then genNum else oneOf(genNum, genVar(env))
        case 1 => genLet(env)
        case 2 => oneOf(genAdd(env), genSub(env), genMul(env))
    yield expr

  given Arbitrary[Expr] = Arbitrary(genExpr(Seq.empty))

  def eliminateLets(e: Expr, env: Map[String, Expr]): Expr = e match
    case Num(value) => Num(value)
    case Var(name)  => env(name)
    case Let(name, value, body) =>
      val value1 = eliminateLets(value, env)
      val body1 = eliminateLets(body, env + (name -> value1))
      eliminateLets(body1, env)
    case Add(e1, e2) => Add(eliminateLets(e1, env), eliminateLets(e2, env))
    case Sub(e1, e2) => Sub(eliminateLets(e1, env), eliminateLets(e2, env))
    case Mul(e1, e2) => Mul(eliminateLets(e1, env), eliminateLets(e2, env))

  def evaluateWithoutLetsAndVars(e: Expr): Int =
    def recur(e: Expr): Int = e match
      case Num(value)   => value
      case Var(_)       => throw new Exception("var not allowed")
      case Let(_, _, _) => throw new Exception("let not allowed")
      case Add(e1, e2)  => recur(e1) + recur(e2)
      case Sub(e1, e2)  => recur(e1) - recur(e2)
      case Mul(e1, e2)  => recur(e1) * recur(e2)
    recur(e)

  property("evaluate") = forAll { (e: Expr) =>
    evaluate(e) == evaluateWithoutLetsAndVars(eliminateLets(e, Map.empty))
  }