import sbt._

object Dependencies {
  object tests {
    def lib(name: String) = "org.specs2" %% s"specs2-$name" % "2.4.15" % "test"
    val mockito = lib("mock")
    val specs2 = lib("junit")
  }

  val scalaParserCombinators = "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4"
}