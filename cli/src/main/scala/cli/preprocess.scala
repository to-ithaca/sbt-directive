package cli

import scala.util.Try

import scala.meta._

trait Preprocessor {
  def name: String
  def transform(source: List[String]): List[String]
}

object Preprocessor {
  def lines(n: String)(f: List[String] => List[String]): Preprocessor = new Preprocessor {
    val name: String = n 
    def transform(source: List[String]): List[String] = 
      f(source)
  }

  def ast(n: String)(f: parsers.Parsed[Term] => parsers.Parsed[Term]): Preprocessor = new Preprocessor {
    val name: String = n
    def transform(source: List[String]): List[String] = {
      val r = source.mkString(System.lineSeparator).parse[Term].get.syntax
      r.split(System.lineSeparator).toList
    }
  }

  def seq(ps: Preprocessor*): Map[String, Preprocessor] =
    ps.map(p => p.name -> p).toMap
}

