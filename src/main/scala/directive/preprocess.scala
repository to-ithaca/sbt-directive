package directive

sealed trait DeferredPreprocessor {
  def source: String
}

object Preprocess {

  def ast(tag: String)(f: String): DeferredPreprocessor =
    new DeferredPreprocessor {
      val source = s"""_root_.cli.Preprocessor.ast("$tag")($f)"""
    }

  def lines(tag: String)(f: String): DeferredPreprocessor = 
    new DeferredPreprocessor {
      val source = s"""_root_.cli.Preprocessor.lines("$tag")($f)"""
    }

  def skip(tag: String): DeferredPreprocessor =
    lines(tag)(s"""_.map(_ => "" )""")

  def identity(tag: String): DeferredPreprocessor =
    lines(tag)("identity")
}
