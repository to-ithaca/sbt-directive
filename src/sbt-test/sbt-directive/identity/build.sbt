lazy val publishSettings = Seq(
  name := "identity",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file(".")).settings(
  publishSettings,
  directiveSettings,
  preprocessors += preprocess.identity("identity")
)
