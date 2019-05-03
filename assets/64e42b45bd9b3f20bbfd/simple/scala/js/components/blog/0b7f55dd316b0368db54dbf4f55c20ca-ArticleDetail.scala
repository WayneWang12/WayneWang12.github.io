package simple.scala.js.components.blog

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import simple.scala.js.components.ArticlePage
import simple.scala.js.components.blog.services.PostService
import simple.scala.js.utils.gitment.{ Gitalk, GitalkOptions }
import simple.scala.js.utils.hightlight.hljs
import simple.scala.js.utils.markdown.Markdown

import scala.collection.mutable
import scala.scalajs.js

case class ArticleState(
    summary: Option[ArticleSummary],
    content: Option[String]
)

class ArticleDetail(bs: BackendScope[ArticlePage, ArticleState]) {

  def updateCodeStyle = Callback {
    val codes = org.scalajs.dom.document.querySelectorAll("pre code")
    for (i <- 0 until codes.length) {
      hljs.highlightBlock(codes.item(i))
    }
  }

  def readPage(page: ArticlePage): Callback = {
    val f = for {
      summary <- PostService.getSummary(page.fileName)
      content <- PostService.getContent(page.fileName)
      state   = ArticleState(summary = summary, content = Some(content))
      _       <- bs.modState(_ => state).asAsyncCallback
    } yield {}
    f.toCallback
  }

  def renderComment =
    bs.state.map { state =>
      state.summary.foreach { summary =>
        val gitalkOptions = new js.Object().asInstanceOf[GitalkOptions]
        gitalkOptions.client_id = "1d9ab4c847ff3b41e99d"
        gitalkOptions.client_secret = "2b909e4055761646dc5ddc341a1fae6759c65aa1"
        gitalkOptions.id = summary.fileName
        gitalkOptions.owner = "WayneWang12"
        gitalkOptions.repo = "WayneWang12.github.io"
        gitalkOptions.admin = "WayneWang12"
        val gitalk = new Gitalk(gitalkOptions)
        gitalk.render("comment")
      }
    }

  def render(state: ArticleState) =
    state.summary.map { summary =>
      val markedContent = Markdown(state.content.get)
      <.div(
        ^.`class` := "container",
        ^.key := summary.fileName,
        <.div(
          ^.key := summary.fileName,
          articleStyle.widget,
          <.h2(
            articleStyle.dateHeader,
            <.span(
              new js.Date(summary.createdAt).asInstanceOf[js.Dynamic].toLocaleString("en-us", jsDateOptions).toString)),
          <.div(
            articleStyle.post,
            <.div(
              <.h1(
                <.a(
                  ^.href := s"#article/${summary.fileName}",
                  summary.title
                )
              )
            ),
            <.div(
              ^.dangerouslySetInnerHtml := markedContent,
            )
          )
        ),
        <.div(^.id := "comment", articleStyle.comment)
      )
    }.getOrElse(<.h3("Loading"))
}

object ArticleDetail {

  def component(articleInfo: ArticlePage): Unmounted[ArticlePage, ArticleState, ArticleDetail] =
    ScalaComponent
      .builder[ArticlePage]("Article")
      .initialState(ArticleState(None, None))
      .renderBackend[ArticleDetail]
      .componentDidMount(pp => pp.backend.updateCodeStyle >> pp.backend.readPage(pp.props))
      .componentDidUpdate(bs => bs.backend.updateCodeStyle >> bs.backend.renderComment)
      .build
      .apply(articleInfo)
}
