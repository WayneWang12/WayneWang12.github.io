package simple.scala.js.utils.hightlight

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("highlight.js/lib/highlight", JSImport.Default)
object hljs extends js.Object {
  def initHighlightingOnLoad():Unit = js.native
  def highlightBlock(block:js.Object):Unit = js.native
  def registerLanguage(name:String, block:js.Object):Unit = js.native

}

@js.native
@JSImport("highlight.js/styles/github.css", JSImport.Default)
object Github extends js.Object

@js.native
@JSImport("highlight.js/lib/languages/scala", JSImport.Default)
object ScalaCodeJs extends js.Object

