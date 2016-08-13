package cli

import scala.io.Source

import java.io._

final class Cli(preprocessors: Map[String, Preprocessor]) {

  def run(srcDir: File, targetDir: File): Unit = {
    println("running cli")
    walk(srcDir, targetDir)
  }

  private def process(file: File, lines: List[String]): List[String] = {
    def go(lines: List[(String, Int)], out: List[String], stack: List[String], acc: List[List[String]]): List[String] =
      lines match {

        // No more lines, done!
        case Nil => 
          if (stack.isEmpty) out.reverse
          else sys.error(s"$file: EOF: expected ${stack.map(s => s"#-$s").mkString(", ")}")

        // Push a token.
        case (s, _) :: ss if s.startsWith("#+") =>
          go(ss, out, s.drop(2).trim :: stack, acc)

        // Pop a token.
        case (s, n) :: ss if s.startsWith("#-") => 
          val tok  = s.drop(2).trim
          val line = n + 1
          stack match {
            case `tok` :: ts => 
              val lines = acc.head.reverse
              preprocessors.get(tok) match {
                case None => sys.error(s"$file: $line: unknown token $tok")
                case Some(preprocessor) =>
                  val result = preprocessor.transform(lines)
                  val nextAcc = (result ::: acc.tail.head) :: acc.tail.tail
                  go(ss, out, ts, nextAcc)
              }

            case t :: _      => sys.error(s"$file: $line: expected #-$t, found #-$tok")
            case _           => sys.error(s"$file: $line: unexpected #-$tok")
          }

        // Add a line, or not, depending on tokens.
        case (s, _) :: ss => 
          go(ss, s :: out, stack, (s :: acc.head) :: acc.tail)
      }
    go(lines.zipWithIndex, Nil, Nil, List(Nil))
  }

  def walk(src: File, destDir: File): List[File] =
    if (src.isFile) {
      if (src.isHidden) Nil
      else {
        val f = new File(destDir, src.getName)
        val s = Source.fromFile(src, "UTF-8")
        try {
          destDir.mkdirs()
          val pw = new PrintWriter(f, "UTF-8")
          try {
            process(src, s.getLines.toList).foreach(pw.println)
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

  // all non-hidden files
  private def closure(src: File): List[File] =
    if (src.isFile) {
      if (src.isHidden) Nil else List(src)
    } else {
      src.listFiles.toList.flatMap(closure)
}
}
