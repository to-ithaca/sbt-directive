package cli

import java.util.concurrent.Executors

import scala.util.control.NonFatal
import scala.collection.immutable.Queue
import scala.io.Source
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
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

  import scala.concurrent.ExecutionContext.Implicits.global

  def run(srcDir: File, targetDir: File): Unit = {
    val r: Future[List[Unit]] = Future.sequence(pairs(srcDir, targetDir).map {
      case (src, dest) => 
        Future(process(src, dest)).flatMap {
        case Right(_) => Future.successful(())
        case Left(err) => Future.failed(err)
      }
    })
    Await.result(r, 5 minutes)
  }

  private def pairs(src: File, dest: File): List[(File, File)] =
    if (src.isDirectory) {
      dest.mkdir()
      val children = src.listFiles.toList
      children.flatMap( f => pairs(f, new File(dest, f.getName)))
    } else if (src.isFile) {
      List(src -> dest)
    } else Nil

  private def process(src: File, dest: File): Attempt[Unit] = {
    val source = Source.fromFile(src, "UTF-8")
    val pw = new PrintWriter(dest, "UTF-8")

    val result = process(src, source.getLines.toList).right.map(destLines =>
      destLines.foreach(pw.println))

    pw.close()
    source.close()

    result
  }

  def process(file: File, lines: List[String]): Attempt[List[String]] = {

    @annotation.tailrec
    def go(
      lines: List[(String, Int)],
      directives: List[String],
      batches: List[Queue[String]]): Attempt[List[String]] =
      lines match {

        // No more lines, done!
        case Nil => 
          if (directives.isEmpty)
            Attempt.success(batches.reverse.flatMap(_.toList))
          else
            Attempt.fail(Cli.UnclosedDirectives(directives, file))

        // Push a token.
        case (s, p) :: tail if s.startsWith("#+") =>
          val d = directive(s)
          if(!preprocessors.contains(d))
            Attempt.fail(Cli.UnknownDirective(d, p + 1, file))
          else
            go(tail,  d :: directives, Queue.empty :: batches)

        // Pop a token.
        case (s, p) :: tail if s.startsWith("#-") => 
          val d  = directive(s)
          directives.headOption match {
            case Some(`d`) =>
              processBatch(preprocessors(d), batches) match {
                case Left(err) => Attempt.fail(Cli.PreprocessorFailed(d, err, p + 1, file))
                case Right(next) => go(tail, directives.tail, next)
              }
            case Some(expected) => 
              Attempt.fail(Cli.UnexpectedDirective(d, expected, p + 1, file))
            case None =>
              Attempt.fail(Cli.UnmatchedDirective(d, p + 1, file))
          }

        case (s, _) :: tail => go(tail, directives, (batches.head.enqueue(s)) :: batches.tail)
      }

    go(lines.zipWithIndex, Nil, List(Queue.empty))
  }

  private def directive(s: String): String = s.drop(2).trim

  private def processBatch(
    p: Preprocessor,
    batches: List[Queue[String]]): Attempt[List[Queue[String]]] =
    batches match {
      case cur :: prev :: tail =>
        Attempt(p.transform(cur.toList)).right.map(r => 
          prev.enqueue(r) :: tail)
      case cur :: Nil =>
        Attempt(p.transform(cur.toList)).right.map(r =>
          List(Queue(r:_*)))
      case Nil => Attempt.success(batches)
    }
}
