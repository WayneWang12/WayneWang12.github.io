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
    scmInfo := Some(ScmInfo(url("https://github.com/WayneWang12/WayneWang12.github.io.git"), "git@github.com:WayneWang12/WayneWang12.github.io.git")),
    git.remoteRepo := scmInfo.value.get.connection,
    name := "Hello Project",
    paradoxMaterialTheme in Paradox := {
      ParadoxMaterialTheme()
        .withColor("red", "orange")
        .withFavicon("favicon.ico")
        .withLogoIcon("toys")
        .withCopyright("Copyright © Wayne Wang")
        .withSocial(
          uri("https://github.com/waynewang12"),
        )
    }
  )

