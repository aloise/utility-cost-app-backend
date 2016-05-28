package models

import javax.inject.Inject

import models.base._
import models.helpers.SlickColumnExtensions._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

/**
  * User: aloise
  * Date: 23.05.16
  * Time: 21:44
  */
case class UsersPlace(
  userId:Int,
  placeId:Int,
  role:UserRole.Value
)

object UserRole extends Enumeration {
  type UserRole = Value

  implicit val mapper = enumColumnType( UserRole )

  val Admin = Value(1) // able to update
  val User = Value(2) // able to read

}

class UsersPlaces(tag:Tag) extends BaseTable[UsersPlace](tag, "users_places") {

  def userId = column[Int]("user_id")
  def placeId = column[Int]("place_id")
  def role = column[UserRole.Value]("role")

  def * = (userId, placeId, role) <> (UsersPlace.tupled, UsersPlace.unapply)

}


class UsersPlacesQuery extends slick.lifted.TableQuery[UsersPlaces]( tag => new UsersPlaces(tag) ) {

}

object UsersPlacesQuery  {

  def apply( ) = new UsersPlacesQuery()

}