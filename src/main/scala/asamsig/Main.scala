package asamsig

import asamsig.homepage.{Homepage, Navbar}
import asamsig.posts.{PostsPage, TrackSSRPosts}
import asamsig.reacthelmet.{Helmet, ReactHelmet}
import org.scalajs.dom
import slinky.core.CustomAttribute
import slinky.core.facade.ReactElement
import slinky.history.History
import slinky.hot
import slinky.reactrouter._
import slinky.web.html._
import slinky.web.{ReactDOM, ReactDOMServer}

import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.{LinkingInfo, js}

@JSImport("resources/index.css", JSImport.Default)
@js.native
object IndexCSS extends js.Object

object Main {
  val css = IndexCSS

  def insideRouter: ReactElement = {
    val charSet = CustomAttribute[String]("charSet")
    div(
      Helmet(
        lang := "en",
        meta(charSet := "utf-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1, shrink-to-fit=no"),
        meta(name := "theme-color", content := "#000000"),
        link(rel := "manifest", href := "/manifest.json"),
        link(rel := "shortcut icon", href := "/favicon.ico"),
        title("ASamsig"),
        style(`type` := "text/css")(IndexCSS.toString)
      ),
      Navbar.component(),
      div(style := js.Dynamic.literal(
        marginTop = "60px"
      ))(
        Switch(
          Route("/", Homepage, exact = true),
          Route("/posts/*", PostsPage.component),
          Route("*", Homepage)
        )
      )
    )
  }

  @JSExportTopLevel("main")
  def main(): Unit = {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }

    val container = Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }

    ReactDOM.render(
      Router(history = History.createBrowserHistory())(insideRouter),
      container
    )
    ()
  }

  var isSSR = false

  @JSExportTopLevel("ssr")
  def ssr(path: String): String = {
    isSSR = true
    TrackSSRPosts.publicSSR = js.Dictionary.empty

    val reactTree = ReactDOMServer.renderToString(
      StaticRouter(location = path, context = js.Dynamic.literal())(
        insideRouter
      )
    )

    val helmetContent = ReactHelmet.Helmet.renderStatic()

    s"""<!DOCTYPE html>
       |<html>
       |  <head>
       |    ${helmetContent.title.toString}
       |    ${helmetContent.meta.toString}
       |    ${helmetContent.link.toString}
       |    ${helmetContent.style.toString}
       |  </head>
       |  <body>
       |    <div id="root">
       |      $reactTree
       |    </div>
       |    <script type="text/javascript">window.publicSSR = ${js.JSON.stringify(TrackSSRPosts.publicSSR)}</script>
       |    <script async src="/asamsig-site-opt-bundle.js"></script>
       |  </body>
       |</html>""".stripMargin
  }

  @JSExportTopLevel("hydrate")
  def hydrate(): Unit = {
    val container = dom.document.getElementById("root")

    ReactDOM.hydrate(
      Router(history = History.createBrowserHistory())(insideRouter),
      container
    )
    ()
  }
}
