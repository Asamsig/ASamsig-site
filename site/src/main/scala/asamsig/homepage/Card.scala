package asamsig.homepage

import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.reactrouter.Link
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

@react object Card {

  val smallLinkStyle = (color: String) => js.Dynamic.literal(
    fontSize = "15px",
    color = color,
    fontWeight = 100,
    marginLeft = "15px",
    width = "33%",
    textDecoration = "none",
    transition = "color 0.3s"
  )

  case class Props(color: String, backgroundColor: String)

  val component = FunctionalComponent[Props] { props =>
    val linkStyle = smallLinkStyle(props.color)
    div(
      style := literal(
        width = "320px",
        marginLeft = "auto",
        marginRight = "auto",
        boxShadow = "rgba(0, 0, 0, 0.4) 0px 10px 16px 0px",
        transition = "background-color 0.3s, color 0.0s",
        padding = "30px",
        boxSizing = "border-box"
      )
    )(
      img(
        alt := "Alexander Samsig",
        style := literal(
          maxWidth = "100%",
          maxHeight = "205px",
          display = "block",
          borderRadius = "50%",
          border = s"3px ${props.color} solid",
          overflow = "hidden",
          marginLeft = "auto",
          marginRight = "auto",
          backgroundColor = "#b0aeaf"
        ),
        src := ProfilePicture.asInstanceOf[String]
      ),
      h1(
        style := literal(
          fontSize = "25px",
          marginTop = "10px",
          textAlign = "center",
        )
      )("Alexander Samsig"),
      hr(style := literal(
        marginBottom = "-1px",
        border = "none",
        borderBottom = "2px solid rgb(220 50 47)",
        marginTop = "20px"
      )),
      h2(
        style := literal(
          display = "block",
          textAlign = "center",
          marginTop = "10px",
          fontSize = "20px"
        )
      )("AWS & functional programming on the JVM"),
      div(style := literal(
        display = "flex",
        marginBottom = "20px",
      ))(
        Link(to = "/posts/welcome")(className := "cta-button", style := literal(
          margin = "0 auto"
        ))(
          "Blog"
        )
      ),
      div(
        style := literal(
          display = "flex",
          height = "100%",
          width = "100%",
          textAlign = "center"
        )
      )(
        a(
          href := "https://linkedin.com/in/alexander-samsig/",
          target := "_blank",
          rel := "noopener noreferrer",
          style := linkStyle
        )(
          "LinkedIn"
        ),
        a(
          href := "https://github.com/ASamsig",
          target := "_blank",
          rel := "noopener noreferrer",
          style := linkStyle
        )(
          "GitHub"
        ),
        a(
          href := "https://twitter.com/ASamsig",
          target := "_blank",
          rel := "noopener noreferrer",
          style := linkStyle
        )(
          "Twitter"
        )
      )
    )
  }
}
