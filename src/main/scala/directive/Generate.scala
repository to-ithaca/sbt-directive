package directive

import sbt._
import sbt.Keys._

import DirectiveKeys._

object Generate {

  val task = Def.task {
    run(streams.value.log, 
      dependencyClasspath.value,
      preprocessors.value.toList,
      target.value
    )
  }

  private def run(log: Logger, 
    cp: Classpath,
    preprocessors: List[DeferredPreprocessor],
    target: File): File = {

    val srcFile = target / "directive" / "src" / "CliGenerated.scala"
    val destDir = target / "directive" / "classes"
 
    IO.write(srcFile, src(preprocessors))

    IO.createDirectory(destDir)
    
    val options = ForkOptions(
      bootJars = cp.files,
      outputStrategy = Some(StdoutOutput)
    )

    val args = List(
      "-d", destDir.getAbsolutePath,
      srcFile.getAbsolutePath
    )

    val result = Fork.scalac(options, args)
    if(result != 0)
      sys.error("Failed to compile preprocessors")
    else destDir
  }

  private def src(preprocessors: List[DeferredPreprocessor]): String = 
s"""
object CliGenerated {

  def main(args: Array[String]): Unit = { 
   val src = new java.io.File(args(0))
   val dest = new java.io.File(args(1))
   val preprocessors = _root_.cli.Preprocessor.seq(${preprocessors.map(_.source).mkString(",")})
   new _root_.cli.Cli(preprocessors).run(src, dest)
 }
}
"""
}
