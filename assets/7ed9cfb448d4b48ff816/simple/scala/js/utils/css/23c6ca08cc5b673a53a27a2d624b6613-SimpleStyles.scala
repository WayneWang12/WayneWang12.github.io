package simple.scala.js.utils.css

// This will choose between dev/prod depending on your scalac `-Xelide-below` setting

object SimpleStyles {
  val CssSettings = scalacss.devOrProdDefaults
}
