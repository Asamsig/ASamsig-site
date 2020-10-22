package asamsig.reactdarkmodetoggle

import slinky.core.ExternalComponent
import slinky.core.annotations.react

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

@JSImport("react-dark-mode-toggle", JSImport.Default)
@js.native
object ReactDarkModeToggle extends js.Object {
  def apply(): js.Object = js.native
}

@react object DarkModeToggle extends ExternalComponent {

  case class Props(onChange: () => Unit = () => (), checked: Boolean = false, size: Int | String = 85, speed: Double = 1.3)

  override val component = ReactDarkModeToggle
}
