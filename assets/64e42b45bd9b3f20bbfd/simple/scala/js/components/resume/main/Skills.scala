package simple.scala.js.components.resume.main

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._

class Skills(bs: BackendScope[Unit, Unit]) {

  def skill(item: String) =
    <.li(
      mainStyles.skillItem,
      ^.key := item,
      item
    )

  def render() =
    <.section(
      mainStyles.section,
      <.h2(
        mainStyles.sectionTitle,
        faIcon("fas", "rocket"),
        "熟悉技能",
      ),
      <.div(
        <.ul(
          List("Scala",
               "Java",
               "Akka",
               "Lagom",
               "Spark",
               "PlayFramework",
               "Netty",
               "Cassandra",
               "Quill",
               "ScalaTest",
               "ScalaJs",
               "C++").map(skill).toVdomArray
        )
      )
    )

}

object Skills {
  def apply(): Unmounted[Unit, Unit, Skills] =
    ScalaComponent
      .builder[Unit]("Skills")
      .renderBackend[Skills]
      .build
      .apply()
}
