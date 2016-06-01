package models.base

import slick.lifted.{LiteralColumn, Rep}
import slick.driver.H2Driver.api._

/**
  * User: aloise
  * Date: 01.06.16
  * Time: 10:39
  */
trait UserHasAccess[R] {

  def hasAccess( access:ObjectAccess.Access )( objectId:Rep[Int] )( userId:Rep[Int] ):Rep[Boolean]

  def hasReadAccess( objectId:Rep[Int] )( userId:Rep[Int] ):Rep[Boolean] = hasAccess(ObjectAccess.Read)(objectId)(userId)

  def hasReadAccess( objectId: Int, userId:Int ):Rep[Boolean] = hasAccess(ObjectAccess.Read)(objectId)(userId)

  def hasWriteAccess( objectId:Rep[Int] )( userId:Rep[Int] ):Rep[Boolean] = hasAccess(ObjectAccess.Write)(objectId)(userId)

  def hasWriteAccess( objectId:Int, userId:Int ):Rep[Boolean] = hasAccess(ObjectAccess.Write)(objectId)(userId)


}
