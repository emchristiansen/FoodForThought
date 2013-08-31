import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "FoodForThoughtSite"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.pegdown" % "pegdown" % "1.3.0",
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "securesocial" %% "securesocial" % "master-SNAPSHOT")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns))

}
