package simple.scala.js.components.resume.main

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

class MainContent(bs: BackendScope[Unit, Unit]) {

  private val mainContentStyle = GlobalRegistry[MainStyles].get

  def render() =
    <.div(
      mainContentStyle.mainWrapper,
      Summary(),
      Experiences(),
      Projects(),
      Skills(),
    )
}

object MainContent {

  private val builder = ScalaComponent
    .builder[Unit]("MainContent")
    .renderBackend[MainContent]
    .build

  def apply() = builder()
}
