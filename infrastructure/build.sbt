

enablePlugins(ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  // Explain type errors in more detail.
  "-explaintypes",
  // Warn when we use advanced language features
  "-feature",
  // Give more information on type erasure warning
  "-unchecked",
  // Enable warnings and lint
  "-Ywarn-unused",
  "-Xlint",
)
webpack / version := "4.44.1"

scalaJSUseMainModuleInitializer := true

// Optional: Disable source maps to speed up compile times
scalaJSLinkerConfig ~= {
  _.withSourceMap(false).withModuleKind(ModuleKind.CommonJSModule)
}

Compile / additionalNpmConfig := Map(
  "main" -> scalajsbundler.util.JSON.str("infrastructure-fastopt.js"),
)

webpackBundlingMode := BundlingMode.LibraryOnly()

Compile / npmDependencies += "@pulumi/aws" -> "3.2.1"
Compile / npmDependencies += "@pulumi/pulumi" -> "2.10.1"

stStdlib := List("esnext")

// Optional: Include some nodejs types (useful for, say, accessing the env)
//libraryDependencies += "net.exoego" %%% "scala-js-nodejs-v14" % "0.12.0"

// Include scalatest
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % "test"

// root webpack config file
webpackConfigFile := Some(baseDirectory.value / "webpack.config.js")