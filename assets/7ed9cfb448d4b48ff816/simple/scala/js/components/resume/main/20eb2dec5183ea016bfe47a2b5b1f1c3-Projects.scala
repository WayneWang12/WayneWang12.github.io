package simple.scala.js.components.resume.main

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._

case class ProjectItem(
    title: String,
    href: Option[String],
    detail: String
)

class Projects(bs: BackendScope[Unit, Unit]) {

  val analysis = ProjectItem(
    "实时智能分析系统",
    None,
    "利用 CQRS 架构和位计算构建的实时分析系统。该系统基于事件驱动，并依靠用户和事件类型分别构建聚合根。事件经过埋点由业务端触发提交，" +
      "之后进入 Kafka 队列，并由实时分析引擎来负责事件入库，同时更新用户关注视图的状态。视图状态使用事件溯源方式，可以使得数据形态自由可控。"
  )

  val quillLagom = ProjectItem(
    "quill-lagom模块",
    Some("https://github.com/getquill/quill/pull/1299"),
    "Scala生态中常用的 Slick 并不支持 Cassandra 数据库，Quill 支持但是其与 Lagom 的结合相对复杂。所以开发了quill-cassandra-lagom模块，" +
      "使得在 Lagom 中使用 Quill 更加方便，并将其回馈社区。"
  )

  val recommendation = ProjectItem(
    "数云推荐系统",
    None,
    "借助自然语言处理的思想，将用户的购买行为视为句子，购买商品视为单词，利用 word2vec 算法，每日定时计算用户和商品的词嵌入值，之后通过特定" +
      "条件缩小商品候选集，并使用 BLAS 库进行快速的余弦计算，找出最相似的商品进行推荐。"
  )

  val tagSystem = ProjectItem(
    "数云标签系统",
    None,
    "同样利用 CQRS 架构，构建用户聚合根。将对用户的标签操作都定义为事件，每次事件在改变用户标签之后，同时对 ElasticSearch 进行更新，以提供按标签查询功能"
  )

  val authorizationSystem = ProjectItem(
    "GrowingIO权限系统",
    None,
    "借鉴 SAP 的权限设计思想，建立角色、权限组、权限的三级架构，实现灵活的、可自定义的权限系统，并使用缓存来减轻数据库压力。"
  )

  def item(projectItem: ProjectItem) =
    <.div(
      mainStyles.projectItem,
      ^.key := projectItem.title,
      <.span(
        mainStyles.projectTitle,
        <.a(
          ^.href :=? projectItem.href,
          projectItem.title
        ),
      ),
      " - ",
      <.span(projectItem.detail)
    )

  def render() =
    <.section(
      mainStyles.section,
      <.h2(
        mainStyles.sectionTitle,
        faIcon("fas", "archive"),
        "项目经历"
      ),
      <.div(
        List(analysis, quillLagom, recommendation, tagSystem, authorizationSystem).map(item).toVdomArray
      )
    )
}

object Projects {
  private val builder = ScalaComponent
    .builder[Unit]("Projects")
    .renderBackend[Projects]
    .build

  def apply() = builder()
}
