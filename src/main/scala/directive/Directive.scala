package directive

import sbt._
import sbt.Keys._

object Directive {

  def runTask(log: Logger, srcDir: File): Seq[File] = {
    log.info(s"parsing directives in $srcDir")
    Seq.empty
  }

  def apply(): Def.Initialize[Task[Seq[File]]] = Def.task(
    runTask(streams.value.log, sourceDirectory.value)
  )
}
