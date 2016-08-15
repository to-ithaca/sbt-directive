import scala.io.Source

val checkIdentity = InputKey[Unit]("checkIdentity")

checkIdentity := {
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val expected = Source.fromFile(args(0)).getLines.filter(_.startsWith("#"))
  val obtained = Source.fromFile(args(1)).getLines

  if(expected != obtained) sys.error(s"File ${args(1)} does not equal ${args(0)} ") else ()
}
