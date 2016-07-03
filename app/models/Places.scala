package models

import java.time.LocalDateTime

import models.base.ObjectAccess.Access
import models.base._
import models.helpers.JsonJodaMoney
import org.joda.money.CurrencyUnit
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._

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
    address:String,
    currency: CurrencyUnit,
    override val isDeleted:Boolean = false
) extends IndexedRow

class PlacesTable(tag:Tag) extends IndexedTable[Place](tag, "PLACES") {

  def title = column[String]("title")
  def country = column[String]("country")
  def city = column[String]("city")
  def state = column[String]("state")
  def zip = column[String]("zip")
  def address = column[String]("address")
  def currency = column[CurrencyUnit]("currency")

  def * = (id.?, title, country, city, state, zip, address, currency, isDeleted) <> (Place.tupled, Place.unapply)
}

object PlacesQuery extends IndexedTableQuery[Place,PlacesTable]( tag => new PlacesTable(tag) ) with UserHasAccess[Place] {

  def findPlaceWithAccess(placeId:Int, userId:Int, accessType: ObjectAccess.Access ) = {
    (
      for {
        user <- UsersQuery
        place <- PlacesQuery
        userPlace <- UsersPlacesQuery
        if
          (place.id === placeId && !place.isDeleted) &&
          ( userPlace.placeId === placeId ) &&
          ( user.id === userPlace.userId && !user.isDeleted ) &&
          (
            ( userPlace.role === UserRole.Admin ) ||
            ( accessType == ObjectAccess.Read )
          )
      } yield place

    ).result.headOption
  }

  def forUser( userId:Int ) = {
    for {
      place <- PlacesQuery
      user <- UsersQuery
      userPlace <- UsersPlacesQuery
      if ( user.id === userId ) && !user.isDeleted && ( userPlace.placeId === place.id ) && !place.isDeleted && ( userPlace.userId === user.id )
    } yield ( userPlace, place )
  }

  override def hasAccess(access: Access)(placeId: Rep[Int])(userId: Rep[Int]): Rep[Boolean] = {
    (
      for {
        user <- UsersQuery
        place <- PlacesQuery
        userPlace <- UsersPlacesQuery
        if
          (place.id === placeId ) &&
          !place.isDeleted &&
          ( userPlace.placeId === placeId ) &&
          ( user.id === userPlace.userId && !user.isDeleted ) &&
          (
            ( userPlace.role === UserRole.Admin ) || ( access == ObjectAccess.Read )
          )
      } yield user.id
    ).exists

  }
}