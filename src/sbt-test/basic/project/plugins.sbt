Option(System.getProperty("plugin.version")) match {
  case None =>
    throw new RuntimeException(
      """|The system property 'plugin.version' is not defined.
         |Please specify this property using the SBT flag -D.""".stripMargin)
  case Some(pluginVersion) =>
    addSbtPlugin("sbt-directive" % "sbt-directive" % pluginVersion)
}
