package models

import java.time.LocalDateTime
import javax.inject.Inject

import models.base._
import models.helpers._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsNull, JsObject, JsValue}
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
    activeFromDate:LocalDateTime = LocalDateTime.now(),
    inactiveFromDate:Option[LocalDateTime] = None,
    rateData:JsValue = JsNull,
    override val isDeleted:Boolean = false
) extends IndexedRow {

  def calculateAmount( previousValue:BigDecimal, nextValue:BigDecimal ) = ???

}

class ServiceRates(tag:Tag) extends IndexedTable[ServiceRate](tag, "service_rates") {
  def serviceId = column[Int]("service_id")
  def isActive = column[Boolean]("is_active")
  def activeFromDate = column[LocalDateTime]("active_from_date")
  def inactiveFromDate = column[Option[LocalDateTime]]("inactive_from_date")
  def rateData = column[JsValue]("rate_data")

  def * = ( id.?,serviceId,isActive, activeFromDate, inactiveFromDate, rateData, isDeleted ) <> ( ServiceRate.tupled, ServiceRate.unapply )
}

object ServiceRatesQuery extends IndexedTableQuery[ServiceRate, ServiceRates]( tag => new ServiceRates(tag) ) {

  def hasAccess( userId:Int, serviceRateId:Int, accessType: ObjectAccess.Access ): Rep[Boolean] = {
    hasAccess( LiteralColumn(userId), LiteralColumn(serviceRateId), accessType )
  }

  def hasAccess( userId:Rep[Int], serviceRateId:Rep[Int], accessType: ObjectAccess.Access  ): Rep[Boolean] = {
    (
      for {
        serviceRate <- ServiceRatesQuery
        if
          !serviceRate.isDeleted &&
          ( serviceRate.id === serviceRateId ) &&
          ServicesQuery.hasAccess( userId, serviceRate.serviceId, accessType )
      } yield serviceRate.id
    ).exists
  }


  def forService(serviceId: Int, includeInactive: Boolean) = {
    for {
      serviceRate <- this
      if !serviceRate.isDeleted && ( serviceRate.serviceId === serviceId ) && ( serviceRate.isActive || LiteralColumn( includeInactive ) )
    } yield serviceRate
  }

}

