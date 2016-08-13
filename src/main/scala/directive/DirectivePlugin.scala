package directive

import sbt._
import Keys._

import DirectiveKeys._

object DirectivePlugin extends AutoPlugin {

  val DirectiveConfig = config("directive")

  object autoImport {
    val directiveSettings = inConfig(DirectiveConfig)(Defaults.compileSettings ++ Seq(
      directive := Directive().value
    )) ++ 
    Seq(
      (sourceGenerators in Compile) <+= (directive in DirectiveConfig),
      ivyConfigurations += DirectiveConfig,
      libraryDependencies ++= Seq(
        "org.scalameta" %% "scalameta" % "1.0.0" % DirectiveConfig,
        "org.scala-lang" % "scala-compiler" % "2.11.8" % DirectiveConfig
      )
    )
  }
}

object DirectiveKeys {
  val directive: TaskKey[Seq[File]] = taskKey("Preprocesses directives")
}
