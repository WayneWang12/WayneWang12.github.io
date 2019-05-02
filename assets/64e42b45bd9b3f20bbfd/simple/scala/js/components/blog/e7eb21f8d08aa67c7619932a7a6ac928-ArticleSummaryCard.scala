package simple.scala.js.components.blog

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import play.api.libs.json.{ Json, OFormat }
import scalacss.ScalaCssReact._
import simple.scala.js.utils.markdown.Markdown

import scala.scalajs.js

case class ArticleSummary(fileName: String, createdAt: String, title: String, summary: String)
object ArticleSummary {
  implicit val formatter: OFormat[ArticleSummary] = Json.format[ArticleSummary]
}

class ArticleSummaryCard(bs: BackendScope[ArticleSummary, Unit]) {

  def render(summary: ArticleSummary) = {
    val markedContent = Markdown(summary.summary)
    <.div(
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
      )
    )
  }

}

object ArticleSummaryCard {

  def component(summary: ArticleSummary): Unmounted[ArticleSummary, Unit, ArticleSummaryCard] =
    ScalaComponent
      .builder[ArticleSummary]("ArticleSummary")
      .renderBackend[ArticleSummaryCard]
      .build
      .apply(summary)

}
