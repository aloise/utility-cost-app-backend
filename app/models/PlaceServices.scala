package models

import java.time.LocalDateTime
import javax.inject.Inject

import models.base._
import models.helpers._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsValue}
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._


/**
  * User: aloise
  * Date: 23.05.16
  * Time: 22:28
  */
case class PlacesService (
  placeId:Int,
  serviceId:Int,
  created:LocalDateTime
)

class PlacesServices(tag:Tag)  extends IndexedTable[PlacesService](tag, "places_services") {
  def placeId = column[Int]("place_id")
  def serviceId = column[Int]("service_id")
  def created = column[LocalDateTime]("created")

  def * = ( placeId, serviceId, created ) <> ( PlacesService.tupled, PlacesService.unapply )


}



