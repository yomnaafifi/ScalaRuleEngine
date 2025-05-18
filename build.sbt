ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaRuleEngine",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.7.1"
  )
