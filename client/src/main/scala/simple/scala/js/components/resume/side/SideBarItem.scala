package simple.scala.js.components.resume.side

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

case class SideBarItemInfo(title:String,
                        items:List[String])

class SideBarItem(bs: BackendScope[SideBarItemInfo, Unit]) {
  private val sideContainerStyle = GlobalRegistry[SideBarStyles].get

  private def award(item: String) = {
    <.li(
      ^.key := item,
      item
    )
  }

  def render(info: SideBarItemInfo) =
    <.div(
      sideContainerStyle.containerBlock,
      <.h2(
        sideContainerStyle.containerBlockTitle,
        info.title
      ),
      <.ul(
        info.items.map(award).toVdomArray
      )
    )
}

object SideBarItem {
  private val builder = ScalaComponent
    .builder[SideBarItemInfo]("SideBarItem")
    .renderBackend[SideBarItem]
    .build

  def apply(sideBarItemInfo: SideBarItemInfo) = builder(sideBarItemInfo)

}
