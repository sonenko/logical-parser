package com.github.sonenko.parser

import org.specs2.matcher.Scope
import org.specs2.mutable._
import scala.util.matching.Regex

class LogicalParserTest extends Specification {

  trait TestScope extends Scope {
    def testRegHate(str: String)(implicit reg: Regex) = reg.findAllIn(str).toList shouldEqual Nil
    def testRegOk(str: String)(implicit reg: Regex) = reg.findAllIn(str).toList shouldEqual List(str)
  }
  
  "booleanReg" should {
    "hate no boolean" in new TestScope {
      implicit val reg = LogicalParser.booleanReg
      testRegHate("")
      testRegHate("True")
    }
    "satisfy booleans" in new TestScope {
      implicit val reg = LogicalParser.booleanReg
      testRegOk("true")
      testRegOk("false")
    }
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
  
  "nullReq" should {
    "find nulls" in new TestScope {
      implicit val reg = LogicalParser.nullReg
      testRegOk("null")
    }
    "hate non nulls" in new TestScope {
      implicit val reg = LogicalParser.nullReg
      testRegHate("")      
      testRegHate("1")      
      testRegHate("==")      
      testRegHate("Null")      
    }
  }
  
  "toTree" should {
    "work in happy case" in {
      LogicalParser.toStructures("").get shouldEqual Empty
      LogicalParser.toStructures("""`true` == false""").get shouldEqual BinaryExpr("true", Eq, false)
      LogicalParser.toStructures("""`x` == 20.1""").get shouldEqual BinaryExpr("x", Eq, 20.1)
      LogicalParser.toStructures("""`x` == "20.0"""").get shouldEqual BinaryExpr("x", Eq, "20.0")
      LogicalParser.toStructures("""`x` == "" """).get shouldEqual BinaryExpr("x", Eq, "")
      LogicalParser.toStructures("""`x` == null """).get shouldEqual BinaryExpr("x", Eq, Null)
      LogicalParser.toStructures("""`x` == 1 AND `y` != 2 """).get shouldEqual 
        CompositeExpr(
          BinaryExpr("x", Eq, 1),
          BinaryExpr("y", Ne, 2),
          And
        )
      LogicalParser.toStructures("""`x` == 1 AND `y` != 2 """).get shouldEqual
        CompositeExpr(
          BinaryExpr("x", Eq, 1),
          BinaryExpr("y", Ne, 2),
          And
        )
      LogicalParser.toStructures("""`x` != 1 AND `y` < 2 OR `x` <= 2 AND `y` > 3""").get shouldEqual
        CompositeExpr(
          CompositeExpr(
            BinaryExpr("x", Ne, 1),
            BinaryExpr("y", Lt, 2),
            And
          ),
          CompositeExpr(
            BinaryExpr("x", Lte, 2),
            BinaryExpr("y", Gt, 3),
            And
          ),
          Or
        )
      // `x` != 1 AND (`y` < 2 OR `x` <= 2) AND `y` > 3
      // same as
      // ((`y` < 2 OR `x` <= 2) AND `x` != 1) AND `y` > 3
      LogicalParser.toStructures("""`x` != 1 AND (`y` < 2 OR `x` <= 2) AND `y` > 3.14""").get shouldEqual
        CompositeExpr(
          CompositeExpr(
            BinaryExpr("x", Ne, 1), 
            CompositeExpr(
              BinaryExpr("y", Lt, 2), 
              BinaryExpr("x", Lte, 2), 
              Or
            ), 
            And
          ), 
          BinaryExpr("y", Gt, 3.14), And
        )
    }
  }
}
