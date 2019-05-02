package simple.scala.js.components

import scalacss.internal.mutable.GlobalRegistry

import scala.scalajs.js

package object blog {

  val articleStyle = GlobalRegistry[ArticleStyle].get

  val jsDateOptions = new js.Object {
    val weekday = "short"
    val year    = "numeric"
    val month   = "short"
    val day     = "numeric"
  }

}
