package simple.scala.js.components.resume.side

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

class SideBar(bs: BackendScope[Unit, Unit]) {

  private val sideBarStyle = GlobalRegistry[SideBarStyles].get

  private val awardItem = SideBarItemInfo(
    "获奖情况",
    List(
      "阿里中间件比赛优胜奖",
      "阿里云 PolarDB 比赛季军",
    )
  )

  private val translation = SideBarItemInfo(
    "译著",
    List(
      "《反应式设计模式》"
    )
  )

  def render() =
    <.div(
      sideBarStyle.sidebarWrapper,
      Profile(),
      Contact(),
      Education(),
      Language(),
      SideBarItem(awardItem),
      SideBarItem(translation),
      WeChatQRCode()
    )

}

object SideBar {

  private val builder = ScalaComponent
    .builder[Unit]("ResumeSideBar")
    .renderBackend[SideBar]
    .build

  def apply() = builder()
}
