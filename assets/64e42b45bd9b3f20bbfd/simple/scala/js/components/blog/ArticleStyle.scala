package simple.scala.js.components.blog

import japgolly.scalajs.react.vdom.TagMod
import scalacss.ScalaCssReact._
import simple.scala.js.utils.css.SimpleStyles.CssSettings._

class ArticleStyle extends StyleSheet.Inline {

  import dsl._

  val dateHeader: TagMod = style(
    addClassName("teal"),
    boxShadow := "0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)",
    padding(5.px),
    width.auto,
    minHeight.`0`,
    fontSize(11.px),
    marginBottom(1.em),
    display.inlineBlock,
    unsafeChild("span")(
      color(c"#F0F0F0"),
      backgroundColor.transparent,
      padding.inherit,
      letterSpacing.inherit,
      margin.inherit
    )
  )

  val widget:TagMod = style(
    margin(30.px, 0.px),
    position.relative,
    minHeight.`0`
  )

  val h3Style: StyleA = style(
    display.inlineBlock,
    fontWeight.normal,
    background := lightseagreen,
    color.white,
    padding(3.px, 10.px, 0.px),
    borderTopRightRadius(3.px),
    borderTopLeftRadius(4.px),
    marginRight(2.px)
  )

  val ff = fontFace("myFont")(
    _.src("Helvetica Neue","Hiragino Sans GB","Microsoft YaHei")
  )

  val postGloable = style(
    fontSize(16.px),
    color(c"#3e3e3e"),
    lineHeight(1.6),
    wordSpacing(0.px),
    letterSpacing(0.px),
    fontFamily(ff)
  )

  val post = style(
    postGloable,
    boxShadow := "0 3px 6px rgba(0,0,0,0.16), 0 3px 6px rgba(0,0,0,0.23)",
    padding(5.px, 50.px, 10.px, 20.px),
    unsafeChild("p")(
      margin(1.5.em, 0.px)
    ),
    unsafeChild("h1,h2,h3,h4,h5,h6")(
      margin(1.5.em, 0.px),
      fontWeight.bold
    ),
    unsafeChild("h1")(
      fontSize(1.6.em),
    ),
    unsafeChild("h2")(
      fontSize(1.4.em),
    ),
    unsafeChild("h3")(
      fontSize(1.3.em),
      borderBottom(2.px, solid, lightseagreen),
    ),
    unsafeChild("h4")(
      fontSize(1.3.em),
    ),
    unsafeChild("h5")(
      fontSize(1.2.em),
    ),
    unsafeChild("h3 span")(
      h3Style
    ),
    unsafeChild("ul, ol")(
      paddingLeft(32.px)
    ),
    unsafeChild("ul")(
      listStyleType := "disc"
    ),
    unsafeChild("ol")(
      listStyleType := "decimal"
    ),
    unsafeChild("strong")(
      fontWeight.bold
    ),
    unsafeChild("blockquote")(
      display.block,
      padding(15.px, 1.rem),
      fontSize(0.9.em),
      margin(1.em, 0.px),
      color(c"#819198"),
      borderLeft(6.px, solid, c"#dce6f0"),
      background := c"#f2f7fb",
      overflow.auto,
      wordWrap.normal,
      wordBreak.normal
    ),

    unsafeChild("p code")(
      wordWrap.breakWord,
      padding(2.px, 4.px),
      borderRadius(4.px),
      margin(0.px, 2.px),
      color(c"#e96900"),
      background := c"#f8f8f8"
    ),
    &.lastChild(
      marginBottom.`0`,
      lineHeight(1.4)
    )
  )

  val postTitle:TagMod = style(
    margin(0.75.em),
    fontWeight._300,
    position.relative,
    unsafeChild("a")(
      &.visited(
        color(c"#0b5394")
      ),
      &.hover(
        color(c"#3d85c6")
      )
    )
  )

}
