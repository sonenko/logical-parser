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

sealed trait Expr
case object Empty extends Expr
case class BinaryExpr[T](field: String, eqOp: EqOp, value: T) extends Expr
case class CompositeExpr(left: Expr, right: Expr, andOr: AndOr) extends Expr

object LogicalParser extends RegexParsers with JavaTokenParsers {
  
  def fieldNameReg: Regex = """`\w+(?:\.\w+)?(?:\.\w+)?(?:\.\w+)?(?:\.\w+)?`""".r
  def fieldName: Parser[String] = fieldNameReg ^^ {x => x.slice(1, x.length - 1)}
  def stringValue = stringLiteral ^^ {x => x.slice(1, x.length - 1)}
  def doubleValue: Parser[Double] = """(\d+\.\d+)""".r ^^ {_.toDouble}
  def longValue: Parser[Long] = wholeNumber ^^ {_.toLong}
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
  
  def expStep = (fieldName ~ eq ~ (stringValue | doubleValue | longValue)) ^^ {
    case field ~ eq ~ value => BinaryExpr(field = field, eqOp = eq, value = value)
  }
  def toStructures(str: String): ParseResult[Expr] = parseAll(orStep, str)
}