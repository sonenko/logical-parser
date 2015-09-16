package com.github.sonenko.parser

import org.specs2.matcher.Scope
import org.specs2.mutable._
import scala.util.matching.Regex

class LogicalParserTest extends Specification {

  trait TestScope extends Scope {
    def testRegHate(str: String)(implicit reg: Regex) = reg.findAllIn(str).toList shouldEqual Nil
    def testRegOk(str: String)(implicit reg: Regex) = reg.findAllIn(str).toList shouldEqual List(str)
  }
  
  "fieldNameReg" should {
    "hate incorrect fields" in new TestScope {
      implicit val reg = LogicalParser.fieldNameReg
      testRegHate("")
      testRegHate(" `")
      testRegHate("` `")
      testRegHate("`. `")
      testRegHate("`asd.`")
      testRegHate("`.asd`")
      testRegHate("``")
      testRegHate("` `")
      testRegHate("`.`")
    }
    "satisfy correct field names" in new TestScope {
      implicit val reg = LogicalParser.fieldNameReg
      testRegOk("`a`")
      testRegOk("`_`")
      testRegOk("`_._`")
      testRegOk("`asd.123.___`")
    }
  }
  
  "eqReg" should {
    "hate incorrect values" in new TestScope {
      implicit val reg = LogicalParser.eqReg
      testRegHate("""""")
      testRegHate(""" """)
      testRegHate(""" " """)
      testRegHate(""" asd """)
    }
    "satisfy correct field values" in new TestScope {
      implicit val reg = LogicalParser.eqReg
      testRegOk("""==""")
      testRegOk("""!=""")
      testRegOk(""">""")
      testRegOk(""">=""")
      testRegOk("""<=""")
      testRegOk("""<""")
      testRegOk(""">""")
    }
  }
  
  "toTree" should {
    "work in happy case" in {
      LogicalParser.toTree("").get shouldEqual Empty
      LogicalParser.toTree("""`x` == 20.1""").get shouldEqual Expression("x", Eq, 20.1)
      LogicalParser.toTree("""`x` == "20.0"""").get shouldEqual Expression("x", Eq, "20.0")
      LogicalParser.toTree("""`x` == "" """).get shouldEqual Expression("x", Eq, "")
      LogicalParser.toTree("""`x` == 1 AND `y` != 2 """).get shouldEqual 
        Leaf(
          Expression("x", Eq, 1),
          Expression("y", Ne, 2),
          And
        )
      LogicalParser.toTree("""`x` == 1 AND `y` != 2 """).get shouldEqual
        Leaf(
          Expression("x", Eq, 1),
          Expression("y", Ne, 2),
          And
        )
      LogicalParser.toTree("""`x` != 1 AND `y` < 2 OR `x` <= 2 AND `y` > 3""").get shouldEqual
        Leaf(
          Leaf(
            Expression("x", Ne, 1),
            Expression("y", Lt, 2),
            And
          ),
          Leaf(
            Expression("x", Lte, 2),
            Expression("y", Gt, 3),
            And
          ),
          Or
        )
      // `x` != 1 AND (`y` < 2 OR `x` <= 2) AND `y` > 3
      // same as
      // ((`y` < 2 OR `x` <= 2) AND `x` != 1) AND `y` > 3
      LogicalParser.toTree("""`x` != 1 AND (`y` < 2 OR `x` <= 2) AND `y` > 3.14""").get shouldEqual
        Leaf(
          Leaf(
            Expression("x", Ne, 1), 
            Leaf(
              Expression("y", Lt, 2), 
              Expression("x", Lte, 2), 
              Or
            ), 
            And
          ), 
          Expression("y", Gt, 3.14), And
        )
    }
  }
}
