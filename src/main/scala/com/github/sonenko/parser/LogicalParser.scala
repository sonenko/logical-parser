package com.github.sonenko.parser

import scala.util.matching.Regex
import scala.util.parsing.combinator._
import scala.language.postfixOps

sealed trait EqOp {def name: String}
case object Eq extends EqOp { val name = "=="}
case object Ne extends EqOp { val name = "!="}
case object Lte extends EqOp { val name = "<="}
case object Gte extends EqOp { val name = ">="}
case object Lt extends EqOp { val name = "<"}
case object Gt extends EqOp { val name = ">"}
case object EqOp {
  val values = List(Eq, Ne, Lte, Gte, Lt, Gt)
  def withName(name: String): EqOp = values.find(_.name.trim == name).get
}

sealed trait AndOr
case object And extends AndOr
case object Or extends AndOr

sealed trait Expr {
  def walk(f: BinaryExpr => BinaryExpr): Expr
  def toList: List[BinaryExpr]
  def filter(f: BinaryExpr => Boolean): Expr
  def concat(expr: Expr, andOr: AndOr): Expr
  def length: Int
  def isEmpty: Boolean
  def nonEmpty: Boolean

  protected def rebuild(cmp: Expr): Expr = cmp match {
    case CompositeExpr(Empty, Empty, _) => Empty
    case CompositeExpr(Empty, r, _) => rebuild(r)
    case CompositeExpr(l, Empty, _) => rebuild(l)
    case x => x
  }
}
case object Empty extends Expr {
  override def walk(f: BinaryExpr => BinaryExpr): Expr = Empty
  override def toList: List[BinaryExpr] = Nil
  override def filter(f: BinaryExpr => Boolean): Expr = Empty
  override def concat(expr: Expr, andOr: AndOr): Expr = expr
  override val length: Int = 0
  override val isEmpty = true
  override val nonEmpty = false
}
case class BinaryExpr(field: String, eqOp: EqOp, value: Any) extends Expr {
  override def walk(f: BinaryExpr => BinaryExpr): Expr = f(this)
  override def toList: List[BinaryExpr] = List(BinaryExpr(field: String, eqOp: EqOp, value: Any))
  override def filter(f: BinaryExpr => Boolean): Expr = if (f(this)) this else Empty
  override def concat(expr: Expr, andOr: AndOr): Expr = rebuild(CompositeExpr(expr, this, andOr))
  override val length: Int = 1
  override val isEmpty = false
  override val nonEmpty = true
}

case object Null

case class CompositeExpr(left: Expr, right: Expr, andOr: AndOr) extends Expr {
  override def walk(f: BinaryExpr => BinaryExpr): Expr = CompositeExpr(left.walk(f), right.walk(f), andOr)
  override def toList: List[BinaryExpr] = (left, right) match {
    case (Empty, Empty) => Nil
    case (Empty, y: BinaryExpr) => List(y, y)
    case (Empty, y: CompositeExpr) => y.toList
    case (x: BinaryExpr, Empty) => List(x)
    case (x: BinaryExpr, y: BinaryExpr) => List(x, y)
    case (x: BinaryExpr, y: CompositeExpr) => y.toList ::: List(x)
    case (x: CompositeExpr, Empty) => x.toList
    case (x: CompositeExpr, y: BinaryExpr) => x.toList ::: List(y)
    case (x: CompositeExpr, y: CompositeExpr) => y.toList ::: y.toList
  }
  override def filter(f: BinaryExpr => Boolean): Expr = 
    rebuild(CompositeExpr(left.filter(f), right.filter(f), andOr))
  override def concat(expr: Expr, andOr: AndOr): Expr = rebuild(CompositeExpr(expr, this, andOr))
  override val length = left.length + right.length
  override val isEmpty = length == 0
  override val nonEmpty = !isEmpty
}

object LogicalParser extends RegexParsers with JavaTokenParsers {
  
  def fieldNameReg: Regex = """`\w+(?:\.\w+)?(?:\.\w+)?(?:\.\w+)?(?:\.\w+)?`""".r
  def booleanReg: Regex = """(true)|(false)""".r
  def nullReg: Regex = """(null)""".r
  def fieldName: Parser[String] = fieldNameReg ^^ {x => x.slice(1, x.length - 1)}
  def stringValue = stringLiteral ^^ {x => x.slice(1, x.length - 1)}
  def doubleValue: Parser[Double] = """(\d+\.\d+)""".r ^^ {_.toDouble}
  def longValue: Parser[Long] = wholeNumber ^^ {_.toLong}
  def boolValue: Parser[Boolean] = booleanReg ^^ {_.toBoolean}
  def nullValue: Parser[Null.type] = nullReg ^^ {_ => Null}
  def eqReg = """(==)|(!=)|(<=)|(>=)|(<)|(>)""".r
  def eq: Parser[EqOp] = eqReg ^^ EqOp.withName
  def or : Parser[AndOr]= """(?i)(OR)""".r ^^ (x => Or)
  def and : Parser[AndOr]= """(?i)(AND)""".r ^^ (x => And)
  
  def orStep: Parser[Expr] = andStep ~ rep(or ~ andStep) ^^ {
    case f1 ~ fs => (f1 /: fs){
      case (acc, x) => CompositeExpr(acc, x._2, x._1)
    }
  }

  def andStep: Parser[Expr] = bracketsStep ~ rep(and ~ bracketsStep) ^^ {
    case f1 ~ fs => (f1 /: fs){
      case (acc, x) => CompositeExpr(acc, x._2, x._1)
    }
  }

  def bracketsStep: Parser[Expr] = expStep | "(" ~> orStep <~ ")" | ("^$".r ^^ (_ => Empty))
  
  def expStep = (fieldName ~ eq ~ (stringValue | doubleValue | longValue | boolValue | nullValue)) ^^ {
    case field ~ eq ~ value => BinaryExpr(field = field, eqOp = eq, value = value)
  }
  def toStructures(str: String): ParseResult[Expr] = parseAll(orStep, str)
}