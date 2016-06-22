import models.rate_data.RateDataContainer._
import org.joda.money.{CurrencyUnit, Money}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec

/**
  * User: aloise
  * Date: 01.06.16
  * Time: 22:42
  */
class RateCalculationSpec extends PlaySpec {

  "Rate data" must {

    "return the correct manual price" in {
      val rate = ManualPriceRateData( Money.of(CurrencyUnit.USD, BigDecimal(124).bigDecimal ) )

      rate.calculatePricePerMonth(10, 20).getAmount.intValue() mustBe 124
    }

    "return the correct fixed price" in {
      val rate = new FixedPriceRateData( Money.of( CurrencyUnit.USD, 200 ) )

      rate.calculatePricePerMonth( 10, 20 ).getAmount.intValue() mustBe 200
      rate.calculatePricePerMonth( 10, 20 ).getCurrencyUnit mustBe CurrencyUnit.USD

    }

    "return the correct multi-rate price" in {

      val curr = CurrencyUnit.EUR

      val rateData = Seq(
        BigDecimal(50),
        BigDecimal(250),
        BigDecimal(500)
      )
      val priceData = Seq(
        Money.of( curr, 1 ),
        Money.of( curr, 2 ),
        Money.of( curr, 3 )
      )

      val rate = new ProgressiveRateData( rateData, priceData, Money.of( curr, 5  ) )
      val price = rate.calculatePricePerMonth( 100, 700 )

      price.getAmount.intValue() mustBe ( 1*50 + 2*200 + 3*250 + 5*100 )
      price.getCurrencyUnit mustBe curr

    }

  }

}
