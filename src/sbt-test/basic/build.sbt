lazy val publishSettings = Seq(
  name := "sandbox",
  scalaVersion := "2.11.8",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  organization := "com.ithaca",
  version := "0.0.1-SNAPSHOT"
)

lazy val commonSettings = Seq(
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= Seq(
    "co.fs2" %% "fs2-core" % "0.9.0-M6"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
)

lazy val root = (project in file("."))
  .settings(sbtPlugin := true)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(directiveSettings:_*)
  .settings(
    preprocessors += directive.Preprocess.lines("stuff") { s"""lines => {
      println("running")
      lines.foreach(println(_))
      List.empty
}
 """
    }
)

