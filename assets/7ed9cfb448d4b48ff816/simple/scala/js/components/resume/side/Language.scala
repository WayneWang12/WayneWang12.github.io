package simple.scala.js.components.resume.side

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

class Language(bs: BackendScope[Unit, Unit]) {

  private val sideContainerStyle = GlobalRegistry[SideBarStyles].get

  private def skillListItem(item: String, level: String) =
    <.li(
      item,
      ^.key := item,
      <.span(
        sideContainerStyle.languageDesc,
        s"（$level）"
      )
    )

  def render() =
    <.div(
      sideContainerStyle.containerBlock,
      <.h2(
        sideContainerStyle.containerBlockTitle,
        "英语能力"
      ),
      <.ul(
        sideContainerStyle.languageList,
        skillListItem("阅读", "优秀"),
        skillListItem("听力", "良好"),
        skillListItem("四级", "602"),
        skillListItem("六级", "539"),
      )
    )
}

object Language {

  private val builder = ScalaComponent
    .builder[Unit]("Language")
    .renderBackend[Language]
    .build

  def apply() = builder()
}
