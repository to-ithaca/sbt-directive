package directive

import sbt._
import sbt.Keys._

import DirectiveKeys._

object Directive {

  val scala = Def.task {
    run(streams.value.log, dependencyClasspath.value, generate.value, scalaSource.value, generatedSourceDir.value)
  }

  val java = Def.task {
    run(streams.value.log, dependencyClasspath.value, generate.value, javaSource.value, generatedSourceDir.value)
  }

  private def run(log: Logger, cp: Classpath, generatedMain: File, sourceDir: File, targetDir: File): Seq[File] =
    walk(generatedMain, cp.files, sourceDir, targetDir) match {
      case Right(files) => files.toSeq
      case Left(e) => sys.error(e.toString)
    }

  private def walk(main: File, cp: Seq[File], src: File, dest: File): Either[Throwable, List[File]] =
    if (src.isDirectory) {
      dest.mkdir()
      val children = src.listFiles.toList
      children.foldLeft(success(List.empty[File])) { (prev, f) =>
        prev.right.flatMap { fs =>
          walk(main, cp, f, new File(dest, f.getName)).right.map(_ ::: fs)
        }
      }
    } else if (src.isFile) {
      preprocess(main, cp, src, dest)
    } else if (!src.exists) {
      success(Nil)
    } else fail(UnexpectedFileType(src))

  private def preprocess(main: File, cp: Seq[File], src: File, dest: File): Either[Throwable, List[File]] = {

    val options = ForkOptions(
      bootJars = cp :+ main,
      outputStrategy = Some(StdoutOutput)
    )

    val args = List(
      "CliGenerated",
      src.getAbsolutePath,
      dest.getAbsolutePath
    )

    val result = Fork.scala(options, args)
    if(result != 0) fail(PreprocessFailed(src))
    else success(List(dest))
  }

  private def success[A](a: A): Either[Throwable, A] = Right(a)
  private def fail[A](e: Throwable): Either[Throwable, A] = Left(e)

  case class UnexpectedFileType(f: File) extends Throwable {
    override def toString: String =
      s"found unexpected file [ $f ], expected file or directory"
  }

  case class PreprocessFailed(f: File) extends Throwable {
    override def toString: String =
      s"failed to process file [ $f ]"
  }
}
