val scalaV = "2.13.3"

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

lazy val root = (project in file("."))
  // always run all commands on each sub project
  .aggregate(site, lambdaPathRewriter, infrastructure)
//  .dependsOn(site, lambdaPathRewriter, infrastructure) // this does the actual aggregation

lazy val site = (project in file("site"))
  .settings(scalaVersion := scalaV)

lazy val lambdaPathRewriter = (project in file("lambdaPathRewriter"))
  .settings(scalaVersion := scalaV)

lazy val infrastructure = (project in file("infrastructure"))
  .settings(scalaVersion := scalaV)

addCommandAlias("build", "fullOptJS::webpack")