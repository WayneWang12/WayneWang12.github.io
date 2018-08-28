name := "wayne-blog"

ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.12.6"

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
    // #source-setting

    // #paradox
    paradoxProperties in Paradox ++= Map(
      "github.base_url" -> s"https://github.com/WayneWang12/Waynewang12.github.io/tree/blog",
      "snip.project.base_dir" -> (baseDirectory in ThisBuild).value.getAbsolutePath
    ),
    // #paradox
    paradoxMaterialTheme in Paradox := {
      ParadoxMaterialTheme()
        .withColor("indigo", "light-blue")
        .withFavicon("favicon.ico")
        .withLogoIcon("timeline")
        .withCopyright("Copyright © Wayne Wang")
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