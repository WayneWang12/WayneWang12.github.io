package simple.scala.js.components.blog.services

import japgolly.scalajs.react.{AsyncCallback, Callback}
import japgolly.scalajs.react.extra.Ajax
import play.api.libs.json.Json
import simple.scala.js.components.blog.ArticleSummary

object PostService {

  def getSummaries: AsyncCallback[List[ArticleSummary]] =
    Ajax
      .get("assets/blog/index.json")
      .send
      .validateStatusIs(200)(Callback.error)
      .asAsyncCallback
      .map { resp =>
        Json.parse(resp.responseText).as[List[ArticleSummary]]
      }

  def getSummary(fileName: String): AsyncCallback[Option[ArticleSummary]] =
    getSummaries.map(_.find(_.fileName == fileName))

  def getContent(fileName: String): AsyncCallback[String] =
    Ajax
      .get(s"assets/blog/$fileName.md")
      .send
      .validateStatusIs(200)(Callback.error)
      .asAsyncCallback
      .map { resp =>
        resp.responseText
      }

}
