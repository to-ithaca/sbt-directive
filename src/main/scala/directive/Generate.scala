package directive

import sbt._
import sbt.Keys._

import DirectiveKeys._

object Generate {

  def apply() = Def.task {
    runTask(streams.value.log, 
      dependencyClasspath.value,
      preprocessors.value.toList,
      target.value,
      scalaSource.value,
      generatedSourceDir.value
    )
  }

  private def runTask(log: Logger, 
    cp: Classpath,
    preprocessors: List[DeferredPreprocessor],
    target: File,
    scalaSrcDir: File,
    scalaTargetDir: File): File = {

    val srcFile = target / "directive" / "src" / "CliGenerated.scala"
    val destDir = target / "directive" / "classes"
 
    IO.write(srcFile, src(scalaSrcDir, scalaTargetDir, preprocessors))

    IO.createDirectory(destDir)
    
    val options = ForkOptions(
      bootJars = cp.files,
      outputStrategy = Some(StdoutOutput)
    )

    val result = Fork.scalac(options, List(
      "-d", destDir.getAbsolutePath,
      srcFile.getAbsolutePath
    ))

    if(result != 0)
      sys.error("Failed to compile preprocessors")
    else destDir
  }

  private def src(scalaSrcDir: File, scalaTargetDir: File, preprocessors: List[DeferredPreprocessor]): String = 
s"""
object CliGenerated {

  def main(args: Array[String]): Unit = { 
   val src = new java.io.File("${scalaSrcDir.getAbsolutePath}")
   val dest = new java.io.File("${scalaTargetDir.getAbsolutePath}")
   val preprocessors = _root_.cli.Preprocessor.seq(${preprocessors.map(_.source).mkString(",")})
   new _root_.cli.Cli(preprocessors).run(src, dest)
 }
}
"""
}
