import scala.io.Source

val checkIdentity = InputKey[Unit]("checkIdentity")

checkIdentity := {
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val expected = Source.fromFile(args(0)).getLines.filterNot(_.startsWith("#")).toList
  val obtained = Source.fromFile(args(1)).getLines.toList

  if(expected != obtained) sys.error(s"File ${args(1)} [ $expected ] does not equal ${args(0)} [ $obtained ]") else ()
}
