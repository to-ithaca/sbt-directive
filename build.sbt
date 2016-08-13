lazy val publishSettings = Seq(
  name := "sbt-directive",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  organization := "sbt-directive",
  version := "0.0.1-SNAPSHOT"
)

lazy val commonSettings = Seq(
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val cli = (project in file("cli"))
  .settings(commonSettings)
.settings(Seq(
  scalaVersion := "2.11.8",
  moduleName := "cli",
  libraryDependencies += "org.scalameta" %% "scalameta" % "1.0.0"
)).settings(publishSettings)

lazy val root = (project in file("."))
  .settings(sbtPlugin := true)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(Seq(
    scalaVersion := "2.10.6"
  ))

