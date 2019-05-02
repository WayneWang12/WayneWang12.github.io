package simple.scala.js.components.resume.main

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._

case class JobItem(
    title: String,
    company: String,
    time: String,
    detail: String
)

class Experiences(bs: BackendScope[Unit, Unit]) {

  val shuyun1 = JobItem(
    "架构师",
    "杭州数云上海研发中心",
    "2017.04 - 现在",
    "<b>负责处理公司内部较难的技术问题，并且预研新技术。</b>" +
      "独自开发了公司的标签系统，使其支持自定义实体和自定义标签，支持任意正反查和部分聚合函数" +
      "，并将其交付公司平台版使用；之后接手数据组的推荐系统的开发，在公司资源有限、客户数据量较多的情形下，放弃了传统的需要大量资源进行训练的协同过滤算法，" +
      "转而使用简版的深度神经网络来处理推荐数据，并取得了优于原先数据组的ALS算法的推荐结果，以及在公司小规模集群下将近10倍的训练速度提升；之后为了改变在公司" +
      "内一直扮演救火、打杂的角色的现状，提议开发实时智能分析系统，以支撑客户精益数据分析的需求。目前该系统已经支持精益数据模型" +
      "中的留存、趋势和漏斗。"
  )

  val gio = JobItem(
    "架构师",
    "GrowingIO",
    "2016.06 - 2017.03",
    "<b>负责重构和整理GIO后端系统，构建研发规范体系。</b>在职期间，对公司创业时期的代码进行了模块整理和重构，拆分业务模块，明晰依赖管理，并且通过研发流程的规范" +
      "引入了持续集成机制，提升了研发效率。在业务上，与前端合作开发了权限管理模块，支持自定义角色和自定义权限，以服务于企业多变的权限需求；" +
      "与大数据组一起开发了智能路径系统，可以自动探索用户在行为端的常用路径，为客户优化自家应用提供帮助。该功能获得了公司内部奖励。"
  )

  val shuyun2 = JobItem(
    "Scala工程师",
    "杭州数云上海研发中心",
    "2014.04 - 2016.05",
    "<b>负责协助构建公司的微服务体系，并维护原有的订购中心系统。</b>期间，独自开发了两个高性能网关，其一：API网关，利用单一接口，使得微服务注册即可用，" +
      "并且遵循反应式编程原则，提供了对相应服务的限流、熔断等功能，同时还进行了API鉴权；其二：第三方平台网关，将公司业务依赖的" +
      "淘宝、京东等第三方平台的接口进行抽象，提供统一接口，分别进行权限控制，同时提供类似 API 网关的限流、熔断功能。"
  )

  val lenovo = JobItem(
    "Java工程师",
    "联想（北京）有限公司",
    "2011.07 - 2013.07",
    "<b>开发和维护联想的SAP CRM系统。</b>使用 Java 和 ABAP 语言，在 SAP 提供的框架下进行CRM应用开发。入职时分配的职位是类似产品经理的角色，" +
      "后经过努力成为了开发，并且成为团队中唯一可以处理三方面事务的人，晋级L3工程师。"
  )

  def experienceItem(jobItem: JobItem, index: Int) =
    <.div(
      mainStyles.jobItem,
      ^.key := index,
      <.div(
        mainStyles.upperRow,
        <.h3(mainStyles.jobTitle, jobItem.title),
        <.div(mainStyles.jobTime, jobItem.time)
      ),
      <.div(mainStyles.jobCompany, jobItem.company),
      <.div(
        ^.dangerouslySetInnerHtml := jobItem.detail
      )
    )

  def render() =
    <.section(
      mainStyles.section,
      <.h2(
        mainStyles.sectionTitle,
        faIcon("fas", "briefcase"),
        "工作经验",
      ),
      <.div(
        List(shuyun1, gio, shuyun2, lenovo).zipWithIndex.map((experienceItem _).tupled).toVdomArray
      )
    )

}

object Experiences {
  private val builder = ScalaComponent
    .builder[Unit]("Experiences")
    .renderBackend[Experiences]
    .build

  def apply() = builder()
}
