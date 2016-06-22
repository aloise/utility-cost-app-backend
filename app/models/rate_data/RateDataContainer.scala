package models.rate_data

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Currency

import julienrf.json.derived._
import org.joda.money.{CurrencyUnit, Money}
import play.api.libs.json._
import slick.ast.BaseTypedType
import slick.driver.H2Driver.api._
import slick.jdbc.JdbcType

import scala.util.Try

/**
  * User: aloise
  * Date: 01.06.16
  * Time: 21:50
  */

object RateDataContainer {

  import models.helpers.JsonJodaMoney.jodaMoneyJsonFormat

  sealed trait RateData {

    protected val zeroPrice = Money.zero(CurrencyUnit.USD)

    // Monthly payments
    def calculatePricePerMonth(previousValue: BigDecimal, newValue: BigDecimal): Money

  }

  case class BrokenPriceRateData(nothing:Int = 0) extends RateData {
    def calculatePricePerMonth(previousValue: BigDecimal, newValue: BigDecimal): Money = zeroPrice
  }

  case class ManualPriceRateData(amount:Money) extends RateData {

    def calculatePricePerMonth(previousValue: BigDecimal, newValue: BigDecimal): Money = amount


  }

  case class FixedPriceRateData(amountPerMonth: Money) extends RateData {
    /*
    Month
     */
    override def calculatePricePerMonth(previousValue: BigDecimal, newValue: BigDecimal): Money = {
      amountPerMonth
    }
  }

  /**
    * Case - Electricity
    * up to 50 KWh    - 1 USD / KWh
    * up to 250 KWh   - 2 USD / KWh
    * up to 500 KWh   - 3 USD / KWh
    * all exceeding   - 5 USD / KWh
    *
    * For example - 600 KWh
    * 1*50 + 2*200 + 3*250 + 5*100 ->
    * 50+250+200+100 == 600
    *
    * @param rates          list of rates in the format at top
    * @param exceedingPrice price for everything that is higher
    */

  case class ProgressiveRateData(rates: Seq[BigDecimal], prices:Seq[Money], exceedingPrice: Money) extends RateData {

    protected val ratesAsc = ( rates zip prices ).sortBy(_._1)

    override def calculatePricePerMonth(previousValue: BigDecimal, newValue: BigDecimal): Money = {
      val delta = newValue - previousValue
      if ((delta > 0) && ratesAsc.nonEmpty) {
        val currencyUnit = prices.head.getCurrencyUnit
        val (totalPrice, exceedingValue, _) =
        // ( price, amount, tariffSum )
          ratesAsc.foldLeft((BigDecimal(0), delta, BigDecimal(0))) { case ((price, valueLeft, prevTariffAmt), (tariffAmt, tariffPrice)) =>
            if (valueLeft <= 0) {
              (price, 0, prevTariffAmt)
            } else {

              val newTariffLimit = tariffAmt - prevTariffAmt

              val currentDelta = if (valueLeft > newTariffLimit) newTariffLimit else valueLeft

              (price + currentDelta * tariffPrice.getAmount, valueLeft - newTariffLimit, tariffAmt)
            }
          }

        Money.of(currencyUnit, (totalPrice + exceedingValue * exceedingPrice.getAmount).bigDecimal)

      } else {
        zeroPrice
      }
    }
  }

  implicit val rateDataFormat: OFormat[RateData] = oformat

  implicit val rateDataColumnType =
    MappedColumnType.base[RateData, String](
      rateData => {
        rateDataFormat.writes( rateData ).toString
      },
      string => {
        val json = Try( Json.parse(string) ).getOrElse(Json.obj())
        rateDataFormat.reads( json ).getOrElse( BrokenPriceRateData() )
      }
    )

}
