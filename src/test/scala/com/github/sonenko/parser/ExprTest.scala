package com.github.sonenko.parser

import org.specs2.mutable.Specification

import org.specs2.matcher.Scope
import org.specs2.mutable._
import scala.util.matching.Regex

class ExprTest extends Specification {
  
  
  "map" should {
    "do nothing on Empty" in {
      Empty.map(x => x) shouldEqual Empty
    }
    "change nothing for identity" in {
      val orig = BinaryExpr("field", Eq, 2)
      orig.map(identity) shouldEqual orig
    }
    "change expression" in {
      val orig = BinaryExpr("field", Eq, 2)
      val newLeaf = orig.copy(field = "field2")
      orig.map(x => x.copy(field = "field2")) shouldEqual newLeaf
    }
    "change expressions in branch" in {
      val leaf1 = BinaryExpr("field1", Eq, 1)
      val newLeaf1 = leaf1.copy(field = "f1", value = 10)
      val leaf2 = BinaryExpr("field2", Eq, 2)
      val newLeaf2 = leaf1.copy(field = "f2", value = 20)
      val branch = CompositeExpr(leaf1, leaf2, And)
      val expected = CompositeExpr(newLeaf1, newLeaf2, And)
      val actual = branch.map(x => {
        if (x.field == "field1") newLeaf1
        else newLeaf2
      })
      actual mustEqual expected
    }
  }
  "filter" should {
    "do nothing with Empty" in {
      Empty.filter(_ => true) shouldEqual Empty
    }
    "return Empty on Leaf if expression is false" in {
      BinaryExpr("f", Eq, "v").filter(_ => false) shouldEqual Empty
    }
    "return same leaf on leaf if expression is true" in {
      val leaf = BinaryExpr("f", Eq, "v")
      leaf.filter(_ => true) shouldEqual leaf
    }
    "return Empty on branch if expression is false for all" in {
      val leaf1 = BinaryExpr("field1", Eq, 1)
      val newLeaf1 = leaf1.copy(field = "f1", value = 10)
      val leaf2 = BinaryExpr("field2", Eq, 2)
      val newLeaf2 = leaf1.copy(field = "f2", value = 20)
      val branch = CompositeExpr(leaf1, leaf2, And)
      branch.filter(_ => false) shouldEqual Empty
    }
    "return same branch on branch if expression is false for all" in {
      val leaf1 = BinaryExpr("field1", Eq, 1)
      val newLeaf1 = leaf1.copy(field = "f1", value = 10)
      val leaf2 = BinaryExpr("field2", Eq, 2)
      val newLeaf2 = leaf1.copy(field = "f2", value = 20)
      val branch = CompositeExpr(leaf1, leaf2, And)
      branch.filter(_ => true) shouldEqual branch
    }
    "return leaf if one of branch leafs is true" in {
      val leaf1 = BinaryExpr("field1", Eq, 1)
      val newLeaf1 = leaf1.copy(field = "f1", value = 10)
      val leaf2 = BinaryExpr("field2", Eq, 2)
      val newLeaf2 = leaf1.copy(field = "f2", value = 20)
      val branch = CompositeExpr(leaf1, leaf2, And)
      branch.filter(_.field == "field1") shouldEqual leaf1
    }
  }
  
  "length" should {
    "return number of BinaryExpr" in {
      BinaryExpr("field1", Eq, 1).length shouldEqual 1
      Empty.length shouldEqual 0
      CompositeExpr(Empty, Empty, And).length shouldEqual 0
      CompositeExpr(BinaryExpr("1", Eq, 1), Empty, And).length shouldEqual 1
      CompositeExpr(BinaryExpr("1", Eq, 1), BinaryExpr("1", Eq, 1), And).length shouldEqual 2
      CompositeExpr(CompositeExpr(BinaryExpr("1", Eq, 1), BinaryExpr("1", Eq, 1), And), BinaryExpr("1", Eq, 1), And).length shouldEqual 3
    }
  }
}
