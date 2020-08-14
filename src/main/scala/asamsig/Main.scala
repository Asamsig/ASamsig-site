package asamsig

import asamsig.context.DarkMode
import asamsig.posts.TrackSSRPosts
import asamsig.reacthelmet.ReactHelmet
import org.scalajs.dom
import slinky.core.facade.{React, ReactElement}
import slinky.history.History
import slinky.hot
import slinky.reactrouter._
import slinky.web.{ReactDOM, ReactDOMServer}

import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.{LinkingInfo, js}

@JSImport("resources/index.css", JSImport.Default)
@js.native
object IndexCSS extends js.Object

object Main {
  val css = IndexCSS

  val darkModeContext = React.createContext[DarkMode](DarkMode())

  def insideRouter: ReactElement = {
    App.component()
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
       |<html lang="en">
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
