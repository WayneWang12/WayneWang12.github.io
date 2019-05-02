package simple.scala.js.components.resume

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry
import simple.scala.js.components.resume.main.{MainContent, MainStyles}
import simple.scala.js.components.resume.side.SideBar

class ResumeComponent(bs: BackendScope[Unit, Unit]) {
  val resumeStyle = GlobalRegistry[MainStyles].get

  def print() = {}

  def render() =
    <.div(
      resumeStyle.body,
      <.div(
        resumeStyle.wrapper,
        SideBar(),
        MainContent(),
      )
    )
}

object ResumeComponent {

  private val builder = ScalaComponent
    .builder[Unit]("Resume")
    .renderBackend[ResumeComponent]
    .build

  def apply() = builder()
}
