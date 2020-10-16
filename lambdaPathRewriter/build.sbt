enablePlugins(ScalaJSBundlerPlugin, UniversalPlugin)

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

webpackConfigFile := Some(baseDirectory.value / "webpack.config.js")

// Optional: Disable source maps to speed up compile times
scalaJSLinkerConfig ~= { _.withSourceMap(false) }

// Incluce type defintion for aws lambda handlers
libraryDependencies += "net.exoego" %%% "aws-lambda-scalajs-facade" % "0.11.0"

// Optional: Include some nodejs types (useful for, say, accessing the env)
//libraryDependencies += "net.exoego" %%% "scala-js-nodejs-v12" % "0.12.0"

// Include scalatest
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % "test"

// Package lambda as a zip. Use `universal:packageBin` to create the zip
topLevelDirectory := None
mappings in Universal ++= (webpack in (Compile, fullOptJS)).value.map { f =>
  // remove the bundler suffix from the file names
  f.data -> f.data.getName().replace("-opt-bundle", "")
}

addCommandAlias("build", "universal:packageBin")