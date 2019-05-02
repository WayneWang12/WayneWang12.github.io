package simple.scala.js.components.resume.side

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

class WeChatQRCode(bs:BackendScope[Unit, Unit]) {
  private val sideContainerStyle = GlobalRegistry[SideBarStyles].get

  def render() =
    <.div(
      sideContainerStyle.containerBlock,
      <.h2(
        sideContainerStyle.containerBlockTitle,
        "公众号"
      ),
      <.img(
        ^.src := "qrcode.jpg",
        sideContainerStyle.qrCode
      )
    )

}

object WeChatQRCode {
  def apply() = ScalaComponent.builder[Unit]("WeChatQRCode")
    .renderBackend[WeChatQRCode]
    .build.apply()
}


