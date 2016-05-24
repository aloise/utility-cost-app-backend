package models

import java.time.LocalDateTime

import models.base.{IndexedRow, IndexedTable}
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

class Places(tag:Tag) extends IndexedTable[Place](tag, "places") {

  def title = column[String]("name")
  def country = column[String]("email")
  def city = column[String]("password")
  def state = column[String]("password")
  def zip = column[String]("password")
  def address = column[String]("created")

  def * = (id.?, title, country, city, state, zip, address) <> (Place.tupled, Place.unapply)
}
