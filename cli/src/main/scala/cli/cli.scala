package cli

import java.io.File

final class Cli(preprocessor: Preprocessor) {
  def run(srcDir: File, targetDir: File): Unit = {
    println("running cli")
    preprocessor.transform("empty")
  }
}
