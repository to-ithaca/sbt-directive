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
      scalaSource := baseDirectory.value / "src" / "main" / "scala"
    )) ++ 
    Seq(
      preprocessors := Nil,
      (sourceGenerators in Compile) <+= (directive in DirectiveConfig),
      (scalaSource in Compile) := target.value / "directive" / "output",
      ivyConfigurations += DirectiveConfig,
      libraryDependencies ++= Seq(
        "sbt-directive" %% "cli" % "0.0.1-SNAPSHOT" % DirectiveConfig,
        "org.scala-lang" % "scala-compiler" % "2.11.8" % DirectiveConfig
      )
    )
  }
}

object DirectiveKeys {
  val preprocessors: SettingKey[List[DeferredPreprocessor]] = settingKey("preprocessor directives")
  val directive: TaskKey[Seq[File]] = taskKey("Preprocesses directives")
}
