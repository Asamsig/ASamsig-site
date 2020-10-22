addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.2.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.18.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.5")

resolvers += Resolver.bintrayRepo("oyvindberg", "converter")
// for Scala.js 1.x.x
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta25")