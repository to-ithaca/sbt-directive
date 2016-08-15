lazy val publishSettings = Seq(
  name := "identity",
  scalaVersion := "2.11.8",
  organization := "com.ithaca",
  version := "0.0.1-SNAPSHOT"
)

lazy val root = (project in file("."))
  .settings(publishSettings: _*)
  .settings(directiveSettings: _*)
  .settings(
    preprocessors += preprocess.identity("identity")
)
