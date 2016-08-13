package directive

sealed trait DeferredPreprocessor {
  def source: String
}



object Preprocess {

  def ast(name: String)(f: String): DeferredPreprocessor =
    new DeferredPreprocessor {
      val source = s"""_root_.cli.Preprocessor.ast("$name")($f)"""
    }

  def lines(name: String)(f: String): DeferredPreprocessor = 
    new DeferredPreprocessor {
      val source = s"""_root_.cli.Preprocessor.lines("$name")($f)"""
  }
}
