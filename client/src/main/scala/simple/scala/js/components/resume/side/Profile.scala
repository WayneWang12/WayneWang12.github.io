package simple.scala.js.components.resume.side

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

class Profile(bs: BackendScope[Unit, Unit]) {

  def render = {
    val profileStyle = GlobalRegistry[SideBarStyles].get
    <.div(
      profileStyle.profileContainer,
      <.img(
        ^.src := "profile.jpg",
        profileStyle.profile
      ),
      <.h1(
        profileStyle.name,
        "王石冲"
      ),
      <.h3(
        profileStyle.tagLine,
        "Scala工程师"
      )
    )
  }

}

object Profile {
  private val builder = ScalaComponent
    .builder[Unit]("profile")
    .renderBackend[Profile]
    .build

  def apply() = builder()
}
