val scalaV = "2.13.3"
val slinkyVersion = "0.6.5"

lazy val lambdaPathRewriter = (project in file("lambdaPathRewriter"))
  .settings(scalaVersion := scalaV)

lazy val site = (project in file("site"))
  .settings(scalaVersion := scalaV)

lazy val infrastructure = (project in file("infrastructure"))
  .settings(scalaVersion := scalaV)

addCommandAlias("dev", ";site / fastOptJS::startWebpackDevServer;~fastOptJS")

addCommandAlias("build", "site / fullOptJS::webpack")