import sbt._, Keys._
import Dependencies._
import sbtrelease.ReleasePlugin._

object Build extends Build {

  lazy val publishSettings = publishTo <<= version.apply {
    v =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("staging" at nexus + "service/local/staging/deploy/maven2")
  }

  lazy val mSettings = Seq(
    publishSettings,
    pomExtra := (
      <scm>
        <url>git@github.com:sonenko/logical-parser.git</url>
        <connection>scm:git:git@github.com:sonenko/logical-parser.git</connection>
      </scm>
        <developers>
          <developer>
            <id>sonenko</id>
            <name>Onenko Sergiy</name>
            <email>growler.ua@gmail.com</email>
          </developer>
        </developers>),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false }
  )

  val commonSettings = Seq(
    organization := "com.github.sonenko",
    description := "expression parser that can convert sql-like expressions to scala case classes",
    updateOptions := updateOptions.value withCachedResolution true,
    scalaVersion := "2.11.7",
    licenses := Seq(("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
    homepage := Some(new URL("https://github.com/sonenko/logical-parser")),
    startYear := Some(2015),
    cancelable in Global := true,
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-Xlog-reflective-calls", "-Xfuture", "-Xlint"),
    testOptions in Test := Seq(Tests.Filter(x => x.endsWith("Test"))),
    parallelExecution in Test := true
  )

  lazy val parser = (project in file("."))
    .settings(libraryDependencies ++= Seq(
      scalaParserCombinators, tests.specs2, tests.mockito
    ))
    .settings(commonSettings ++ releaseSettings ++ mSettings:_*)
}