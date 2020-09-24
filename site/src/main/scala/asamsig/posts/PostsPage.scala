package asamsig.posts

import asamsig.Main
import asamsig.reacthelmet.Helmet
import asamsig.remarkreact.{ReactRenderer, Remark}
import io.scalajs.nodejs.fs.Fs.Dirent
import io.scalajs.nodejs.fs.{Fs, ReaddirOptions}
import org.scalajs.dom.raw.XMLHttpRequest
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.{Fragment, ReactElement}
import slinky.core.{FunctionalComponent, ReactComponentClass}
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

@react object RemarkH1 {

  case class Props(children: Seq[ReactElement])

  val component = FunctionalComponent[Props] { props =>
    Fragment(
      props.children.headOption.map { head =>
        Helmet(
          title(s"$head | ASamsig")
        )
      },
      h1(props.children: _*)
    )
  }
}

@react object RemarkH2 {

  case class Props(children: Seq[String])

  val component = FunctionalComponent[Props] { props =>
    Fragment(
      hr(style := literal(
        height = "1px",
        marginBottom = "-1px",
        border = "none",
        borderBottom = "1px solid #ececec",
        marginTop = "40px"
      )),
      h2(props.children.head)
    )
  }
}

object PostsTree {
  val tree: List[(String, List[(String, String)])] = {
    if (Main.isSSR) {
      val pageLocation = "../../../../public/posts"
      val ret = js.Dynamic.global.fs.asInstanceOf[Fs].readdirSync(pageLocation, ReaddirOptions(withFileTypes = true)).asInstanceOf[js.Array[Dirent]]
        .filter(dirent => dirent.isDirectory())
        .map(dirent => dirent.name.asInstanceOf[String])
      ret.toList.map(_ -> List())
    } else {
      List(
        "Posts" -> List()
      )
    }
  }
}

object TrackSSRPosts {
  var publicSSR: js.Dictionary[String] = js.Dictionary.empty[String]

  def getPublic(page: String): String = {
    val pageLocation = "../../../../public" + page
    val ret = js.Dynamic.global.fs.asInstanceOf[Fs].readFileSync(pageLocation, "UTF-8")
    publicSSR(page) = ret
    ret
  }
}

@react object PostsPage {
  def postsFilePath(props: js.Dynamic) = {
    val matchString = props.selectDynamic("match").params.selectDynamic("0").toString
    s"/posts/${matchString.reverse.dropWhile(_ == '/').reverse}.md"
  }

  val component = FunctionalComponent[js.Dynamic] { props =>
    val matchString = props.selectDynamic("match").params.selectDynamic("0").toString
    val selectedGroup = PostsTree.tree.find(_._2.exists(_._2 == s"/posts/${matchString}")).map(_._1)
    val (document, setDocument) = useState(() => {
      if (Main.isSSR) {
        Some(TrackSSRPosts.getPublic(postsFilePath(props)))
      } else if (js.typeOf(js.Dynamic.global.window.publicSSR) != "undefined") {
        js.Dynamic.global.window.publicSSR.asInstanceOf[js.Dictionary[String]].get(postsFilePath(props))
      } else None
    })

    useEffect(() => {
      val xhr = new XMLHttpRequest
      xhr.onload = _ => {
        setDocument(Some(xhr.responseText))
      }

      xhr.open("GET", postsFilePath(props))
      xhr.send()
    }, Seq(postsFilePath(props)))
    Main.darkModeContext.Consumer(darkMode =>
      div(className := s"article ${if (darkMode.isDarkMode) "dark" else "light"} fill-right", style := literal(
        marginTop = "40px",
        paddingLeft = "15px",
        boxSizing = "border-box"
      ))(
        div(style := literal(
          display = "flex",
          flexDirection = "row",
          transition = "background-color 0.3s, color 0.3s",
          color = darkMode.color
        ), className := "posts-page")(
          div(style := literal(
            width = "calc(100% - 300px)"
          ), className := "posts-content")(
            div(style := literal(maxWidth = "1400px"))(
              document.map { t =>
                Remark().use(ReactRenderer, literal(
                  remarkReactComponents = literal(
                    h1 = RemarkH1.component: ReactComponentClass[_],
                    h2 = RemarkH2.component: ReactComponentClass[_]
                  )
                )).processSync(t).result
              }
            )
          ),
          div(style := literal(
            width = "300px",
            marginLeft = "20px"
          ), className := "posts-sidebar")(
            div(
              style := literal(
                position = "fixed",
                top = "60px",
                height = "calc(100vh - 60px)",
                backgroundColor = "#f7f7f7",
                borderLeft = "1px solid #ececec",
                paddingTop = "40px",
                paddingRight = "1000px",
                boxSizing = "border-box"
              ),
              className := "posts-sidebar-content"
            )(
              nav(style := literal(
                position = "relative",
                paddingLeft = "20px",
                width = "300px"
              ))(
                PostsTree.tree.map { case (group, value) =>
                  PostsGroup(
                    name = group,
                    isOpen = selectedGroup.contains(group)
                  )(value).withKey(group)
                }
              )
            )
          )
        )
      )
    )
  }
}
