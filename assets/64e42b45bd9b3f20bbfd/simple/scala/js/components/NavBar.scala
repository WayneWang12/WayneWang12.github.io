package simple.scala.js.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

class NavBar(bs: BackendScope[RouterCtl[Page], Unit]) {
  val dataTarget = VdomAttr("data-target")

  def render(routerCtl: RouterCtl[Page]) =
    <.nav(
      <.div(
        ^.`class` := "nav-wrapper teal",
        <.div(
          ^.`class` := "container",
          <.a(^.href := "#",
              dataTarget := "nav-mobile",
              ^.`class` := "sidenav-trigger",
              <.i(^.`class` := "material-icons", "menu")),
          <.ul(^.`class` := "sidenav",
               ^.id := "nav-mobile",
               <.li(
                 <.a("博客"),
                 routerCtl.setOnClick(Home)
               ),
               <.li(
                 <.a("关于我"),
                 routerCtl.setOnClick(About)
               )),
          <.a(^.href := "#", ^.`class` := "brand-logo", "写Scala的老王"),
          <.ul(
            ^.id := "nav-mobile",
            ^.`class` := "right hide-on-med-and-down",
            <.li(
              <.a("博客"),
              routerCtl.setOnClick(Home)
            )
//            ),
//            <.li(
//              <.a("关于我"),
//              routerCtl.setOnClick(About)
//            )
          )
        )
      )
    )

}

object NavBar {
  private val builder = ScalaComponent
    .builder[RouterCtl[Page]]("NavBar")
    .renderBackend[NavBar]
    .build

  def apply(r: RouterCtl[Page]): Unmounted[RouterCtl[Page], Unit, NavBar] = builder(r)
}

sealed trait Page
case object Home  extends Page
case class ArticlePage(fileName:String) extends Page
case object About extends Page
