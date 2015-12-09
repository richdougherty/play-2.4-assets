name := """example"""

version := "1.0-SNAPSHOT"

def commonSettings = Seq(
  scalaVersion := "2.11.7"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .aggregate(frontend)
  .dependsOn(frontend)

lazy val frontend = project
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator