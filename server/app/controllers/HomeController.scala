package controllers

import play.api.mvc.{ AbstractController, ControllerComponents }

class HomeController(cc: ControllerComponents, assets: Assets) extends AbstractController(cc) {

  def scriptUrl(projectName: String) = {
    val name = projectName.toLowerCase
    Seq(s"$name-opt-bundle.js", s"$name-fastopt-bundle.js")
      .find(name => getClass.getResource(s"/public/$name") != null)
      .map(controllers.routes.Assets.versioned(_).url)
  }

  def home(path: String) = index

  def index = Action {
    val result = scriptUrl("client")
    Ok(views.html.index(result, assets))
  }

}
