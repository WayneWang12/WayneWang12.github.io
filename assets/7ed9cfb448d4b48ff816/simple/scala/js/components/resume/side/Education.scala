package simple.scala.js.components.resume.side

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

class Education(bs: BackendScope[Unit, Unit]) {

  private val sideContainerStyle = GlobalRegistry[SideBarStyles].get

  def render() =
    <.div(
      sideContainerStyle.containerBlock,
      <.h2(
        sideContainerStyle.containerBlockTitle,
        "教育经历"
      ),
      <.h4(
        sideContainerStyle.euducationDegree,
        "计算机专业 本科"
      ),
      <.h5(
        sideContainerStyle.educationMeta,
        "华中科技大学"
      ),
      <.div(
        sideContainerStyle.educationTime,
        "2007 - 2011"
      )
    )
}

object Education {

  private val builder = ScalaComponent
    .builder[Unit]("education")
    .renderBackend[Education]
    .build

  def apply() = builder()
}
