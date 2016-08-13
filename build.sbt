lazy val publishSettings = Seq(
  name := "sbt-directive",
  scalaVersion := "2.10.6",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  organization := "com.ithaca",
  version := "0.0.1-SNAPSHOT"
)

lazy val commonSettings = Seq(
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val root = (project in file("."))
  .settings(sbtPlugin := true)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)

