enablePlugins(ScalaJSBundlerPlugin)

name := "asamsig-site"

scalaVersion := "2.13.3"

Compile / npmDependencies += "react" -> "16.13.1"
Compile / npmDependencies += "react-dom" -> "16.13.1"
Compile / npmDependencies += "react-proxy" -> "1.1.8"

Compile / npmDependencies += "react-router-dom" -> "5.2.0"
Compile / npmDependencies += "remark" -> "12.0.1"
Compile / npmDependencies += "remark-react" -> "7.0.1"
Compile / npmDependencies += "react-helmet" -> "6.1.0"
Compile / npmDependencies += "history" -> "4.10.1"

Compile / npmDevDependencies += "url-loader" -> "4.1.0"
Compile / npmDevDependencies += "css-loader" -> "0.28.7"
Compile / npmDevDependencies += "html-webpack-plugin" -> "4.3.0"
Compile / npmDevDependencies += "copy-webpack-plugin" -> "6.0.3"
Compile / npmDevDependencies += "static-site-generator-webpack-plugin" -> "3.4.2"

webpack / version := "4.44.1"
startWebpackDevServer / version := "3.11.0"

fastOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack-fastopt.config.js")
fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack-opt.config.js")

fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot")
fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly()

libraryDependencies += "me.shadaj" %%% "slinky-web" % "0.6.5"
libraryDependencies += "me.shadaj" %%% "slinky-hot" % "0.6.5"
libraryDependencies += "me.shadaj" %%% "slinky-react-router" % "0.6.5"
libraryDependencies += "me.shadaj" %%% "slinky-history" % "0.6.5"

// if using Scala 2.13.0, instead use
scalacOptions += "-Ymacro-annotations"

addCommandAlias("dev", ";fastOptJS::startWebpackDevServer;~fastOptJS")

addCommandAlias("build", "fullOptJS::webpack")