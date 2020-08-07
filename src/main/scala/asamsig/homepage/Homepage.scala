package asamsig.homepage

import org.scalajs.dom
import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.annotation.JSImport

@JSImport("resources/alexander-samsig.png", JSImport.Default)
@js.native
object ProfilePicture extends js.Object

@react class Homepage extends StatelessComponent {
  type Props = Unit

  override def componentDidMount(): Unit = {
    dom.window.scrollTo(0, 0)
  }

  def render() = {
    div(style := literal(
      height = "calc(100vh - 60px)",
      color = "#ffffff",
      backgroundColor = "#282C34",
      paddingTop = "20px"
    ), className := "main-background")(
      Card.component()
    )
  }
}
