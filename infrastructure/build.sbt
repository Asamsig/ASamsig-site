import scalajsbundler.util.JSON

enablePlugins(ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)

webpack / version := "4.44.1"

scalaJSUseMainModuleInitializer := true

scalaJSLinkerConfig ~= {
  _
    // Optional: Disable source maps to speed up compile times
    .withOptimizer(false)
    .withSourceMap(false)
}

Compile / additionalNpmConfig := Map(
  "main" -> JSON.str("infrastructure-opt.js"),
)

webpackBundlingMode := BundlingMode.LibraryOnly()

//useYarn := true

Compile / npmDependencies += "@pulumi/aws" -> "3.5.0"
Compile / npmDependencies += "@pulumi/pulumi" -> "2.10.2"

stStdlib := List("esnext")

// Optional: Include some nodejs types (useful for, say, accessing the env)
libraryDependencies += "net.exoego" %%% "scala-js-nodejs-v14" % "0.12.0"

// Incluce type defintion for aws lambda handlers
libraryDependencies += "net.exoego" %%% "aws-lambda-scalajs-facade" % "0.11.0"

// Include scalatest
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.0" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"

// root webpack config file
webpackConfigFile := Some(baseDirectory.value / "webpack.config.js")