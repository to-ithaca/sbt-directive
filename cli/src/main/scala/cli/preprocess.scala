package cli

import scala.util.Try

trait Preprocessor {
  def transform(source: String): Try[String]
}

object Preprocessor {
  def lines(name: String)(f: List[String] => List[String]): Preprocessor = new Preprocessor {
    def transform(source: String): Try[String] = 
      Try(f(source.split("\n").toList).mkString("\n"))
  }
  def seq(preprocessors: Preprocessor*): Preprocessor =
    preprocessors.head
}

