package simple.scala.js.utils.markdown

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object Markdown {

  import simple.scala.js.utils.markdown.Markdown.Marked.Options

  @JSImport("marked", JSImport.Default)
  @js.native
  private object Marked extends js.Object {

    @js.native
    trait Options extends js.Object {
      var renderer: Renderer = js.native
    }

    @js.native
    class Renderer extends js.Object {
      var heading: js.Function2[String, Int, String] = js.native
    }

    def apply(source: String, option: Options): String = js.native
  }

  private val render  = new Marked.Renderer()
  private val options = new js.Object().asInstanceOf[Options]
  render.heading = (text, level) => {
    s"""<h$level><span>$text</span></h$level>"""
  }
  options.renderer = render

  def apply(source: String) = Marked(source, options)

}
