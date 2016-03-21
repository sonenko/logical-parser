package project

import sbt._
import sbt.Keys._

object ProjectBuild extends Build {
  lazy val projectBuild = project.in(file(".")).settings(List(
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0"),
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.2")
  ): _*)
}