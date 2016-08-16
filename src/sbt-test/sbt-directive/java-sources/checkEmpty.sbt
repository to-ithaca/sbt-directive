import scala.io.Source

val checkEmpty = InputKey[Unit]("checkEmpty")

checkEmpty := {
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val lines = Source.fromFile(args(0)).getLines

  if(lines.nonEmpty) sys.error(s"File ${args(0)} is non empty - contents ${lines.toList}") else ()
}
