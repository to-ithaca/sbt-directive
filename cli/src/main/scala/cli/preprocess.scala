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

  def statement(n: String)(f: Stat => Stat): Preprocessor = new Preprocessor {
    val name: String = n
    def transform(source: List[String]): List[String] = {
      println(s"source is ${source}")
      val (res, rem) = source.filter(_.nonEmpty).foldLeft(List.empty[Stat] -> List.empty[String]) {
        case ((acc, stack), line) =>
          val fragment = (line :: stack).reverse.mkString(" ")
          fragment.parse[Stat] match {
            case Parsed.Success(statement) =>
              (f(statement) :: acc) -> List.empty
            case Parsed.Error(_) =>
              acc -> (line :: stack)
          }
      }
      
      require(rem.isEmpty, s"cannot parse $rem")
      res.reverse.map(_.syntax).flatMap(_.split(System.lineSeparator))
    }
  }

  def seq(ps: Preprocessor*): Map[String, Preprocessor] =
    ps.map(p => p.name -> p).toMap
}

