package directive

import sbt._
import Keys._

import DirectiveKeys._

object DirectivePlugin extends AutoPlugin {

  val DirectiveConfig = config("directive")

  object autoImport {

    val preprocessors = DirectiveKeys.preprocessors

    val directiveSettings = 
      inConfig(DirectiveConfig)(Defaults.compileSettings ++ Seq(
      directive := Directive().value,
      scalaSource := baseDirectory.value / "src" / "main" / "scala",
      generatedSourceDir := target.value / "directive" / "generatedSources"
    )) ++ 
    Seq(
      preprocessors := Seq.empty,
      (sourceGenerators in Compile) <+= (directive in DirectiveConfig),
      (scalaSource in Compile) := (generatedSourceDir in DirectiveConfig).value,
      ivyConfigurations += DirectiveConfig,
      libraryDependencies ++= Seq(
        "sbt-directive" %% "cli" % "0.0.1-SNAPSHOT" % DirectiveConfig,
        "org.scala-lang" % "scala-compiler" % "2.11.8" % DirectiveConfig
      )
    )
  }
}

object DirectiveKeys {
  val generatedSourceDir: SettingKey[File] = settingKey("directory containing generated sources")
  val preprocessors: SettingKey[Seq[DeferredPreprocessor]] = settingKey("preprocessor directives")
  val directive: TaskKey[Seq[File]] = taskKey("Preprocesses directives")
}
