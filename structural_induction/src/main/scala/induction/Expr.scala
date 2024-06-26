package induction

enum Expr:
  case Num(value: BigInt)
  case Var(name: String)
  case Add(e1: Expr, e2: Expr)
  case Sub(e1: Expr, e2: Expr)
  case Mul(e1: Expr, e2: Expr)

import Expr.*

def evaluate(ctx: String => BigInt, e: Expr): BigInt = e match
  case Num(value)  => value
  case Var(name)   => ctx(name)
  case Add(e1, e2) => evaluate(ctx, e1) + evaluate(ctx, e2)
  case Sub(e1, e2) => evaluate(ctx, e1) - evaluate(ctx, e2)
  case Mul(e1, e2) => evaluate(ctx, e1) * evaluate(ctx, e2)

def mirror(e: Expr): Expr = e match
  case Num(value)  => Num(value)
  case Var(name)   => Var(name)
  case Add(e1, e2) => Add(mirror(e2), mirror(e1))
  case Sub(e1, e2) => Sub(mirror(e2), mirror(e1))
  case Mul(e1, e2) => Mul(mirror(e2), mirror(e1))

def zeroExpr(e: Expr): Boolean = e match
  case Num(value)  => value == 0
  case Var(_)      => false
  case Add(e1, e2) => zeroExpr(e1) && zeroExpr(e2)
  case Sub(e1, e2) => zeroExpr(e1) && zeroExpr(e2)
  case Mul(e1, e2) => zeroExpr(e1) && zeroExpr(e2)

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