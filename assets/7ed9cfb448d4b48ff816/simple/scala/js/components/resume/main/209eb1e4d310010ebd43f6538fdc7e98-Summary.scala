package simple.scala.js.components.resume.main

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._

class Summary(bs: BackendScope[Unit, Unit]) {

  def render() =
    <.section(
      mainStyles.section,
      <.h2(mainStyles.sectionTitle, faIcon("fas", "user"), "个人简介"),
      <.div(
        <.p(
          <.b("Scala 程序员，反应式宣言践行者。"),
          "活跃于 Akka、Play Framework、Lagom 等开源社区，擅长构建高性能、全异步、健壮且易于扩展的反应式应用，并且能依托 CQRS 架构进行领域开发。" +
            "学习能力强，可以快速掌握新技术，并在工作中实践应用。具有较强的业务攻坚能力、疑难杂症治理能力和架构设计能力。"
        )
      )
    )

}

object Summary {
  private val builder = ScalaComponent
    .builder[Unit]("summary")
    .renderBackend[Summary]
    .build

  def apply() = builder()
}
