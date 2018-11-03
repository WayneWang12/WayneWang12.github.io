name := "wayne-blog"

ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.12.7"

lazy val `wayne-blog` = (project in file("."))
  .enablePlugins(ParadoxSitePlugin)
  .enablePlugins(ParadoxPlugin)
  .enablePlugins(GhpagesPlugin)
  .enablePlugins(ParadoxMaterialThemePlugin)
  .settings(
    ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox),

    // #github-setting
    scmInfo := Some(ScmInfo(url("https://github.com/WayneWang12/WayneWang12.github.io.git"), "git@github.com:WayneWang12/WayneWang12.github.io.git")),
    git.remoteRepo := scmInfo.value.get.connection,
    name := "Wayne's Blog",
    ghpagesBranch := "master",
    // #github-setting

    // #source-setting
    sourceDirectory in Paradox := sourceDirectory.value / "main" / "paradox",

    sourceDirectory in Paradox in paradoxTheme := sourceDirectory.value / "main" / "paradox" / "_template",

    // #source-setting

    // #paradox
    paradoxProperties in Paradox ++= Map(
      "github.base_url" -> s"https://github.com/WayneWang12/Waynewang12.github.io/tree/blog",
      "snip.reactive-stream-demo.base_dir" -> s"${(baseDirectory in ThisBuild).value.getAbsolutePath}/reactive-stream-demo/src/test/scala/io/github/waynewang12",
      "snip.project.base_dir" -> (baseDirectory in ThisBuild).value.getAbsolutePath
    ),
    // #paradox
    paradoxMaterialTheme in Paradox := {
      ParadoxMaterialTheme()
        .withColor("indigo", "light-blue")
        .withFavicon("favicon.ico")
        .withLogoIcon("timeline")
        .withCopyright("Copyright © Wayne Wang")
        .withRepository(uri("https://github.com/WayneWang12/WayneWang12.github.io"))
        .withGoogleAnalytics("UA-124806176-1")
        .withSocial(
          uri("https://github.com/waynewang12"),
        )
    }
  )

// #doc-demo-sbt
lazy val docDemo = (project in file("docs")).
  enablePlugins(ParadoxPlugin).
  settings(
    name := "Demo Project",
    paradoxTheme := Some(builtinParadoxTheme("generic"))
  )
// #doc-demo-sbt

val akkaVersion = "2.5.17"

lazy val akkaStreamDemo = (project in file("reactive-stream-demo"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    )
  )

lazy val jmhDemo = (project in file("jmh-demo"))
  .enablePlugins(JmhPlugin)
  .settings(
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
      "com.madhukaraphatak" %% "java-sizeof" % "0.1",
      "it.unimi.dsi" % "fastutil" % "8.1.0",
      "com.carrotsearch" % "hppc" % "0.8.1",
      "net.sf.trove4j" % "trove4j" % "3.0.3",
      "com.madhukaraphatak" %% "java-sizeof" % "0.1",
      "com.koloboke" % "koloboke-api-jdk8" % "1.0.0",
      "com.koloboke" % "koloboke-impl-jdk8" % "1.0.0",
      "org.eclipse.collections" % "eclipse-collections" % "9.2.0")
  )
