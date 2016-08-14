lazy val publishSettings = Seq(
  name := "sbt-directive",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  organization := "sbt-directive",
  version := "0.0.1-SNAPSHOT"
)

lazy val commonSettings = Seq(
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")),
    libraryDependencies += 
      "org.scalatest" %% "scalatest" % "3.0.0-M7" % "test"
)

lazy val testSettings = ScriptedPlugin.scriptedSettings ++ Seq(
  scriptedLaunchOpts ++= Seq(
    "-Xmx1024M", 
    "-XX:MaxPermSize=256M", 
    s"-Dplugin.version=${version.value}"),
    scriptedBufferLog := false
)

lazy val cli = (project in file("cli")).settings(
  commonSettings,
  scalaVersion := "2.11.8",
  moduleName := "cli",
  libraryDependencies += "org.scalameta" %% "scalameta" % "1.0.0",
  publishSettings
)

lazy val root = (project in file(".")).settings(
  sbtPlugin := true,
  commonSettings,
  publishSettings,
  scalaVersion := "2.10.6",
  testSettings
).aggregate(cli)
