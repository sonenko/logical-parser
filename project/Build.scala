import sbt._, Keys._
import Dependencies._

object Build extends Build {

  val publishSettings = Seq(
    publishMavenStyle := true,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := true,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )

  val commonSettings = Seq(
    organization := "com.github.sonenko",
    description := "",
    updateOptions := updateOptions.value withCachedResolution true,
    scalaVersion := "2.11.7",
    cancelable in Global := true,
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-Xlog-reflective-calls", "-Xfuture", "-Xlint"),
    testOptions in Test := Seq(Tests.Filter(x => x.endsWith("Test"))),
    parallelExecution in Test := false
  )

  lazy val scratch = (project in file("."))
    .settings(libraryDependencies ++= Seq(
      log.logback, log.scalaloggingSlf4j, log.jclOverSlf4j, log.julToSlf4j, log.log4jOverSlf4j, log.slf4jApi,
      typesafe.config, scalaParserCombinators,
      tests.specs2, tests.mockito
    ))
    .settings(commonSettings:_*)
}