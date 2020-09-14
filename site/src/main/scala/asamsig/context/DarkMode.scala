package asamsig.context

case class DarkMode(isDarkMode: Boolean = true, color: String = "white", backgroundColor: String = "#282C34", toggleDarkMode: () => Unit = () => ())