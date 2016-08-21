package directive

import sbt._
import Keys._

import DirectiveKeys._

object DirectivePlugin extends AutoPlugin {

  object autoImport {

    val Directive = config("directive")

    val preprocessors = DirectiveKeys.preprocessors
    val preprocess = Preprocess

    val directiveSettings = 
      inConfig(Directive)(Defaults.compileSettings ++ Seq(
      generate := Generate.task.value,
      directiveScala := Run.scala.value,
      directiveJava := Run.java.value,
      scalaSource := baseDirectory.value / "src" / "main" / "scala",
      javaSource := baseDirectory.value / "src" / "main" / "java"
    )) ++ 
    Seq(
      preprocessors := Seq.empty,
      (sourceGenerators in Compile) <+= (directiveScala in Directive),
      (sourceGenerators in Compile) <+= (directiveJava in Directive),
      (scalaSource in Compile) := target.value / "directive" / "empty",
      (javaSource in Compile) := target.value / "directive" / "empty",
      (sourceDirectories in Compile) += (sourceManaged in Directive).value,
      ivyConfigurations += Directive,
      libraryDependencies ++= Seq(
        "org.directive" %% "cli" % "0.2.0-SNAPSHOT" % Directive,
        "org.scala-lang" % "scala-compiler" % "2.11.8" % Directive
      )
    )
  }
}

object DirectiveKeys {

  val preprocessors: SettingKey[Seq[DeferredPreprocessor]] = settingKey("preprocessor directives")
  val generate: TaskKey[File] = taskKey("generates and compiles a synthetic main")
  val directiveScala: TaskKey[Seq[File]] = taskKey("Runs preprocessors on scala sources")
  val directiveJava: TaskKey[Seq[File]] = taskKey("Runs preprocessors on java sources")
}
