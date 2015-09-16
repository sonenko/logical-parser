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

sealed trait Tree
case object Empty extends Tree
case class Expression[T](field: String, eqOp: EqOp, value: T) extends Tree
case class Leaf(left: Tree, right: Tree, andOr: AndOr) extends Tree

object LogicalParser extends RegexParsers with JavaTokenParsers{
  
  def fieldNameReg: Regex = """`\w+(?:\.\w+)?(?:\.\w+)?(?:\.\w+)?(?:\.\w+)?`""".r
  def fieldName: Parser[String] = fieldNameReg ^^ {x => x.slice(1, x.length - 1)}
  def stringValue = stringLiteral ^^ {x => x.slice(1, x.length - 1)}
  def doubleValue: Parser[Double] = """(\d+\.\d+)""".r ^^ {_.toDouble}
  def longValue: Parser[Long] = wholeNumber ^^ {_.toLong}
  def eqReg = """(==)|(!=)|(<=)|(>=)|(<)|(>)""".r
  def eq: Parser[EqOp] = eqReg ^^ EqOp.withName
  def or : Parser[AndOr]= """(?i)(OR)""".r ^^ (x => Or)
  def and : Parser[AndOr]= """(?i)(AND)""".r ^^ (x => And)
  
  def orStep: Parser[Tree] = andStep ~ rep(or ~ andStep) ^^ {
    case f1 ~ fs => (f1 /: fs){
      case (acc, x) => Leaf(acc, x._2, x._1)
    }
  }

  def andStep: Parser[Tree] = bracketsStep ~ rep(and ~ bracketsStep) ^^ {
    case f1 ~ fs => (f1 /: fs){
      case (acc, x) => Leaf(acc, x._2, x._1)
    }
  }

  def bracketsStep: Parser[Tree] = expStep | "(" ~> orStep <~ ")" | ("^$".r ^^ (_ => Empty))
  
  def expStep = (fieldName ~ eq ~ (stringValue | doubleValue | longValue)) ^^ {
    case field ~ eq ~ value => Expression(field = field, eqOp = eq, value = value)
  }
  def toTree(str: String): ParseResult[Tree] = parseAll(orStep, str)
}