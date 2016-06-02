package models

import java.time.LocalDateTime
import javax.inject.Inject

import models.base.ObjectAccess.Access
import models.base._
import models.helpers._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.Codecs
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsNull, JsObject, JsValue}
import slick.driver.H2Driver.api._
import models.helpers.SlickColumnExtensions._
import models.rate_data.RateDataContainer.{ManualPriceRateData, RateData}
import org.joda.money.{CurrencyUnit, Money}

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
    rateData:RateData = ManualPriceRateData(Money.zero(CurrencyUnit.USD)),
    override val isDeleted:Boolean = false
) extends IndexedRow {

  def calculateAmount( previousValue:BigDecimal, nextValue:BigDecimal ) =
    rateData.calculatePricePerMonth( previousValue, nextValue )

}

class ServiceRatesTable(tag:Tag) extends IndexedTable[ServiceRate](tag, "service_rates") {
  def serviceId = column[Int]("service_id")
  def isActive = column[Boolean]("is_active")
  def activeFromDate = column[LocalDateTime]("active_from_date")
  def inactiveFromDate = column[Option[LocalDateTime]]("inactive_from_date")
  def rateData = column[RateData]("rate_data")

  def * = ( id.?,serviceId,isActive, activeFromDate, inactiveFromDate, rateData, isDeleted ) <> ( ServiceRate.tupled, ServiceRate.unapply )
}

object ServiceRatesQuery extends IndexedTableQuery[ServiceRate, ServiceRatesTable]( tag => new ServiceRatesTable(tag) ) with UserHasAccess[ServiceRate] {


  def forService(serviceId: Int, includeInactive: Boolean) = {
    for {
      serviceRate <- this
      if !serviceRate.isDeleted && ( serviceRate.serviceId === serviceId ) && ( serviceRate.isActive || LiteralColumn( includeInactive ) )
    } yield serviceRate
  }

  override def hasAccess(access: Access)(serviceRateId: Rep[Int])(userId: Rep[Int]): Rep[Boolean] = {
    (
      for {
        serviceRate <- ServiceRatesQuery
        if
        !serviceRate.isDeleted &&
          ( serviceRate.id === serviceRateId ) &&
          ServicesQuery.hasAccess(access)( serviceRate.serviceId )( userId )
      } yield serviceRate.id
      ).exists
  }
}

