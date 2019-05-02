package simple.scala.js.components.resume.main

import simple.scala.js.utils.css.SimpleStyles.CssSettings._

class MainStyles extends StyleSheet.Inline {
  import dsl._

  def mediaMaxWidth = media.maxWidth(767.px)

  val body = style(
    color(c"#545E6C"),
    background := whitesmoke,
    fontSize(14.px),
    padding(30.px),
    unsafeChild("a")(
      &.hover(
        textDecoration := "underline",
        color(c"#1a454f")
      ),
      &.focus(
        textDecoration := "none"
      )
    ),
  )

  val wrapper = style(
    background := darkcyan,
    maxWidth(960.px),
    margin(0.px, auto),
    position.relative,
    boxShadow := "0px 2px 4px rgba(0,0,0,0.1)"
  )

  val mainWrapper = style(
    background := c"#fff",
    padding(60.px),
    paddingRight(300.px),
    pageBreakBefore.always,
    unsafeChild("h6")(
      fontWeight._700
    ),
    unsafeChild("a")(
      color.darkcyan
    ),
    unsafeChild("p")(
      lineHeight(1.5)
    ),
    mediaMaxWidth(
      padding(30.px)
    )
  )

  val sectionTitle = style(
    textTransform.uppercase,
    fontSize(20.px),
    fontWeight._500,
    color.darkcyan,
    position.relative,
    marginTop.`0`,
    marginBottom(20.px)
  )

  val fa = style(
    width(30.px),
    height(30.px),
    marginRight(8.px),
    display.inlineBlock,
    color.white,
    borderRadius(50.%%),
    background := darkcyan,
    textAlign.center,
    paddingTop(8.px),
    fontSize(16.px),
    position.relative,
    top(-2.px)
  )

  val section = style(
    marginBottom(60.px),
    pageBreakInside.avoid
  )

  val jobItem = style(
    marginBottom(30.px)
  )

  val upperRow = style(
    position.relative,
    overflow.hidden,
    marginBottom(2.px),
    mediaMaxWidth(
      marginBottom.`0`
    )
  )

  val jobTitle = style(
    color(c"#3f4650"),
    fontSize(16.px),
    marginTop.`0`,
    marginBottom.`0`,
    fontWeight._500
  )

  val jobTime = style(
    position.absolute,
    right.`0`,
    top.`0`,
    color.darkgray,
    mediaMaxWidth(
      position.static,
      display.block,
      marginTop(5.px)
    )
  )

  val jobCompany = style(
    marginBottom(10.px),
    color.darkgray
  )

  val projectTitle = style(
    fontSize(16.px),
    fontWeight._400,
    marginTop.`0`,
    marginBottom(5.px)
  )

  val projectIntro = style(
    marginBottom(30.px)
  )

  val projectItem = style(
    marginBottom(15.px)
  )

  val skillItem = style(
    display.inlineBlock,
    backgroundColor(c"#2d7788"),
    padding(3.px, 6.px),
    color.white,
    fontSize(15.px),
    margin(5.px)
  )

}
