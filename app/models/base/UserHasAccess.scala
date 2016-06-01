package models.base

import slick.lifted.{LiteralColumn, Rep}
import slick.driver.H2Driver.api._

/**
  * User: aloise
  * Date: 01.06.16
  * Time: 10:39
  */
trait UserHasAccess[R, T <: BaseTable[R]] {
  this: BaseTableQuery[R,T] =>

  def hasAccess( objectId:Rep[Int] )( userId:Rep[Int], access:ObjectAccess.Access ):Rep[Boolean]

  def hasAccess( objectId:Int )( userId:Int, access:ObjectAccess.Access ): Rep[Boolean] =
    hasAccess( LiteralColumn(objectId) )( LiteralColumn(userId), access )


}
