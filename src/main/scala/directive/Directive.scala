package directive

import sbt._
import sbt.Keys._

object Directive {

  def main(src: File, target: File, preprocessors: List[DeferredPreprocessor]) = s"""
object CliGenerated {

  def main(args: Array[String]): Unit = { 
   val sourceDir = new java.io.File("${src.getAbsolutePath}")
   val targetDir = new java.io.File("${target.getAbsolutePath}")
   val preprocessors = _root_.cli.Preprocessor.seq(${preprocessors.map(_.source).mkString(",")})
   new _root_.cli.Cli(preprocessors).run(sourceDir, targetDir)
 }
}
"""

  def runTask(log: Logger, srcDir: File, cp: Classpath, 
    genDir: File, javaHome: Option[File], preprocessors: List[DeferredPreprocessor], targetDir: File): Seq[File] = {
    val parentFile = genDir / "directive"
    val srcFile = genDir / "directive" / "src" / "DirectiveMain.scala"
    val destDir = genDir / "directive" / "classes"
 
    IO.write(srcFile, main(srcDir, targetDir, preprocessors))
    IO.createDirectory(destDir)
    
    val options = ForkOptions(
      bootJars = cp.files,
      outputStrategy = Some(StdoutOutput),
      javaHome = javaHome
    )
    val result = Fork.scalac(options, List(
      "-d", destDir.getAbsolutePath,
      srcFile.getAbsolutePath
    ))

    log.info(s"source directory is ${srcDir}")
    log.info(s"result is $result")
    log.info(s"parsing directives in $srcDir")
    cp.files.foreach { f =>
      log.info(s"classpath ${f.absolutePath}")
    }

    val runOptions = ForkOptions(
      bootJars = cp.files :+ destDir,
      outputStrategy = Some(StdoutOutput),
      javaHome = javaHome
    )

    val run = Fork.scala(runOptions, List(
      "CliGenerated",
      srcDir.absolutePath
    ))
    Seq.empty
  }

  import DirectiveKeys._

  def apply(): Def.Initialize[Task[Seq[File]]] = Def.task(
    runTask(streams.value.log, scalaSource.value, dependencyClasspath.value, 
      target.value, javaHome.value, preprocessors.value.toList, generatedSourceDir.value)
  )
}

//java scala.tools.nsc.MainGenericRunner //
