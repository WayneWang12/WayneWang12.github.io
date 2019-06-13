name := "Wayne-Blog"

version := "0.1"

val commonSettings = Seq(
  scalaVersion := "2.12.8",
  scalacOptions ++= Seq(
//    "-Xfatal-warnings",
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Xfuture",
    "-Xlint:-unused,_",
    "-Ywarn-unused:imports",
    "-Ycache-macro-class-loader:last-modified"
  )
)

val scalaCssVersion     = "0.5.5"
val scalaJsReactVersion = "1.4.0"
val materialUIVersion   = "^0.40.0"
val reactVersion        = "16.7.0"

lazy val client = (project in file("client"))
  .settings(commonSettings: _*)
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js"                      %%% "scalajs-java-time" % "0.2.5",
      "com.github.japgolly.scalacss"      %%% "ext-react"         % scalaCssVersion,
      "com.github.japgolly.scalacss"      %%% "core"              % scalaCssVersion,
      "com.github.japgolly.scalajs-react" %%% "core"              % scalaJsReactVersion,
      "com.typesafe.play"                 %%% "play-json"         % "2.7.2",
      "com.github.japgolly.scalajs-react" %%% "extra"             % scalaJsReactVersion
    ),
//    webpackBundlingMode := BundlingMode.LibraryOnly(),
//    emitSourceMaps := false,
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "dev.webpack.config.js"),
    // Use a different Webpack configuration file for production
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "prod.webpack.config.js"),
    // Use the shared Webpack configuration file for reload workflow and for running the tests
    webpackConfigFile in Test := Some(baseDirectory.value / "common.webpack.config.js"),
    npmDevDependencies in Compile ++= Seq(
      "webpack-merge"        -> "4.2.1",
      "image-webpack-loader" -> "4.6.0",
      "css-loader"           -> "^2.1.0",
      "less"                 -> "3.0.0",
      "less-loader"          -> "^4.1.0",
      "url-loader"           -> "^1.1.2",
      "file-loader"          -> "^3.0.1",
      "style-loader"         -> "^0.23.1"
    ),
    npmDependencies in Compile ++= Seq(
      "react"                         -> reactVersion,
      "react-dom"                     -> reactVersion,
      "browser"                       -> "0.2.6",
      "materialize-css"               -> "1.0.0",
      "@fortawesome/fontawesome-free" -> "5.7.2",
      "material-design-icons"         -> "3.0.1",
      "marked"                        -> "0.6.2",
      "gitalk"                        -> "1.5.0",
      "highlight.js"                  -> "9.15.6"
    )
  )

lazy val macwire = {
  val macwireVersion = "2.3.1"
  Seq(
    "com.softwaremill.macwire" %% "macros"     % macwireVersion % "provided",
    "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % "provided",
    "com.softwaremill.macwire" %% "util"       % macwireVersion,
    "com.softwaremill.macwire" %% "proxy"      % macwireVersion
  )
}


val server =
  project.in(file("server"))
    .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
    .settings(commonSettings: _*)
    .settings(
      PlayKeys.devSettings := Seq("play.server.http.port" -> "9527"),
      scalaJSProjects := Seq(client),
      pipelineStages in Assets := Seq(scalaJSPipeline),
      pipelineStages := Seq(digest, gzip),
      ivyLoggingLevel := UpdateLogging.Quiet,
      libraryDependencies ++= macwire
    )

val play =
  project.in(file("."))
    .aggregate(client, server)
    .settings(
      ivyLoggingLevel := UpdateLogging.Quiet
    )
