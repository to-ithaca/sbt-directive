package directive

sealed trait DeferredPreprocessor {
  def source: String
}



object Preprocess {
  def lines(name: String)(f: String): DeferredPreprocessor = 
    new DeferredPreprocessor {
      val source = s"""_root_.cli.Preprocessor.lines("$name")($f)"""
  }
}
