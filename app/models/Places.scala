package models

import java.time.LocalDateTime
import javax.inject.Inject

import models.base.{IndexedRow, IndexedTable, IndexedTableComponent}
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted._
import slick.driver.H2Driver.api._

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

/*
class Places @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends IndexedTableComponent[Place, PlacesTable](slick.lifted.TableQuery[PlacesTable]) {

}
*/

class PlacesQuery extends slick.lifted.TableQuery[PlacesTable]( tag => new PlacesTable(tag) ) {

  def forUser( userId:Int ) = {
    for {
      place <- this
      user <- UsersQuery()
      userPlace <- UsersPlacesQuery()
      if ( user.id === userId ) && ( userPlace.placeId === place.id ) && ( userPlace.userId === user.id )
    } yield ( userPlace, place )
  }

}

object PlacesQuery {

  def apply( ) = new PlacesQuery()



}