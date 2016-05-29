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
  * Time: 22:44
  */
case class ServiceRate(
  override val id:Option[Int],
  serviceId:Int,
  isActive: Boolean,
  activeFromDate:LocalDateTime,
  inactiveFromDate:Option[LocalDateTime],
  rateData:JsValue
) extends IndexedRow

class ServiceRates(tag:Tag) extends IndexedTable[ServiceRate](tag, "service_rates") {
  def serviceId = column[Int]("service_id")
  def isActive = column[Boolean]("is_active")
  def activeFromDate = column[LocalDateTime]("active_from_date")
  def inactiveFromDate = column[Option[LocalDateTime]]("inactive_from_date")
  def rateData = column[JsValue]("rate_data")

  def * = ( id.?,serviceId,isActive, activeFromDate, inactiveFromDate, rateData ) <> ( ServiceRate.tupled, ServiceRate.unapply )
}

object ServiceRatesQuery extends IndexedTableQuery[ServiceRate, ServiceRates]( tag => new ServiceRates(tag) ) {

}

