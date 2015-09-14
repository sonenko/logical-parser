package project

import sbt._
import sbt.Keys._

object ProjectBuild extends Build {
  lazy val projectBuild = project.in(file(".")).settings(List(
    addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3"),
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3"),
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  ): _*)
}