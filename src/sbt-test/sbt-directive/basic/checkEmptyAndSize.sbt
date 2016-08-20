import scala.io.Source

val checkEmptyAndSize = InputKey[Unit]("checkEmptyAndSize")

checkEmptyAndSize := {
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val source = Source.fromFile(args(0))
  val lines = source.getLines.toList
  val nonEmptyLines = lines.filter(_.nonEmpty)
  source.close

  if(nonEmptyLines.nonEmpty) sys.error(s"File ${args(0)} is non empty - contents ${nonEmptyLines.toList}") 
  else {
    val expectedSize = args(1).toInt
    if(lines.size != expectedSize) 
      sys.error(s"File [ ${args(0)} ] contains [ ${lines.size} ] lines, expected [ $expectedSize ] lines") 
    else ()
  }
}
