package directive

import sbt._
import sbt.Keys._

import DirectiveKeys._

object Directive {

  def apply() = Def.task {
    runTask(streams.value.log, dependencyClasspath.value, generate.value)
  }

  def runTask(log: Logger, cp: Classpath, generatedMain: File): Seq[File] = {

    val options = ForkOptions(
      bootJars = cp.files :+ generatedMain,
      outputStrategy = Some(StdoutOutput)
    )

    val run = Fork.scala(options, List("CliGenerated"))

    if(run != 0) sys.error("Failed to preprocess directives")
    Seq.empty
  }
}
