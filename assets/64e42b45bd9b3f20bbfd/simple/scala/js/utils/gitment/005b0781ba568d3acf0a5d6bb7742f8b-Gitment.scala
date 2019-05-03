package simple.scala.js.utils.gitment

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("gitment", JSImport.Default)
class Gitment(options: GitmentOptions) extends js.Object {
  var render: js.Function1[String, Unit] = js.native
}

@js.native
trait GitmentOptions extends js.Object {
  var id: String       = js.native
  var owner: String    = js.native
  var repo: String     = js.native
  var oauth: js.Object = js.native
}

@js.native
trait OauthOptions extends js.Object {
  var client_id: String     = js.native
  var client_secret: String = js.native
}

@js.native
@JSImport("gitment/style/default.css", JSImport.Default)
object GitmentCss extends js.Object
