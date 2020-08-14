package asamsig

import asamsig.context.DarkMode
import asamsig.homepage.{Homepage, Navbar}
import asamsig.posts.PostsPage
import asamsig.reacthelmet.Helmet
import org.scalajs.dom
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.{CustomAttribute, FunctionalComponent}
import slinky.reactrouter.{Route, Switch}
import slinky.web.html._

import scala.scalajs.js

@react object App {

  val component = FunctionalComponent[Unit] { _ =>
    val (darkMode, setDarkMode) = useState(DarkMode())

    val charSet = CustomAttribute[String]("charSet")

    useEffect(() => dom.document.body.style.backgroundColor = darkMode.backgroundColor)

    Main.darkModeContext.Provider(
      //TODO: Make this prettier
      darkMode.copy(toggleDarkMode = () => setDarkMode(s => s.copy(!s.isDarkMode, if (s.isDarkMode) "black" else "white", if (s.isDarkMode) "white" else "#282c34")))
    )(
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
            Route("/", Homepage.component, exact = true),
            Route("/posts/*", PostsPage.component),
            Route("*", Homepage.component)
          )
        )
      )
    )
  }

}
