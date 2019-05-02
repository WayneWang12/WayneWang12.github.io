package simple.scala.js.components.resume

import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

package object main {

  val mainStyles = GlobalRegistry[MainStyles].get

  def faIcon(prefix: String, icon: String) = <.i(
    mainStyles.fa,
    ^.`class` := s"$prefix fa-$icon"
  )

}
