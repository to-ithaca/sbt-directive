lazy val publishSettings = Seq(
  name := "basic",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file(".")).settings(
  publishSettings,
  directiveSettings,
  preprocessors += preprocess.skip("strip")
)
