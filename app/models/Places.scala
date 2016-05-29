package models

import java.time.LocalDateTime

import models.base._
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted._
import slick.driver.H2Driver.api._

import scala.concurrent.Future

/**
  * User: aloise
  * Date: 23.05.16
  * Time: 21:17
  */
case class Place(
  override val id:Option[Int],
  title:String,
  country:String,
  city:String,
  state:String,
  zip:String,
  address:String
) extends IndexedRow

class PlacesTable(tag:Tag) extends IndexedTable[Place](tag, "places") {

  def title = column[String]("title")
  def country = column[String]("country")
  def city = column[String]("city")
  def state = column[String]("state")
  def zip = column[String]("zip")
  def address = column[String]("address")

  def * = (id.?, title, country, city, state, zip, address) <> (Place.tupled, Place.unapply)
}

object PlacesQuery extends IndexedTableQuery[Place,PlacesTable]( tag => new PlacesTable(tag) ) {

  def hasAccess( userId:Int, accessType: ObjectAccess.Access ) = {
    (
      for {
        user <- UsersQuery()
        userPlace <- UsersPlacesQuery()
        if
          ( user.id === userPlace.userId ) &&
          (
            ( userPlace.role === UserRole.Admin ) ||
            ( accessType == ObjectAccess.Read )
          )
      } yield user.id
    ).exists
  }

  def forUser( userId:Int ) = {
    for {
      place <- PlacesQuery
      user <- UsersQuery
      userPlace <- UsersPlacesQuery
      if ( user.id === userId ) && ( userPlace.placeId === place.id ) && ( userPlace.userId === user.id )
    } yield ( userPlace, place )
  }

}