package cli

import scala.io.Source

import java.io._

final class Cli(preprocessors: Map[String, Preprocessor]) {

  def run(srcDir: File, targetDir: File): Unit = {
    println("running cli")
    walk(srcDir, targetDir)
  }

  def process(file: String, lines: List[String]): List[String] = {

    @annotation.tailrec
    def go(lines: List[(String, Int)], directives: List[String], 
      batches: List[List[String]]): List[String] =
      lines match {
        // No more lines, done!
        case Nil => 
          if (directives.isEmpty) batches.reverse.flatten.reverse
          else sys.error(s"$file: EOF: expected ${directives.map(s => s"#-$s").mkString(", ")}")

        // Push a token.
        case (s, _) :: ss if s.startsWith("#+") =>
          go(ss,  s.drop(2).trim :: directives, Nil :: batches)

        // Pop a token.
        case (s, n) :: ss if s.startsWith("#-") => 
          val tok  = s.drop(2).trim
          val lineNumber = n + 1
          directives match {
            case `tok` :: ts => 
              preprocessors.get(tok) match {
                case None => sys.error(s"$file: $lineNumber: no preprocessor for token $tok")
                case Some(preprocessor) =>
                  batches match {
                    case batch :: prevBatch :: tail => 
                      val result = preprocessor.transform(batch.reverse).reverse
                      go(ss, ts, (result ::: prevBatch) :: tail)
                   case batch :: Nil => 
                      val result = preprocessor.transform(batch.reverse).reverse
                      go(ss, ts, result :: Nil)
                    case Nil =>
                      go(ss, ts, batches)
                  }

              }

            case t :: _      => sys.error(s"$file: $lineNumber: expected #-$t, found #-$tok")
            case _           => sys.error(s"$file: $lineNumber: unexpected #-$tok")
          }

        // Add a line, or not, depending on tokens.
        case (s, _) :: ss => go(ss, directives, (s :: batches.head) :: batches.tail)
      }
    go(lines.zipWithIndex, Nil, List(Nil))
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
            process(src.getAbsolutePath, s.getLines.toList).foreach(pw.println)
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
