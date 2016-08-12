package directive

import sbt._
import Keys._

object DirectivePlugin extends AutoPlugin {
  object autoImport {
    val directiveSettings = Seq( 
      DirectiveKeys.directive := Directive().value,
      (sourceGenerators in Compile) <+= DirectiveKeys.directive,
      libraryDependencies ++= Seq(
        "org.scalameta" %% "scalameta" % "1.0.0"
      )
    )
  }
}

object DirectiveKeys {
  val directive: TaskKey[Seq[File]] = taskKey("Preprocesses directives")
}
