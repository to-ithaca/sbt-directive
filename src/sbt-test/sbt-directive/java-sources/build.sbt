lazy val publishSettings = Seq(
  name := "java-sources",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file(".")).settings(
  publishSettings,
  directiveSettings,
  preprocessors += preprocess.skip("strip")
)
