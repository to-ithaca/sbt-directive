package directive

import sbt._
import sbt.Keys._

object Directive {

  val main = s"""
object TestApp extends App {
  println("Hello!")
}
"""

  def runTask(log: Logger, srcDir: File, cp: Classpath, genDir: File, javaHome: Option[File]): Seq[File] = {
    val parentFile = genDir / "directive"
    val srcFile = genDir / "directive" / "src" / "DirectiveMain.scala"
    val destDir = genDir / "directive" / "classes"
 
    IO.write(srcFile, main)
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
      "TestApp",
      srcDir.absolutePath
    ))

    Seq.empty
  }


  def apply(): Def.Initialize[Task[Seq[File]]] = Def.task(
    runTask(streams.value.log, sourceDirectory.value, dependencyClasspath.value, 
      target.value, javaHome.value)
  )
}

//java scala.tools.nsc.MainGenericRunner //
