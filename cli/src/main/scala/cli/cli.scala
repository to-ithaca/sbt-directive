package cli


import scala.util.control.NonFatal
import scala.io.Source

import java.io._

object Cli {

  sealed trait ProcessError extends Throwable

  case class UnexpectedDirective(found: String, expected: String, line: Int, file: File) extends ProcessError {
    override def toString: String =
      s"found [$found], expected [ $expected ] on line [ lineNumber ] for file [ $file ]"
  }

  case class UnmatchedDirective(found: String, line: Int, file: File) extends ProcessError {
    override def toString: String =
      s"found unmatched directive [ $found ] at line [ $line ] for file [ $file ]"
  }

  case class UnclosedDirectives(directives: List[String], file: File) extends ProcessError {
    override def toString: String =
      s"found unclosed directives: [ $directives ] for file [ $file ]" 
  }

  case class PreprocessorFailed(directive: String, t: Throwable, line: Int, file: File) extends ProcessError {
    override def toString: String =
      s"preprocessor for directive [$directive] failed due to: [ $t ] on line: [ $line ] for [ $file ]"
  }

  case class UnknownDirective(directive: String, line: Int, file: File) extends ProcessError {
    override def toString: String =
      s"found unknown directive [ $directive ] on line [ $line ] for [ $file ]"
  }

}

object Attempt {

  def success[A](a: A): Attempt[A] = Right(a)
  def fail[A](e: Throwable): Attempt[A] = Left(e)

  def apply[A](a: => A): Attempt[A] = try {
    success(a)
  } catch {
    case NonFatal(e) => fail(e)
  }
}


final class Cli(preprocessors: Map[String, Preprocessor]) {

  def run(srcDir: File, targetDir: File): Unit = {
    println(s"running cli: ${srcDir.getAbsolutePath}")
    walk(srcDir, targetDir)
  }


  private def processBatch(
    preprocessor: Preprocessor,
    batches: List[List[String]]): Attempt[List[List[String]]] =
    batches match {
      case batch :: prevBatch :: tail =>
        Attempt(preprocessor.transform(batch.reverse).reverse).right.map( r => (r ::: prevBatch) :: tail )
      case batch :: Nil =>
        Attempt(preprocessor.transform(batch.reverse).reverse).right.map(List(_))
      case Nil => Attempt.success(batches)
    }

  def process(file: File, lines: List[String]): Attempt[List[String]] = {

    @annotation.tailrec
    def go(
      lines: List[(String, Int)],
      directives: List[String],
      batches: List[List[String]]): Attempt[List[String]] =
      lines match {
        // No more lines, done!
        case Nil => 
          if (directives.isEmpty)
            Attempt.success(batches.reverse.flatten.reverse)
          else
            Attempt.fail(Cli.UnclosedDirectives(directives, file))
        // Push a token.
        case (s, p) :: ss if s.startsWith("#+") =>
          val tok = s.drop(2).trim
          if(preprocessors.get(tok).isEmpty)
            Attempt.fail(Cli.UnknownDirective(tok, p + 1, file))
          else
          go(ss,  tok :: directives, Nil :: batches)
        // Pop a token.
        case (s, p) :: ss if s.startsWith("#-") => 
          val tok  = s.drop(2).trim
          directives.headOption match {
            case Some(expected) =>
              if(expected == tok)
                processBatch(preprocessors(tok), batches) match {
                  case Left(err) => Attempt.fail(Cli.PreprocessorFailed(tok, err, p + 1, file))
                  case Right(next) => go(ss, directives.tail, next)  
                }
              else
                Attempt.fail(Cli.UnexpectedDirective(tok, expected, p + 1, file))
            case None =>
              Attempt.fail(Cli.UnmatchedDirective(tok, p + 1, file))
          }
        case (s, _) :: ss => go(ss, directives, (s :: batches.head) :: batches.tail)
      }
    go(lines.zipWithIndex, Nil, List(Nil))
  }

  def walk(src: File, destDir: File): List[File] =
    if (src.isFile) {
      if (src.isHidden) Nil
      else {
        println(s"walking with source $src dest $destDir")
        val f = new File(destDir, src.getName)
        val s = Source.fromFile(src, "UTF-8")
        try {
          destDir.mkdirs()
          val pw = new PrintWriter(f, "UTF-8")
          try {
            process(src, s.getLines.toList) match {
              case Left(err) => sys.error(err.toString)
              case Right(lines) => lines.foreach(pw.println)  
            }
          } finally {
            pw.close()
          }
        } finally {
          s.close()
        }
        List(f)
      }
    } else {
      try {
        src.listFiles.toList.flatMap(f => walk(f, new File(destDir, src.getName)))
      } catch {
        case n: NullPointerException => Nil
      }
    }
}
