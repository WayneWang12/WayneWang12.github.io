import controllers.{Assets, HomeController}
import play.api.ApplicationLoader.Context
import play.api.{ApplicationLoader, BuiltInComponentsFromContext}
import router.Routes
import com.softwaremill.macwire._
import play.api.mvc.EssentialFilter

class Loader extends ApplicationLoader {
  def load(context: Context) = new ExampleComponents(context).application
}

class ExampleComponents(context: Context) extends BuiltInComponentsFromContext(context) with controllers.AssetsComponents {
  val controller = wire[HomeController]
  val router = {
    val s = "/"
    wire[Routes]
  }

  override def httpFilters: Seq[EssentialFilter] = Seq.empty
}
