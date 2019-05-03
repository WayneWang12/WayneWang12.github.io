package simple.scala.js

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.router.{
  BaseUrl,
  Redirect,
  Resolution,
  Router,
  RouterConfig,
  RouterConfigDsl,
  RouterCtl
}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document
import org.scalajs.dom.raw.UIEvent
import scalacss.internal.mutable.GlobalRegistry
import simple.scala.js.components._
import simple.scala.js.components.blog.{ ArticleDetail, ArticleStyle, BlogComponent }
import simple.scala.js.components.resume.ResumeComponent
import simple.scala.js.components.resume.main.MainStyles
import simple.scala.js.components.resume.side.SideBarStyles
import simple.scala.js.utils.gitment.GitalkCss
import simple.scala.js.utils.hightlight.{ hljs, Github, ScalaCodeJs }
import simple.scala.js.utils.materialize._

import scala.scalajs.js

object Main {
  MaterializeJs
  Materialize
  MaterialIcons
  Fontawesome
  hljs
  Github
  GitalkCss
  hljs.registerLanguage("scala", ScalaCodeJs)

  def registerCss() = {
    GlobalRegistry.register(new SideBarStyles)
    GlobalRegistry.register(new MainStyles)
    GlobalRegistry.register(new ArticleStyle)
  }

  import simple.scala.js.utils.css.SimpleStyles.CssSettings._

  def main(args: Array[String]): Unit = {
    registerCss()
    GlobalRegistry.addToDocumentOnRegistration()

    def layout(c: RouterCtl[Page], r: Resolution[Page]) =
      <.div(NavBar(c), <.div(r.render()))

    val config: RouterConfig[Page] = RouterConfigDsl[Page].buildConfig { dsl =>
      import dsl._

      (emptyRule
        | staticRoute(root, Home) ~> render(BlogComponent())
        | staticRoute("#about", About) ~> render(ResumeComponent())
        | dynamicRouteCT("#article" ~ ("/" ~ string("[a-zA-Z0-9]+")).caseClass[ArticlePage])
          ~> dynRender(ArticleDetail.component))
        .notFound(redirectToPage(Home)(Redirect.Push))
        .renderWith(layout)
    }

    val router = Router(BaseUrl.fromWindowUrl(_.takeWhile(_ != '#')), config)

    router().renderIntoDOM(
      document.getElementById("body"),
      Callback {
        document.addEventListener("DOMContentLoaded", (t: UIEvent) => {
          val elems = document.querySelectorAll(".sidenav")
          M.asInstanceOf[js.Dynamic].Sidenav.init(elems)
        })
      }
    )

  }
}
