package simple.scala.js.utils.gitment

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("gitalk", JSImport.Default)
class Gitalk(var options: js.Object = js.native) extends js.Object {
  def render(container: String): js.Any = js.native
}

@js.native
trait GitalkOptions extends js.Object {
  var id: String                   = js.native
  var owner: String                = js.native
  var repo: String                 = js.native
  var client_id: String            = js.native
  var client_secret: String        = js.native
  var admin: String                = js.native
  var distractionFreeMode: Boolean = js.native
}

@js.native
@JSImport("gitalk/dist/gitalk.css", JSImport.Default)
object GitalkCss extends js.Object
