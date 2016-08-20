package directive

import sbt._
import sbt.Keys._
import Def._

import DirectiveKeys._

object Run {

  val scala = Def.task {
    run(streams.value.log, dependencyClasspath.value, generate.value, "*.scala" ,scalaSource.value,
      sourceManaged.value)
  }

  val java = Def.task {
    run(streams.value.log, dependencyClasspath.value, generate.value, "*.java", javaSource.value,
      sourceManaged.value)
  }

  private def run(log: Logger, cp: Classpath, main: File, pattern: String, source: File, dest: File): Seq[File] = {

    log.info(s"Generating processed sources in $dest")

    dest.mkdirs

    val options = ForkOptions(
      bootJars = cp.files :+ main,
      outputStrategy = Some(StdoutOutput)
    )

    val args = List("CliGenerated", 
      source.getAbsolutePath, dest.getAbsolutePath)

    val run = Fork.scala(options, args)
    if(run != 0) sys.error("Failed to preprocess directives")

    Thread.sleep(1000)

    val generated = (dest ** pattern).get
    log.info(s"processed ${generated.size} sources for pattern [ $pattern ]")

    generated
  }
}
