package asamsig.homepage

import asamsig.Main
import org.scalajs.dom
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSImport

@JSImport("resources/alexander-samsig.png", JSImport.Default)
@js.native
object ProfilePicture extends js.Object

@react object Homepage {

  val component = FunctionalComponent[Unit] { _ =>
    useEffect(() => {
      dom.window.scrollTo(0, 0)
    })

    Main.darkModeContext.Consumer(darkMode =>
      div(style := literal(
        color = darkMode.color,
        paddingTop = "20px"
      ))(
        Card(darkMode.color, darkMode.backgroundColor)
      )
    )
  }
}
