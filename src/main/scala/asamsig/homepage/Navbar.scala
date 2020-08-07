package asamsig.homepage

import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.reactrouter.Link
import slinky.web.html.{div, header, style}

import scala.scalajs.js

@react object Navbar {
  val linkStyle = js.Dynamic.literal(
    color = "white",
    fontSize = "21px",
    fontWeight = 300,
    marginLeft = "30px",
    letterSpacing = "1.3px",
    textDecoration = "none"
  )

  val component = FunctionalComponent[Unit](_ => {
    header(style := js.Dynamic.literal(
      width = "100%",
      position = "fixed",
      top = 0,
      left = 0,
      backgroundColor = "#20232a"
    ))(
      div(
        style := js.Dynamic.literal(
          display = "flex",
          height = "60px",
          flexDirection = "row",
          alignItems = "center",
          justifyContent = "space-between",
          maxWidth = "1400px",
          marginLeft = "auto",
          marginRight = "auto"
        )
      )(
        div(
          style := js.Dynamic.literal(
            display = "flex",
            height = "100%",
            alignItems = "center",
            minWidth = "150px"
          )
        )(
          Link(to = "/")(
            style := js.Dynamic.literal(
              marginLeft = "25px",
              marginRight = "50px",
              color = "white",
              fontSize = "21px",
              fontWeight = 600,
              letterSpacing = "1.3px",
              textDecoration = "none"
            )
          )(
            "Alexander Samsig"
          )
        ),
        div(
          style := js.Dynamic.literal(
            display = "flex",
            height = "100%",
            alignItems = "center",
            paddingRight = "15px",
            marginRight = "auto"
          )
        )(
          Link(to = "/posts/welcome")(style := linkStyle)(
            "Blog"
          )
        )
      )
    )
  })
}
