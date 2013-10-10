import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  val appName = "FFTSite"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.pegdown" % "pegdown" % "1.3.0",
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "st.sparse" %% "persistent-map" % "0.1-SNAPSHOT",
    "st.sparse" %% "sundry" % "0.1-SNAPSHOT",
    "org.jumpmind.symmetric.jdbc" % "mariadb-java-client" % "1.1.1",
    //    "org.xerial" % "sqlite-jdbc" % "3.7.2",
    "org.scala-lang" %% "scala-pickling" % "0.8.0-SNAPSHOT",
    "securesocial" %% "securesocial" % "master-2.2-SNAPSHOT")

  val extraResolvers =
    resolvers += Resolver.url(
      "sbt-plugin-snapshots",
      new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(
        Resolver.ivyStylePatterns)

  val main = play.Project(appName, appVersion, appDependencies).settings(
    Keys.fork := true,
    scalaVersion := "2.10.3",
    extraResolvers,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    scalacOptions ++= Seq(
      "-optimize",
      "-feature",
      "-language:postfixOps",
      "-language:existentials"),
    resolvers += Resolver.file("LocalIvy", file(Path.userHome +
      java.io.File.separator + ".ivy2" + java.io.File.separator +
      "local"))(Resolver.ivyStylePatterns),
    libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _))
}