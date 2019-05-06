package simple.scala.js.components.resume.side

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.{Div, Element}
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

class Contact(bs: BackendScope[Unit, Unit]) {
  private val sideStyles = GlobalRegistry[SideBarStyles].get

  val emailAddress = "scweang@hotmail.com"
  val phoneNumber  = "186********"
  val website      = "waynewang12.me"

  def listIcon(prefix: String, icon: String): VdomTagOf[Element] = <.i(
    sideStyles.fa,
    ^.`class` := s"$prefix fa-$icon"
  )

  def render: VdomTagOf[Div] =
    <.div(
      sideStyles.containerBlock,
      <.ul(
        sideStyles.contactList,
        <.li(
          listIcon("far", "envelope"),
          <.a(
            ^.href := s"mailto: $emailAddress",
            emailAddress
          )
        ),
        <.li(
          listIcon("fas", "phone"),
          <.a(
            ^.href := s"tel: $phoneNumber",
            phoneNumber
          )
        ),
        <.li(
          listIcon("fas", "globe-asia"),
          <.a(
            ^.href := s"https://$website",
            ^.target.blank,
            website
          )
        ),
        <.li(
          listIcon("fab", "github"),
          <.a(
            ^.href := "https://github.com/WayneWang12",
            ^.target.blank,
            "WayneWang12"
          )
        ),
        <.li(
          listIcon("fab", "weixin"),
          "******"
        )
      )
    )
}

object Contact {

  private val builder = ScalaComponent
    .builder[Unit]("Contact")
    .renderBackend[Contact]
    .build

  def apply(): Unmounted[Unit, Unit, Contact] = builder()

}
