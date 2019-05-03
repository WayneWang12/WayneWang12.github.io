package simple.scala.js.components.blog

import java.time.LocalDate

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^._
import play.api.libs.json.Json

class BlogComponent(bs: BackendScope[Unit, List[ArticleSummary]]) {

  def getArticleIndex =
    Ajax
      .get("assets/blog/index.json")
      .send
      .onComplete { xhr =>
        xhr.status match {
          case 200 =>
            val results = Json.parse(xhr.responseText).as[List[ArticleSummary]]
            bs.setState(results)
          case _ =>
            bs.setState(List.empty)
        }
      }
      .asCallback

  def render(articleSummaries: List[ArticleSummary]) =
    <.div(
      ^.`class` := "container",
      articleSummaries.map(ArticleSummaryCard.component).toVdomArray
    )
}

object BlogComponent {

  val summaries = List(
    ArticleSummary(
      "cqrs",
      LocalDate.now().toString,
      "CRUD写累了？试试CQRS吧！",
      "很多年以来，程序员的工作都充斥着大量的CRUD开发，我们要做的就是根据实体的变化去创建、查询、更新或者删除数据库中的记录而已。这些开发工作并不难，所以做多了以后，业务程序员可能会觉得枯燥、没意思。因为开发工作也许就是调调其他人的接口、提供一下自己的接口，然后顺便更新后面的库而已。\n\n但是实际上，我一直以来的观点是，只要在系统性能指标可以接受的范围内，快速支撑业务往往比精良的高性能系统更重要，除非你的当前CRUD系统无法支撑自己的海量业务和复杂需求。这个时候，就需要架构师来做一下架构升级，要么将后端的数据库引入主从提升性能，要么分库分表，要么引入复杂的分布式数据库，以继续提供ACID的支持。\n\n当然，还有另一种选项，就是CQRS。"
    ),
    ArticleSummary(
      "cap",
      LocalDate.now().toString,
      "你真的懂CAP吗？",
      "想写这个是源于微信群里面的一个讨论。在讨论分布式系统的时候，有群友明确地如下说：\n\n> CAP是可以兼顾的啊！\n\n这把我惊起了一身冷汗，赶紧去查了一下是不是分布式系统理论界又有新的论文来推翻了之前的CAP定理了。后来深入讨论以后，才发现是他对CAP的理解有误。\n\nCAP理论是分布式领域的基础，所以大家的讨论和研究很多。学界和工业界也想出来好多办法来折中处理不可兼得时候的情形，例如著名的“BASE\"。但是诸如上面的“CAP可以兼顾”的话是绝对不应该出现的。如果能证明这点并且能写出学术文章的话，那是肯定能发 PODC 并且成为学术大牛的。而现阶段的研究没有一个往着打破CAP定理的方向走，这说明CAP定理挺牢固的，只是因为“BASE”的存在而产生好像兼顾了的误解。那么，为了帮助大家更好的理解CAP及其应用呢，借此机会，我来试着写篇文章讨论一下这方面的内容，并且争取能通过实践将其表达的更加清楚。"
    ),
    ArticleSummary(
      "polardb",
      LocalDate.now().toString,
      "第一届阿里云PolarDB数据库性能大赛\"RDP飞起来\"队伍攻略总结",
      "持续好几个月的第一届阿里云PolarDB性能挑战赛终于圆满结束了。我所在的“RDP飞起来”团队获得了初赛第三、复赛第六的名次，最终拿到了赛事的季军。在参加比赛的这段时间内，我学到了很多东西，认识了很多人，也有幸去北京阿里中心转了一圈，聆听各位大佬教导，受益匪浅。为了让其他同样对阿里比赛有兴趣的朋友可以有同样的机会，这里将我这段时间的比赛心得做一个分享，希望能对后续想参加比赛的人有所帮助。"
    ),
    ArticleSummary(
      "jmh",
      LocalDate.now().toString,
      "应用JMH测试大型HashMap的性能",
      "写这篇是因为PolarDB比赛很重要的一点是控制内存。C++只有2G，Java也只有3G，而6400W的键值对，即使只是`Long`类型，也需要`16 * 64 * 10e6 ≈ 1G`的内存，这还不包括其他对象引用的相关开销，所以内存控制在这里是非常重要的，因为稍不小心就会被CGroup无情地kill掉。因此在比赛开始没多久的时候我就研究了一下使用怎样的HashMap可以达到内存最简的状况。在这个过程中，顺便使用了JMH来分析了一下几个侯选库的性能。因为初赛相对来说比较简单，而且HashMap实际上在复赛时候的Range操作上没有发挥余地，所以我决定将这篇写下来分享给大家，希望能帮助更多对比赛有兴趣的同学找到一个比较好的入手点。\n\n之前的初赛简单思路可以看这里。"
    )
  )

  def apply() =
    ScalaComponent
      .builder[Unit]("Blog")
      .initialState(List.empty[ArticleSummary])
      .renderBackend[BlogComponent]
      .componentDidMount(_.backend.getArticleIndex)
      .build
      .apply()
}
