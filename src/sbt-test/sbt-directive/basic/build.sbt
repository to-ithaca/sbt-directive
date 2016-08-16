lazy val publishSettings = Seq(
  name := "basic",
  scalaVersion := "2.11.8",
  organization := "sbt-directive",
  version := "0.0.1-SNAPSHOT"
)

lazy val root = (project in file(".")).settings(
  publishSettings,
  directiveSettings,
  preprocessors += preprocess.skip("strip")
)
