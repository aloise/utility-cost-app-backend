package models.base

/**
  * User: aloise
  * Date: 29.05.16
  * Time: 21:13
  */
object ObjectAccess extends Enumeration {

  type Access = Value

  val Write = Value("write")
  val Read = Value("read")

}
