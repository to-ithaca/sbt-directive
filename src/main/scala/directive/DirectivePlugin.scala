package directive

import sbt._
import Keys._

import DirectiveKeys._

object DirectivePlugin extends AutoPlugin {

  val DirectiveConfig = config("directive")

  object autoImport {

    val preprocessors = DirectiveKeys.preprocessors
    val preprocess = Preprocess

    val directiveSettings = 
      inConfig(DirectiveConfig)(Defaults.compileSettings ++ Seq(
      generate := Generate.task.value,
      directiveScala := Directive.scala.value,
      directiveJava := Directive.java.value,
      scalaSource := baseDirectory.value / "src" / "main" / "scala",
      javaSource := baseDirectory.value / "src" / "main" / "java",
      generatedSourceDir := target.value / "directive" / "generatedSources"
    )) ++ 
    Seq(
      preprocessors := Seq.empty,
      (sourceGenerators in Compile) <+= (directiveScala in DirectiveConfig),
      (sourceGenerators in Compile) <+= (directiveJava in DirectiveConfig),
      (scalaSource in Compile) := target.value / "directive" / "empty",
      (javaSource in Compile) := target.value / "directive" / "empty",
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
  val generate: TaskKey[File] = taskKey("generates and compiles a synthetic main")
  val directiveScala: TaskKey[Seq[File]] = taskKey("Runs preprocessors on scala sources")
  val directiveJava: TaskKey[Seq[File]] = taskKey("Runs preprocessors on java sources")
}
