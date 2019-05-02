package simple.scala.js.components.resume.side

import simple.scala.js.utils.css.SimpleStyles.CssSettings._

class SideBarStyles extends StyleSheet.Inline {

  import dsl._

  private val mediaMaxWidth = media.maxWidth(767.px)(
    position.static,
    width.inherit
  )

  val sidebarWrapper = style(
    background := darkcyan,
    position.absolute,
    right.`0`,
    width(240.px),
    height(100.%%),
    minHeight(800.px),
    color.white,
    unsafeChild("a")(
      color.white
    ),
    mediaMaxWidth
  )

  val profileContainer = style(
    padding(30.px),
    background := rgba(0, 0, 0, 0.2),
    textAlign.center,
    color.white
  )

  val profile = style(
    marginBottom(15.px),
    borderRadius(50.%%),
    verticalAlign.middle,
    maxWidth(100.px)
  )

  val name = style(
    fontSize(32.px),
    fontWeight._900,
    marginTop.`0`,
    marginBottom(10.px)
  )

  val tagLine = style(
    color(rgba(255, 255, 255, 0.6)),
    fontSize(16.px),
    fontWeight._400,
    marginTop.`0`,
    marginBottom.`0`
  )

  val fa: StyleA = style(
    fontSize(18.px),
    marginRight(5.px),
    verticalAlign.middle
  )

  val contactList: StyleA = style(
    unsafeChild("li")(
      marginBottom(15.px),
      &.lastChild(
        marginBottom.`0`
      )
    )
  )

  val containerBlock = style(
    padding(30.px)
  )

  val containerBlockTitle = style(
    textTransform.uppercase,
    fontSize(16.px),
    fontWeight._700,
    marginTop.`0`,
    marginBottom(15.px)
  )

  val euducationDegree = style(
    fontSize(14.px),
    marginTop.`0`,
    marginBottom(5.px),
  )

  val educationItem = style(
    marginBottom(15.px),
    &.lastChild(
      marginBottom.`0`
    )
  )

  val educationMeta = style(
    color(rgba(255, 255, 255, 0.6)),
    fontWeight._500,
    marginBottom.`0`,
    marginTop.`0`,
    fontSize(14.px)
  )

  val educationTime = style(
    color(rgba(255, 255, 255, 0.6)),
    fontWeight._500,
    marginBottom.`0`
  )

  val languageDesc = style(
    color(rgba(255, 255, 255, 0.6))
  )

  val languageList = style(
    marginBottom.`0`,
    unsafeChild("li")(
      marginBottom(10.px),
      &.lastChild(
        marginBottom.`0`
      )
    )
  )

  val qrCode = style(
    marginBottom(15.px),
    verticalAlign.middle,
    maxWidth(100.px)
  )

}
